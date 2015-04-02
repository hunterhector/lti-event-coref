package edu.cmu.lti.event_coref.analysis_engine.resoluter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.features.semantic.AgentPatientFeatures;
import edu.cmu.lti.event_coref.features.semantic.EntityOfEventFeatures;
import edu.cmu.lti.event_coref.features.semantic.LocationFeatures;
import edu.cmu.lti.event_coref.features.semantic.SemaforRoleFeatures;
import edu.cmu.lti.event_coref.model.EventCorefConstants;
import edu.cmu.lti.event_coref.model.EventMentionRow;
import edu.cmu.lti.event_coref.model.EventMentionRowPair;
import edu.cmu.lti.event_coref.model.EventMentionTable;
import edu.cmu.lti.event_coref.pipeline.SudokuInferencePipelineControllerPool;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.APLUtils;
import edu.cmu.lti.event_coref.utils.ClusterUtils;
import edu.cmu.lti.event_coref.utils.SimilarityCalculator;
import edu.cmu.lti.event_coref.utils.eval.CorefChecker;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * This simple unification annotator will do two things:
 * <p/>
 * 1. Create cluster from the event mention
 * links
 * <p/>
 * 2. Copy information between events (you can disable this to make it faster)
 *
 * @author Zhengzhong Liu, Hector
 */
public class SudokuUnificationAnnotator extends JCasAnnotator_ImplBase {
    // public static final String PARAM_TRAIN_DOCUMENT_NUMBER = "TrainingDocumentNumber";
    private static final Logger logger = LoggerFactory.getLogger(SudokuUnificationAnnotator.class);

    public static final String PARAM_UPDATE_FEATURES = "NeedUpdateFeature";

    public static final String PARAM_DO_UNIFICATION = "DoUnification";

    public static final String PARAM_VERBOSE_LEVEL = "verboseLevel";

    public static final String PARAM_CLUSTER_METHOD = "clusterMethod";

    public static final String PARAM_UNIFICATION_CONFIDENCE_THRESHOLD = "UnificationThreshold";

    @ConfigurationParameter(name = PARAM_CLUSTER_METHOD, mandatory = true)
    private int clusterMethod;

    @ConfigurationParameter(name = PARAM_UPDATE_FEATURES, mandatory = true)
    private boolean needUpdateFeatures;

    @ConfigurationParameter(name = PARAM_DO_UNIFICATION, mandatory = true)
    private boolean doUnification;

    @ConfigurationParameter(name = PARAM_VERBOSE_LEVEL, mandatory = true)
    private int verboseLevel;

    @ConfigurationParameter(name = PARAM_UNIFICATION_CONFIDENCE_THRESHOLD, mandatory = true)
    private double unifConfidenceThreshold;

    private final String ANNOTATOR_COMPONENT_ID = "System-Sudoku-Unification";

    private ArrayListMultimap<TemporalVariable, TemporalRelation> headTemporalRelations;

    ArrayListMultimap<TemporalVariable, TemporalRelation> tailTemporalRealtions;

    private SimilarityCalculator rowFiller;

    List<String> guessAnnotatorNames = Arrays.asList(new String("System-APL-similar-slots"));

    Set<String> lowConfidentAnnotatorNames = new HashSet<String>(guessAnnotatorNames);

    private Random randomGen = new Random();

    private int unifyPairCount = 0;

    private Set<EventMention> changedEvents = new HashSet<EventMention>();

    private Set<EntityMention> changedEntities = new HashSet<EntityMention>();

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
        rowFiller = new SimilarityCalculator();

        if (doUnification) {
            System.err.println("Unification function is activated");
        } else {
            System.err.println("Unification function is not activated");
        }

        if (needUpdateFeatures) {
            System.err.println("Feature update function is activated");
        } else {
            System.err.println("Feature update function is not activated");
        }

    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        UimaConvenience.printProcessLog(aJCas, logger);
        String articleTitle = UimaConvenience.getShortDocumentName(aJCas);

        Collection<TemporalRelation> allTemporalRelations = JCasUtil.select(aJCas,
                TemporalRelation.class);
        headTemporalRelations = ArrayListMultimap.create();
        tailTemporalRealtions = ArrayListMultimap.create();

