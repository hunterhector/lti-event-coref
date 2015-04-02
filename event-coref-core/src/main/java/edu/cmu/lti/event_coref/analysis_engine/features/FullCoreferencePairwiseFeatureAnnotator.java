package edu.cmu.lti.event_coref.analysis_engine.features;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.features.discourse.EventDistanceFeatures;
import edu.cmu.lti.event_coref.features.discourse.TitleFeatures;
import edu.cmu.lti.event_coref.features.lexical.EventStrictStringFeatures;
import edu.cmu.lti.event_coref.features.lexical.EventSurfaceStringFeatures;
import edu.cmu.lti.event_coref.features.semantic.*;
import edu.cmu.lti.event_coref.features.syntactic.EventSyntaticDependencyFeatures;
import edu.cmu.lti.event_coref.features.syntactic.ModifierFeatures;
import edu.cmu.lti.event_coref.features.syntactic.WordFormFeatures;
import edu.cmu.lti.event_coref.model.EventMentionTable;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.EventCoreferenceConstants;
import edu.cmu.lti.event_coref.utils.SimilarityCalculator;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Zhengzhong Liu, Hector
 */
public class FullCoreferencePairwiseFeatureAnnotator extends
        JCasAnnotator_ImplBase {
    public static final String PARAM_DO_FILTERING = "DoFiltering";

    public static final String PARAM_TARGET_COMPONENT_ID = "targetComponentId";

    @ConfigurationParameter(name = PARAM_TARGET_COMPONENT_ID, mandatory = true)
    String targetComponentId;

    @ConfigurationParameter(name = PARAM_DO_FILTERING)
    Boolean doFiltering;

    // this thing will get faster when used more because it caches things
    SimilarityCalculator simCalc = new SimilarityCalculator();

    List<String> guessAnnotatorNames = Arrays.asList("System-APL-similar-slots");

    Set<String> lowConfidentAnnotatorNames = new HashSet<String>(guessAnnotatorNames);

    private static final Logger logger = LoggerFactory.getLogger(FullCoreferencePairwiseFeatureAnnotator.class);

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException {
        super.initialize(aContext);

        if (doFiltering) {
            logger.info("Please note that feature annotator is initialized with filtering on!");
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        logger.info(String.format("Processing article: %s with [%s]",
                UimaConvenience.getShortDocumentName(aJCas), this.getClass()
                        .getSimpleName()));

        EventMentionTable sTable = new EventMentionTable(aJCas);
        logger.info("#Non epistemic domain events: "
                + sTable.getNumOfNonEpistemicdDomainEvents());

        logger.info("Creating features");
        // Step 1: Create each features
        List<PairwiseFeatureGenerator> allFeatureGenerators = new ArrayList<PairwiseFeatureGenerator>();
        allFeatureGenerators.add(new EventSurfaceStringFeatures(aJCas));
        allFeatureGenerators.add(new TitleFeatures(sTable));
        allFeatureGenerators.add(new EventDistanceFeatures(sTable));
        allFeatureGenerators.add(new EventSyntaticDependencyFeatures(aJCas));
        allFeatureGenerators.add(new AgentPatientFeatures(aJCas, sTable,
                simCalc, lowConfidentAnnotatorNames));
        allFeatureGenerators.add(new LocationFeatures(sTable, simCalc,
                lowConfidentAnnotatorNames));
        allFeatureGenerators.add(new ModifierFeatures(simCalc));
        allFeatureGenerators.add(new WordFormFeatures());
        allFeatureGenerators.add(new SemaforFeatures());
        allFeatureGenerators.add(new EventStrictStringFeatures(aJCas));
        allFeatureGenerators.add(new EventSemanticFeature());
        allFeatureGenerators.add(new EntityOfEventFeatures(aJCas));
        allFeatureGenerators.add(new SemaforRoleFeatures(simCalc));

        // allFeatureGenerators.add(new DbpediaNamedEventFeatures(aJCas));

        // Get all pairwise event coreference evaluations.
        List<PairwiseEventCoreferenceEvaluation> peceList = new ArrayList<PairwiseEventCoreferenceEvaluation>(
                JCasUtil.select(aJCas, PairwiseEventCoreferenceEvaluation.class));

        int numberPairs = peceList.size();

        logger.debug(String.format("Setting pairwise features for %d pair of events.", numberPairs));

        int pairCounter = 0;
        int tenPercent = numberPairs / 10;
        int percentCounter = 0;

        for (PairwiseEventCoreferenceEvaluation pece : peceList) {
            // only add features to the specific peces
            if (pece.getComponentId() != null) {
                if (!pece.getComponentId().equals(targetComponentId))
                    continue;
            }

            EventMention event1 = pece.getEventMentionI();
            EventMention event2 = pece.getEventMentionJ();

            List<PairwiseEventFeature> allFeatures = new ArrayList<PairwiseEventFeature>();

            boolean discardExample = false;

            if (doFiltering) {
                Table<EventMention, EventMention, EventSurfaceSimilarity> surfaceSimilarityTable = HashBasedTable.create();

                for (EventSurfaceSimilarity ess : JCasUtil.select(aJCas,
                        EventSurfaceSimilarity.class)) {
                    EventMention eevm1 = ess.getEventMentionI();
                    EventMention eevm2 = ess.getEventMentionJ();
                    surfaceSimilarityTable.put(eevm1, eevm2, ess);
                }

                Table<EventMention, EventMention, Boolean> semanticDbDecisions = HashBasedTable.create();

                for (SemanticDatabasePairwiseDecision sdpd : JCasUtil.select(
                        aJCas, SemanticDatabasePairwiseDecision.class)) {
                    EventMention eevm1 = sdpd.getEventI();
                    EventMention eevm2 = sdpd.getEventJ();
                    semanticDbDecisions.put(eevm1, eevm2, sdpd.getIsFullCoref());
                }

                EventSurfaceSimilarity surfaceSim = surfaceSimilarityTable.get(event1, event2);

                double wordNetSim = surfaceSim.getWordNetWuPalmer();
                double sennaSim = surfaceSim.getSennaSimilarity();
                double diceSim = surfaceSim.getDiceCoefficient();

                boolean isFullBySemanticDb = semanticDbDecisions.get(event1, event2);

                if (!(wordNetSim > 0.4 || sennaSim > 0.2 || diceSim > 0.4 || isFullBySemanticDb)) {
                    discardExample = true;
                }
            }

            if (!discardExample) {
                for (PairwiseFeatureGenerator featureGenerator : allFeatureGenerators) {
                    List<PairwiseEventFeature> currentFeatures = featureGenerator
                            .createFeatures(aJCas, event1, event2);
                    if (currentFeatures == null) {
                        logger.info("Some feature generator is returning NULL!");
                        throw new AnalysisEngineProcessException();
                    }
                    allFeatures.addAll(currentFeatures);
                }
                if (pece.getPairwiseEventFeatures() == null) {
                    pece.setPairwiseEventFeatures(FSCollectionFactory.createFSList(aJCas, allFeatures));
                } else {
                    pece.setPairwiseEventFeatures(UimaConvenience.appendAllFSList(aJCas,
                            pece.getPairwiseEventFeatures(), allFeatures, PairwiseEventFeature.class));
                }
            } else {
                // directly set as NO
                pece.setEventCoreferenceRelationSystem(EventCoreferenceConstants.EventCoreferenceRelationType.NO.toString());
            }

            pairCounter++;

            if (tenPercent > 0) {
                if (pairCounter % tenPercent == 0) {
                    percentCounter++;
                    logger.info(String.format("%d0 %s (%d pairs) of the event pairs finished", percentCounter, "%", pairCounter));
                }
            }
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException {
        super.collectionProcessComplete();
    }
}