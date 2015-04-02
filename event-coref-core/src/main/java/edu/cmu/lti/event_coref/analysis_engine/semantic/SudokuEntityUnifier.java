package edu.cmu.lti.event_coref.analysis_engine.semantic;

import com.google.common.collect.ArrayListMultimap;
import edu.cmu.lti.event_coref.model.EventCorefConstants;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.ClusterUtils;
import edu.cmu.lti.utils.model.AnnotationCondition;
import edu.cmu.lti.utils.model.Span;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * Propagate entity information inside the entity clusters
 *
 * @author Zhengzhong Liu, Hector
 */
public class SudokuEntityUnifier extends JCasAnnotator_ImplBase {
    private boolean debug = true;

    public static final String ANNOTATOR_COMPONENTID = "System-entity-unify";

    private static final Logger logger = LoggerFactory.getLogger(SudokuEntityUnifier.class);

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {

        super.initialize(aContext);

    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        logger.info(String.format("Processing article: %s with [%s]",
                UimaConvenience.getShortDocumentName(aJCas), this.getClass().getSimpleName()));

        // List<StanfordEntityCoreferenceCluster> stanfordCoreferenceClusters = UimaConvenience
        // .getAnnotationList(aJCas, StanfordEntityCoreferenceCluster.class);

        Map<Span, EntityMention> span2EntityMention = new HashMap<Span, EntityMention>();
        Map<EntityMention, EntityCoreferenceCluster> clusteredEntityMentions = new HashMap<EntityMention, EntityCoreferenceCluster>();

        // 1 Add Stanford entities and coreferences
        // 1.1 Stanford entities
        ArrayListMultimap<StanfordEntityCoreferenceCluster, EntityMention> sc2em = ArrayListMultimap
                .create();
        for (StanfordEntityMention sem : UimaConvenience.getAnnotationList(aJCas,
                StanfordEntityMention.class)) {
            int begin = sem.getBegin();
            int end = sem.getEnd();
            EntityMention em = new EntityMention(aJCas, begin, end);
            em.setEntityType(sem.getEntityType());
            em.addToIndexes(aJCas);
            em.setComponentId(ANNOTATOR_COMPONENTID);

            span2EntityMention.put(new Span(begin, end), em);

            StanfordEntityCoreferenceCluster stanfordCluster = sem.getEntityCoreferenceCluster();
            if (stanfordCluster != null) {
                sc2em.put(stanfordCluster, em);
            }
        }

        // 1.2 Associate to Stanford clusters
        for (StanfordEntityCoreferenceCluster sc : sc2em.keySet()) {
            List<EntityMention> ems = sc2em.get(sc);
            EntityCoreferenceCluster emCluster = new EntityCoreferenceCluster(aJCas);
            emCluster.setEntityMentions(FSCollectionFactory.createFSList(aJCas, ems));
            emCluster.addToIndexes(aJCas);
            emCluster.setComponentId(ANNOTATOR_COMPONENTID);

            for (EntityMention em : ems) {
                ClusterUtils.setEntityFullClusterSystem(aJCas, em, emCluster);
                clusteredEntityMentions.put(em, emCluster);
            }
        }

        // 2 Associate subj of relative clause modifier to the NP it modifies
        // 2.1 find rcmod relations
        AnnotationCondition npFilter = new AnnotationCondition() {
            @Override
            public Boolean check(TOP aAnnotation) {
                OpennlpChunk chunk = (OpennlpChunk) aAnnotation;
                return chunk.getTag().equals(EventCorefConstants.NOUN_PHRASE_TAG);
            }
        };

        Map<StanfordDependencyNode, OpennlpChunk> token2NounPhrase = new HashMap<StanfordDependencyNode, OpennlpChunk>();
        for (OpennlpChunk np : UimaConvenience.getAnnotationListWithFilter(aJCas, OpennlpChunk.class,
                npFilter)) {
            for (StanfordDependencyNode word : JCasUtil.selectCovered(StanfordDependencyNode.class, np)) {
                token2NounPhrase.put(word, np);
            }
        }

        AnnotationCondition rcFilter = new AnnotationCondition() {
            @Override
            public Boolean check(TOP aAnnotation) {
                StanfordDependencyRelation sdr = (StanfordDependencyRelation) aAnnotation;
                return sdr.getRelationType().equals("rcmod");
            }
        };

        Map<EntityMention, EntityMention> pairwiseCoreferences = new HashMap<EntityMention, EntityMention>();

        List<StanfordDependencyRelation> rcmodRelations = UimaConvenience.getAnnotationListWithFilter(
                aJCas, StanfordDependencyRelation.class, rcFilter);
        for (StanfordDependencyRelation rcmodRel : rcmodRelations) {
            StanfordDependencyNode modifier = rcmodRel.getChild();
            FSList childRelations = modifier.getChildRelations();
            if (childRelations != null) {
                for (StanfordDependencyRelation childRelation : FSCollectionFactory.create(
                        modifier.getChildRelations(), StanfordDependencyRelation.class)) {
                    String childRelationType = childRelation.getRelationType();
                    if (childRelationType.equals("nsubj") || childRelationType.equals("rel")) {
                        StanfordDependencyNode modifierChild = childRelation.getChild();
                        if (modifierChild.getToken().getPos()
                                .startsWith(EventCorefConstants.WH_WORD_LABEL_PREFIX)) {
                            StanfordDependencyNode rcmodHead = rcmodRel.getHead();
                            OpennlpChunk rcmodHeadNp = token2NounPhrase.get(rcmodHead);
                            Span rcmodHeadNPSpan = new Span(rcmodHead.getBegin(), rcmodHead.getEnd());
                            if (debug) {
                                logger.info(String.format("Linking %s and %s as coreference, verb is %s",
                                        rcmodHead.getCoveredText(), modifierChild.getCoveredText(),
                                        modifier.getCoveredText()));
                            }
                            EntityMention rcmodHeadMention, whMention;
                            if (span2EntityMention.containsKey(rcmodHeadNPSpan)) {
                                rcmodHeadMention = span2EntityMention.get(rcmodHeadNPSpan);
                            } else {
                                rcmodHeadMention = new EntityMention(aJCas, rcmodHeadNPSpan.getBegin(),
                                        rcmodHeadNPSpan.getEnd());
                                rcmodHeadMention.addToIndexes(aJCas);
                                rcmodHead.setComponentId(ANNOTATOR_COMPONENTID);
                            }
                            int whWordBegin = modifierChild.getBegin();
                            int whWordEnd = modifierChild.getEnd();
                            Span whWordSpan = new Span(whWordBegin, whWordEnd);
                            if (span2EntityMention.containsKey(whWordSpan)) {
                                whMention = span2EntityMention.get(whWordSpan);
                            } else {
                                whMention = new EntityMention(aJCas, whWordBegin, whWordEnd);
                                whMention.addToIndexes(aJCas);
                                whMention.setComponentId(ANNOTATOR_COMPONENTID);
                            }
                            pairwiseCoreferences.put(rcmodHeadMention, whMention);
                        }
                    }
                }
            }
        }

        // 2.2 add wh coreference to clusters
        for (Entry<EntityMention, EntityMention> entry : pairwiseCoreferences.entrySet()) {
            EntityMention em1 = entry.getKey();
            EntityMention em2 = entry.getValue();

            EntityCoreferenceCluster emCluster;
            if (clusteredEntityMentions.containsKey(em1) || clusteredEntityMentions.containsKey(em2)) {
                if (clusteredEntityMentions.containsKey(em1) && clusteredEntityMentions.containsKey(em2)) {
                    // TODO: merge the two exisiting clusters
                    logger.error("We haven't implemented this cuz it is not likely!");
                }
                List<EntityMention> entitiesInCluster;
                if (clusteredEntityMentions.containsKey(em1)) {
                    emCluster = clusteredEntityMentions.get(em1);
                    entitiesInCluster = new LinkedList<EntityMention>(FSCollectionFactory.create(
                            emCluster.getEntityMentions(), EntityMention.class));
                    entitiesInCluster.add(em2);
                } else {
                    emCluster = clusteredEntityMentions.get(em2);
                    entitiesInCluster = new LinkedList<EntityMention>(FSCollectionFactory.create(
                            emCluster.getEntityMentions(), EntityMention.class));
                    entitiesInCluster.add(em1);
                }
                unifyEntityType(entitiesInCluster);
                emCluster.setEntityMentions(FSCollectionFactory.createFSList(aJCas, entitiesInCluster));
            } else {
                List<EntityMention> ems = Arrays.asList(em1, em2);
                unifyEntityType(ems);
                emCluster = new EntityCoreferenceCluster(aJCas);
                emCluster.setEntityMentions(FSCollectionFactory.createFSList(aJCas, ems));
                emCluster.addToIndexes(aJCas);
                emCluster.setComponentId(ANNOTATOR_COMPONENTID);

                clusteredEntityMentions.put(em1, emCluster);
                clusteredEntityMentions.put(em2, emCluster);
            }

            ClusterUtils.setEntityFullClusterSystem(aJCas, em1, emCluster);
            ClusterUtils.setEntityFullClusterSystem(aJCas, em2, emCluster);
        }

        // 3. Unify entity types in all clusters
        for (EntityCoreferenceCluster cluster : UimaConvenience.getAnnotationList(aJCas,
                EntityCoreferenceCluster.class)) {
            unifyEntityType(FSCollectionFactory.create(cluster.getEntityMentions(), EntityMention.class));
        }

    }

    /**
     * Propagate the collection of entity type information if compatible
     *
     * @param entityMentions
     */
    private void unifyEntityType(Collection<EntityMention> entityMentions) {
        String entityType = null;
        boolean compatible = true;
        for (EntityMention em : entityMentions) {
            String newEntityType = em.getEntityType();
            if (newEntityType != null) {
                if (entityType == null) {
                    entityType = newEntityType;
                } else {
                    if (!entityType.equals(newEntityType)) {
                        compatible = false;
                        break;
                    }
                }
            }
        }
        if (compatible && entityType != null) {
            for (EntityMention em : entityMentions) {
                em.setEntityType(entityType);
            }
        }
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        super.collectionProcessComplete();
    }
}