        EventMentionTable sTable = new EventMentionTable(aJCas);

        Collection<TemporalRelation> temporalRelations = JCasUtil.select(aJCas, TemporalRelation.class);
        HashBasedTable<TemporalVariable, TemporalVariable, TemporalRelation> temporalRelationTable = HashBasedTable
                .create();

        for (TemporalRelation tRelation : allTemporalRelations) {
            TemporalVariable tHead = tRelation.getTemporalVariableLhs();
            TemporalVariable tTail = tRelation.getTemporalVariableRhs();

            headTemporalRelations.put(tTail, tRelation);
            tailTemporalRealtions.put(tHead, tRelation);
        }

        Collection<PairwiseEventCoreferenceEvaluation> peceCorefEvals = JCasUtil.select(aJCas,
                PairwiseEventCoreferenceEvaluation.class);

        for (TemporalRelation tRelation : temporalRelations) {
            TemporalVariable t1 = tRelation.getTemporalVariableLhs();
            TemporalVariable t2 = tRelation.getTemporalVariableRhs();
            temporalRelationTable.put(t1, t2, tRelation);
        }

        int directTransferCount = 0;

        // NOTE: clean out cluster result produced by last iteration
        clearSystemClusters(aJCas);
        List<Set<EventMention>> fullCorefClusters = createCluster(peceCorefEvals, aJCas);

        for (PairwiseEventCoreferenceEvaluation pece : peceCorefEvals) {
            EventMention event1 = pece.getEventMentionI();
            EventMention event2 = pece.getEventMentionJ();

            if (!CorefChecker.isFullSystem(pece)) {
                for (Set<EventMention> fullCorefCluster : fullCorefClusters) {
                    if (fullCorefCluster.contains(event1) && fullCorefCluster.contains(event2)) {
                        pece.setEventCoreferenceRelationSystem(EventCorefConstants.FULL_COREFERENCE_TYPE_IN_PECE);
                        directTransferCount++;
                        break;
                    }
                }
            }

            if (doUnification) {
                if (CorefChecker.isFullSystem(pece) && !pece.getIsUnified()) {
                    double confidence = pece.getConfidence();
                    if (confidence > unifConfidenceThreshold) {
                        // System.out.println("Confidence level " + confidence);
                        unifyEvents(aJCas, pece);
                        changedEvents.add(event1);
                        changedEvents.add(event2);
                        pece.setIsUnified(true);
                    }
                }
            }
        }

        Map<EventMentionRowPair, PairwiseEventCoreferenceEvaluation> affectedRowPairs = findAffectedRowPairs(
                aJCas, peceCorefEvals, sTable);

        if (verboseLevel >= 1) {
            logger.debug("#Changed events : " + changedEvents.size());
            logger.debug("#Direct transfer : " + directTransferCount);
            logger.debug("#Affected pairs :" + affectedRowPairs.size());
        }

        if (needUpdateFeatures) {
            if (verboseLevel >= 1) {
                logger.debug("Updating features...");
            }
            updateNeccessaryFeatures(aJCas, affectedRowPairs.values(), sTable, rowFiller,
                    lowConfidentAnnotatorNames);
            if (verboseLevel >= 1)
                logger.debug("Finish update");
        }
    }

    /**
     * Remove clusters generated by this annotator to ensure no inconsistent clustering from different
     * steps
     *
     * @param aJCas
     */
    private void clearSystemClusters(JCas aJCas) {
        for (EventCoreferenceCluster cluster : UimaConvenience.getAnnotationList(aJCas,
                EventCoreferenceCluster.class)) {
            if (cluster.getComponentId().equals(ANNOTATOR_COMPONENT_ID)) {
                cluster.removeFromIndexes(aJCas);
            }
        }
    }

    /**
     * Create cluster
     *
     * @param peceCorefEvals
     * @return
     */
    private List<Set<EventMention>> createCluster(
            Collection<PairwiseEventCoreferenceEvaluation> peceCorefEvals, JCas aJCas) {

        List<Set<EventMention>> clusters = new ArrayList<Set<EventMention>>();

        if (clusterMethod == 0) {
            clusters = bestLinkCluster(peceCorefEvals);
        } else if (clusterMethod == 1) {
            clusters = transtiveClosureCluster(peceCorefEvals);
        }

        for (Set<EventMention> clusterSet : clusters) {
            EventCoreferenceCluster cluster = new EventCoreferenceCluster(aJCas);
            cluster.setChildEventMentions(FSCollectionFactory.createFSList(aJCas, clusterSet));
            cluster.setComponentId(ANNOTATOR_COMPONENT_ID);
            cluster.setClusterType(EventCorefConstants.FULL_COREFERENCE_TYPE);
            cluster.addToIndexes();
        }

        return clusters;
    }

    /**
     * Best link method, usually used in Entity coreference, which assume
     *
     * @param peceCorefEvals
     * @return
     */
    private List<Set<EventMention>> bestLinkCluster(
            Collection<PairwiseEventCoreferenceEvaluation> peceCorefEvals) {
        Map<EventMention, Pair<EventMention, Double>> bestLinks = new HashMap<EventMention, Pair<EventMention, Double>>();

        for (PairwiseEventCoreferenceEvaluation pece : peceCorefEvals) {
            EventMention evm1 = pece.getEventMentionI();
            EventMention evm2 = pece.getEventMentionJ();

            if (CorefChecker.isFullSystem(pece)) {
                double score = pece.getConfidence();
                if (bestLinks.containsKey(evm2)) {
                    double oldScore = bestLinks.get(evm2).getValue1();
                    if (oldScore < score) {
                        bestLinks.put(evm2, new Pair<EventMention, Double>(evm1, score));
                    }
                } else {
                    bestLinks.put(evm2, new Pair<EventMention, Double>(evm1, score));
                }
            }
        }

        List<Set<EventMention>> fullCorefClusters = new ArrayList<Set<EventMention>>();

        for (Entry<EventMention, Pair<EventMention, Double>> bestLink : bestLinks.entrySet()) {
            EventMention event1 = bestLink.getKey();
            EventMention event2 = bestLink.getValue().getValue0();

            boolean inPreviousCluster = false;
            for (Set<EventMention> cluster : fullCorefClusters) {
                if (cluster.contains(event1) || cluster.contains(event2)) {
                    cluster.add(event1);
                    cluster.add(event2);
                    inPreviousCluster = true;
                }
            }

            if (!inPreviousCluster) {
                // create a new cluster
                Set<EventMention> cluster = new HashSet<EventMention>();
                cluster.add(event1);
                cluster.add(event2);
                fullCorefClusters.add(cluster);
            }
        }

        return fullCorefClusters;
    }

    /**
     * A simple transitive closure method
     *
     * @param peceCorefEvals
     * @return
     */
    private List<Set<EventMention>> transtiveClosureCluster(
            Collection<PairwiseEventCoreferenceEvaluation> peceCorefEvals) {
        int goldenLotteryCount = 0;

        List<Set<EventMention>> fullCorefClusters = new ArrayList<Set<EventMention>>();
        for (PairwiseEventCoreferenceEvaluation pece : peceCorefEvals) {
            EventMention event1 = pece.getEventMentionI();
            EventMention event2 = pece.getEventMentionJ();

            if (CorefChecker.isFullSystem(pece)) {
                boolean inPreviousCluster = false;
                for (Set<EventMention> cluster : fullCorefClusters) {
                    if (cluster.contains(event1) || cluster.contains(event2)) {
                        cluster.add(event1);
                        cluster.add(event2);
                        inPreviousCluster = true;
                    }
                }

                if (!inPreviousCluster) {
                    // create a new cluster
                    Set<EventMention> cluster = new HashSet<EventMention>();
                    cluster.add(event1);
                    cluster.add(event2);
                    fullCorefClusters.add(cluster);
                }
            }
        }

        if (verboseLevel >= 1) {
            logger.debug("#Golden helps : " + goldenLotteryCount);
        }
        return fullCorefClusters;
    }

    private Map<EventMentionRowPair, PairwiseEventCoreferenceEvaluation> findAffectedRowPairs(JCas aJCas,
                                                                                              Collection<PairwiseEventCoreferenceEvaluation> peceCorefEvals, EventMentionTable sTable) {
        Map<EventMention, EventMentionRow> sudokuTable = sTable.getTableView();

        Map<EventMentionRowPair, PairwiseEventCoreferenceEvaluation> affectedPair2Pece = new HashMap<EventMentionRowPair, PairwiseEventCoreferenceEvaluation>();

        for (PairwiseEventCoreferenceEvaluation pece : peceCorefEvals) {
            EventMention event1 = pece.getEventMentionI();
            EventMention event2 = pece.getEventMentionJ();

            boolean eventAffected = changedEvents.contains(event1) || changedEvents.contains(event2);

            boolean entityAffected = false;
            for (EntityMention entity : getRelatedEntities(event1)) {
                if (changedEntities.contains(entity)) {
                    entityAffected = true;
                }
            }

            for (EntityMention entity : getRelatedEntities(event2)) {
                if (changedEntities.contains(entity)) {
                    entityAffected = true;
                }
            }

            if (eventAffected || entityAffected) {
                if (event1.getEventType().equals("event") && event2.getEventType().equals("event")) {
                    EventMentionRow row1 = sudokuTable.get(event1);
                    EventMentionRow row2 = sudokuTable.get(event2);
                    affectedPair2Pece.put(new EventMentionRowPair(row1, row2), pece);
                }
            }
        }

        return affectedPair2Pece;
    }

    private Set<EntityMention> getRelatedEntities(EventMention evm) {
        Set<EntityMention> relatedEntities = new HashSet<EntityMention>();

        for (EntityBasedComponent component : getComponents(evm.getAgentLinks())) {
            relatedEntities.addAll(getComponentRelatedEntityMentions(component));
        }

        return relatedEntities;
    }

    private List<EntityBasedComponent> getComponents(FSList componentLinkFS) {
        List<EntityBasedComponent> components = new ArrayList<EntityBasedComponent>();
        if (componentLinkFS != null) {
            for (EntityBasedComponentLink link : FSCollectionFactory.create(componentLinkFS,
                    EntityBasedComponentLink.class)) {
                components.add(link.getComponent());
            }
        }
        return components;
    }

    private Set<EntityMention> getComponentRelatedEntityMentions(EntityBasedComponent component) {
        Set<EntityMention> relatedEntities = new HashSet<EntityMention>();

        FSList entityMetionsFS = component.getContainingEntityMentions();
        if (entityMetionsFS != null) {
            for (EntityMention entityMention : FSCollectionFactory.create(entityMetionsFS,
                    EntityMention.class)) {
                relatedEntities.addAll(getClusterMentions(entityMention));
            }
        }

        return relatedEntities;
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        System.out.println("Number of unifications " + unifyPairCount);
        SudokuInferencePipelineControllerPool.unifiedEventCount = unifyPairCount;
    }

    /**
     * Only update features affected by the event changes
     *
     * @param aJCas
     * @param peceList
     * @param sTable
     * @param simCalc
     * @param lowConfidentAnnotatorNames
     */
    private void updateNeccessaryFeatures(JCas aJCas,
                                          Collection<PairwiseEventCoreferenceEvaluation> peceList, EventMentionTable sTable,
                                          SimilarityCalculator simCalc, Set<String> lowConfidentAnnotatorNames) {
        List<PairwiseFeatureGenerator> affectedFeatureGenerators = new ArrayList<PairwiseFeatureGenerator>();

        // affect agent, patient, location and entity features
        affectedFeatureGenerators.add(new AgentPatientFeatures(aJCas, sTable, simCalc,
                lowConfidentAnnotatorNames));
        affectedFeatureGenerators
                .add(new LocationFeatures(sTable, simCalc, lowConfidentAnnotatorNames));
        affectedFeatureGenerators.add(new SemaforRoleFeatures(simCalc));
        affectedFeatureGenerators.add(new EntityOfEventFeatures(aJCas));

        for (PairwiseEventCoreferenceEvaluation pece : peceList) {
            Collection<PairwiseEventFeature> features = FSCollectionFactory.create(
                    pece.getPairwiseEventFeatures(), PairwiseEventFeature.class);

            HashMap<String, PairwiseEventFeature> featureByName = new HashMap<String, PairwiseEventFeature>();
            for (PairwiseEventFeature feature : features) {
                featureByName.put(feature.getName(), feature);
            }

            EventMention event1 = pece.getEventMentionI();
            EventMention event2 = pece.getEventMentionJ();

            for (PairwiseFeatureGenerator generator : affectedFeatureGenerators) {
                for (PairwiseEventFeature feature : generator.createFeatures(aJCas, event1, event2)) {
                    String featureName = feature.getName();
                    if (featureByName.containsKey(featureName)) {
                        PairwiseEventFeature oldFeature = featureByName.get(featureName);
                        oldFeature.removeFromIndexes(aJCas); // remove the old feature
                    }
                    featureByName.put(feature.getName(), feature); // put in the new feature
                }
            }

            FSList oldFeatureFS = pece.getPairwiseEventFeatures();
            oldFeatureFS.removeFromIndexes(aJCas);// let's remove this too
            pece.setPairwiseEventFeatures(FSCollectionFactory.createFSList(aJCas, featureByName.values()));
        }

    }

    private void unifyEvents(JCas aJCas, PairwiseEventCoreferenceEvaluation pece) {
        unifyPairCount++;

        EventMention event1 = pece.getEventMentionI();
        EventMention event2 = pece.getEventMentionJ();

        if (verboseLevel >= 2) {
            logger.debug("Unifying " + event1.getGoldStandardEventMentionId() + " "
                    + event1.getCoveredText() + " <-> " + event2.getGoldStandardEventMentionId() + " "
                    + event2.getCoveredText());
        }
        // merge agents
        FSList agentLinks1FS = event1.getAgentLinks();
        FSList agentLinks2FS = event2.getAgentLinks();

        mergeEntityOnLinks(aJCas, event1, event2, agentLinks1FS, agentLinks2FS, APLUtils.AGENT_LINK_TYPE);

        // merge patients
        FSList patientLinks1FS = event1.getPatientLinks();
        FSList patientLinks2FS = event2.getPatientLinks();

        mergeEntityOnLinks(aJCas, event1, event2, patientLinks1FS, patientLinks2FS, APLUtils.PATIENT_LINK_TYPE);

        // merge locations
        FSList locationLinks1FS = event1.getLocationLinks();
        FSList locationLinks2FS = event2.getLocationLinks();

        mergeEntityOnLinks(aJCas, event1, event2, locationLinks1FS, locationLinks2FS, APLUtils.LOCATION_LINK_TYPE);

        // time currently not available
        // // merge time variables
        // TemporalVariable t1 = event1.getTemporalVariable();
        // TemporalVariable t2 = event2.getTemporalVariable();
        //
        // List<TemporalRelation> t1HeadTemporalRelations = headTemporalRelations.get(t1);
        // List<TemporalRelation> t1TailTemporalRelations = tailTemporalRealtions.get(t1);
        // List<TemporalRelation> t2HeadTemporalRelations = headTemporalRelations.get(t2);
        // List<TemporalRelation> t2TailTemporalRelations = tailTemporalRealtions.get(t2);
        //
        // // from t1 to t2
        // copyHeadTemporalRelations(aJCas, t2, t1HeadTemporalRelations);
        // copyTailTemporalRelations(aJCas, t2, t1TailTemporalRelations);
        //
        // // from t2 to t1
        // copyHeadTemporalRelations(aJCas, t1, t2HeadTemporalRelations);
        // copyTailTemporalRelations(aJCas, t1, t2TailTemporalRelations);
    }

    private void mergeEntityOnLinks(JCas aJCas, EventMention event1, EventMention event2,
                                    FSList componentLinks1, FSList componentLinks2, String linkType) {
        Collection<EntityBasedComponentLink> links1 = null;
        if (componentLinks1 != null)
            links1 = FSCollectionFactory.create(componentLinks1, EntityBasedComponentLink.class);

        Collection<EntityBasedComponentLink> links2 = null;
        if (componentLinks2 != null)
            links2 = FSCollectionFactory.create(componentLinks2, EntityBasedComponentLink.class);

        // if both have links, merge the entity cluster
        if (links1 != null && links2 != null) {
            for (EntityBasedComponentLink link1 : links1) {
                for (EntityBasedComponentLink link2 : links2) {
                    EntityBasedComponent comp1 = link1.getComponent();
                    EntityBasedComponent comp2 = link2.getComponent();

                    FSList Entities1FS = comp1.getContainingEntityMentions();
                    FSList Entities2FS = comp2.getContainingEntityMentions();

                    if (Entities1FS != null && Entities2FS != null) {
                        for (EntityMention mention1 : FSCollectionFactory.create(Entities1FS,
                                EntityMention.class)) {
                            for (EntityMention mention2 : FSCollectionFactory.create(Entities2FS,
                                    EntityMention.class)) {
                                mergeEntityMention(aJCas, mention1, mention2);
                            }
                        }
                    }
                }
            }
        } else { // if only one have, copy the other's to it
            if (links1 == null && links2 == null) {
                // do nothing if both null
            } else {
                if (links1 == null)
                    copyEntityBasedLinks(aJCas, event2, event1, componentLinks2, linkType);
                else if (links2 == null)
                    copyEntityBasedLinks(aJCas, event1, event2, componentLinks1, linkType);
            }
        }
    }

    private void copyEntityBasedLinks(JCas aJCas, EventMention fromEvent, EventMention toEvent,
                                      FSList fromLinks, String linkType) {
        Set<EntityBasedComponent> componentsToCopy = new HashSet<EntityBasedComponent>();
        if (fromLinks != null) {
            Collection<EntityBasedComponentLink> componentLinks1 = FSCollectionFactory.create(fromLinks,
                    EntityBasedComponentLink.class);
            for (EntityBasedComponentLink componentLink : componentLinks1) {
                EntityBasedComponent component = componentLink.getComponent();
                componentsToCopy.add(component);
            }
        }

        List<EntityBasedComponentLink> newComponentLinksForToEvent = APLUtils.addMultiLinksToEvent(
                aJCas, toEvent, componentsToCopy, linkType, ANNOTATOR_COMPONENT_ID);

        if (linkType.equals(APLUtils.AGENT_LINK_TYPE)) {
            toEvent.setAgentLinks(FSCollectionFactory.createFSList(aJCas, newComponentLinksForToEvent));
        } else if (linkType.equals(APLUtils.PATIENT_LINK_TYPE)) {
            toEvent.setPatientLinks(FSCollectionFactory.createFSList(aJCas, newComponentLinksForToEvent));
        } else if (linkType.equals(APLUtils.LOCATION_LINK_TYPE)) {
            toEvent.setLocationLinks(FSCollectionFactory.createFSList(aJCas, newComponentLinksForToEvent));
        }
    }

    private void mergeEntityMention(JCas aJCas, EntityMention mention1, EntityMention mention2) {
        EntityCoreferenceCluster entityMentionCluster1 = ClusterUtils.getEntityFullClusterSystem(mention1);
        EntityCoreferenceCluster entityMentionCluster2 = ClusterUtils.getEntityFullClusterSystem(mention2);

        List<EntityMention> cluster1Mentions = getClusterMentions(mention1);
        List<EntityMention> cluster2Mentions = getClusterMentions(mention2);

        if (!cluster1Mentions.equals(cluster2Mentions)) {
            if (verboseLevel >= 2) {
                System.out.println("Merging entity coreference clusters");
                System.out.println("before: ");
                printEntityMentionCluster(mention1);
                printEntityMentionCluster(mention2);
            }
            EntityCoreferenceCluster mergedClusterFS = new EntityCoreferenceCluster(aJCas);
            mergedClusterFS.setComponentId(ANNOTATOR_COMPONENT_ID);
            List<EntityMention> mergedCluster = new ArrayList<EntityMention>();
            for (EntityMention clusterMention1 : cluster1Mentions) {
                mergedCluster.add(clusterMention1);
            }
            for (EntityMention clusterMention2 : cluster2Mentions) {
                mergedCluster.add(clusterMention2);
            }

            mergedClusterFS.setEntityMentions(FSCollectionFactory.createFSList(aJCas, mergedCluster));

            for (EntityMention clusterMention1 : cluster1Mentions) {
                clusterMention1.setEntityCoreferenceClusters(UimaConvenience.replaceFSList(aJCas,
                        clusterMention1.getEntityCoreferenceClusters(), entityMentionCluster1,
                        mergedClusterFS, EntityCoreferenceCluster.class));
                // clusterMention1.setCoreferenceCluster(mergedClusterFS);
                changedEntities.add(clusterMention1);
            }

            for (EntityMention clusterMention2 : cluster2Mentions) {
                clusterMention2.setEntityCoreferenceClusters(UimaConvenience.replaceFSList(aJCas,
                        clusterMention2.getEntityCoreferenceClusters(), entityMentionCluster2,
                        mergedClusterFS, EntityCoreferenceCluster.class));
                // clusterMention2.setCoreferenceCluster(mergedClusterFS);
                changedEntities.add(clusterMention2);
            }

            if (entityMentionCluster1 != null)
                entityMentionCluster1.removeFromIndexes(aJCas);
            if (entityMentionCluster2 != null)
                entityMentionCluster2.removeFromIndexes(aJCas);

            if (verboseLevel >= 2) {
                System.out.println("after: ");
                printEntityMentionCluster(mention1);
                printEntityMentionCluster(mention2);
            }
        }
    }

    private List<EntityMention> getClusterMentions(EntityMention mention) {
        EntityCoreferenceCluster cluster = ClusterUtils.getEntityFullClusterSystem(mention);
        List<EntityMention> mentions = new ArrayList<EntityMention>();
        if (cluster != null) {
            for (EntityMention clusterMention : FSCollectionFactory.create(cluster.getEntityMentions(),
                    EntityMention.class)) {
                mentions.add(clusterMention);
            }
        } else {
            mentions.add(mention);
        }

        return mentions;
    }

    private void printEntityMentionCluster(EntityMention entity) {
        EntityCoreferenceCluster entityMentionCluster = ClusterUtils.getEntityFullClusterSystem(entity);
        System.out.print("{");
        if (entityMentionCluster != null) {
            for (EntityMention clusterMention : FSCollectionFactory.create(
                    entityMentionCluster.getEntityMentions(), EntityMention.class)) {
                System.out.print(clusterMention.getCoveredText().replace("\n", " ") + " ");
            }
        }
        System.out.println("}");
    }

    private void copyHeadTemporalRelations(JCas aJCas, TemporalVariable tvCopyTo,
                                           List<TemporalRelation> headTemporalRelationsCopyFrom) {
        for (TemporalRelation headTemporalRelation : headTemporalRelationsCopyFrom) {
            TemporalRelation newTRelation = new TemporalRelation(aJCas);
            TemporalVariable tHead = headTemporalRelation.getTemporalVariableLhs();
            newTRelation.setTemporalVariableLhs(tHead);
            newTRelation.setTemporalVariableRhs(tvCopyTo);

            String relationType = headTemporalRelation.getRelation();

            if (relationType.contains("+")) {
                newTRelation.setRelation("+");
            } else if (relationType.contains("-")) {
                newTRelation.setRelation("-");
            } else if (relationType.contains("<=")) {
                newTRelation.setRelation("<=");
            }

            newTRelation.addToIndexes();
        }
    }

    private void copyTailTemporalRelations(JCas aJCas, TemporalVariable tvCopyTo,
                                           List<TemporalRelation> tailTemporalRelationsCopyFrom) {
        for (TemporalRelation headTemporalRelation : tailTemporalRelationsCopyFrom) {
            TemporalRelation newTRelation = new TemporalRelation(aJCas);
            TemporalVariable tTail = headTemporalRelation.getTemporalVariableLhs();
            newTRelation.setTemporalVariableRhs(tTail);
            newTRelation.setTemporalVariableLhs(tvCopyTo);

            String relationType = headTemporalRelation.getRelation();

            if (relationType.contains("+")) {
                newTRelation.setRelation("+");
            } else if (relationType.contains("-")) {
                newTRelation.setRelation("-");
            } else if (relationType.contains("<=")) {
                newTRelation.setRelation("<=");
            }

            newTRelation.addToIndexes();
        }
    }

}