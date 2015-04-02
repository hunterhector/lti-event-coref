package edu.cmu.lti.event_coref.pipeline;

import edu.cmu.lti.event_coref.DefaultConfigs;
import edu.cmu.lti.event_coref.analysis_engine.FanseAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.SemaforAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.StanfordCoreNlpAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.features.FullCoreferencePairwiseFeatureAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.features.PairwiseEventFeatureContainerGenerator;
import edu.cmu.lti.event_coref.analysis_engine.lexical.EventSurfaceSimilarityAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.location.IntegratedLocationAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.mention.GoldStandardBasedEventMentionAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.mention.NaiveReportingEventAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.prerequisite.*;
import edu.cmu.lti.event_coref.analysis_engine.semantic.*;
import edu.cmu.lti.event_coref.analysis_engine.syntatic.EventDirectSyntaticRelationAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.syntatic.ExtendedChunkerAnnotator;
import edu.cmu.lti.event_coref.io.ReaderWriterFactory;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * Date: 3/26/15
 * Time: 9:45 PM
 *
 * @author Zhengzhong Liu
 */
public class EventCorefProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EventCorefProcessor.class);

    private final String parentOutputDir;

    //the rest are defaults, relative to base directory
    private final String eventFrameNamePath;
    private final String vn2FnPath;
    private final String vn2pbPath;
    private final String frRelataionPath;
    private final String fanseResourceDir;
    private final String semaforModelDir;
    private final String sennaEmbedding;
    private final String sennaWordlist;
    private final String wordGazetteer;
    private final String stringRuleFile;
    private final String suTimeConfPath;
    private final String featureNamePath;

    private static TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory
            .createTypeSystemDescription(DefaultConfigs.TypeSystemDescriptorName);

    // Parameters for the writer
    private static final String baseOutputDirName = "xmi_processed";
    private static final String outputFileSuffix = null;
    private static final int outputStepNumber = 1;

    private String goldStandardViewName = DefaultConfigs.goldStandardViewName;
    private String targetEventType = DefaultConfigs.targetEventType;
    private String[] viewsToAnnotate = {CAS.NAME_DEFAULT_SOFA};

    public static final Boolean doWordNet = false;
    public static final Boolean DoFilteringWhenFeatureGeneration = false;
    public static final String targetComponentId = PairwiseEventFeatureContainerGenerator.ANNOTATOR_COMPONENT_ID;

    public EventCorefProcessor(String parentOutputDir, String resourceDir) {
        this.parentOutputDir = parentOutputDir;
        this.eventFrameNamePath = new File(resourceDir, "FrameNet/event_frames.txt").getAbsolutePath();
        this.vn2FnPath = new File(resourceDir, "FrameNet/SemLink_1.2.2c/vn-fn/VN-FNRoleMapping.txt").getAbsolutePath();
        this.vn2pbPath = new File(resourceDir, "FrameNet/SemLink_1.2.2c/vn-pb/vnpbMapping").getAbsolutePath();
        this.frRelataionPath = new File(resourceDir, "frRelation.xml").getAbsolutePath();
        this.fanseResourceDir = new File(resourceDir, "fanse").getAbsolutePath();
        this.semaforModelDir = new File(resourceDir, "semafor/semafor_malt_model_20121129").getAbsolutePath();
        this.sennaEmbedding = new File(resourceDir, "senna/embeddings.txt").getAbsolutePath();
        this.sennaWordlist = new File(resourceDir, "senna/words.lst").getAbsolutePath();
        this.wordGazetteer = new File(resourceDir, "the_world_gazetteer/dataen.txt").getAbsolutePath();
        this.stringRuleFile = new File(resourceDir, "biu/string_rules.txt").getAbsolutePath();
        this.suTimeConfPath = new File(resourceDir, "sutime").getAbsolutePath();
        this.featureNamePath = new File(resourceDir, "feature_lists/featureNames.txt").getAbsolutePath();
    }

    public AnalysisEngineDescription[] getDefaultEngineDescriptors() throws ResourceInitializationException {
        AnalysisEngineDescription goldStandardLuEngine = AnalysisEngineFactory.createEngineDescription(
                GoldStandardBasedBasicLanguageUnitAnnotator.class, typeSystemDescription,
                GoldStandardBasedBasicLanguageUnitAnnotator.PARAM_GOLD_STANDARD_VIEWNAME, goldStandardViewName);

        AnalysisEngineDescription stanfordEngine = AnalysisEngineFactory.createEngineDescription(
                StanfordCoreNlpAnnotator.class, typeSystemDescription,
                StanfordCoreNlpAnnotator.PARAM_USE_SUTIME, true,
                StanfordCoreNlpAnnotator.PARAM_SU_TIME_CONF, suTimeConfPath);

        AnalysisEngineDescription fanseEngine = AnalysisEngineFactory.createEngineDescription(
                FanseAnnotator.class, typeSystemDescription,
                FanseAnnotator.PARAM_MODEL_BASE_DIR, fanseResourceDir);

        AnalysisEngineDescription semaforAnnotator = AnalysisEngineFactory.createEngineDescription(
                SemaforAnnotator.class, typeSystemDescription,
                SemaforAnnotator.SEMAFOR_MODEL_PATH, semaforModelDir);

        // annotate language units, this is required before most annotators
        // because it provide the basic units such as sentences
        AnalysisEngineDescription autoLuEngine = AnalysisEngineFactory.createEngineDescription(
                StanfordCorenlpBasedBasicLanguageUnitAnnotator.class, typeSystemDescription,
                StanfordCorenlpBasedBasicLanguageUnitAnnotator.PARAM_ANNOTATION_VIEW_NAMES, viewsToAnnotate);

        AnalysisEngineDescription tokenAlignmentEngine = AnalysisEngineFactory.createEngineDescription(
                TokenAlignmentAnnotator.class, typeSystemDescription);

        AnalysisEngineDescription goldEventMentionEngine = AnalysisEngineFactory.createEngineDescription(
                GoldStandardBasedEventMentionAnnotator.class, typeSystemDescription,
                GoldStandardBasedEventMentionAnnotator.PARAM_GOLD_STANDARD_VIEWNAME, goldStandardViewName);

        AnalysisEngineDescription eventMentionHeadwordEngine = AnalysisEngineFactory.createEngineDescription(
                HeadWordAnnotator.class, typeSystemDescription);

        AnalysisEngineDescription argumentExtractor = AnalysisEngineFactory.createEngineDescription(
                FrameBasedEventArgumentExtractor.class, typeSystemDescription,
                FrameBasedEventArgumentExtractor.PARAM_REDUCE_TO_VERB_NET, true,
                FrameBasedEventArgumentExtractor.PARAM_VN2FN_MAP_PATH, vn2FnPath,
                FrameBasedEventArgumentExtractor.PARAM_VN2PB_MAP_PATH, vn2pbPath,
                FrameBasedEventArgumentExtractor.PARAM_FN_RELATION_PATH, frRelataionPath);

        AnalysisEngineDescription naiveReportingEventTypeAnnotator = AnalysisEngineFactory.createEngineDescription(
                NaiveReportingEventAnnotator.class, typeSystemDescription,
                NaiveReportingEventAnnotator.PARAM_ANNOTATION_VIEW_NAMES, viewsToAnnotate,
                NaiveReportingEventAnnotator.PARAM_FRAME_RELATION_PATH, frRelataionPath);

        AnalysisEngineDescription eventMorphaEngine = AnalysisEngineFactory.createEngineDescription(
                WordMorphaAnnotator.class, typeSystemDescription);

        AnalysisEngineDescription eventSurfaceEngine = AnalysisEngineFactory.createEngineDescription(
                EventSurfaceSimilarityAnnotator.class, typeSystemDescription,
                EventSurfaceSimilarityAnnotator.PARAM_SENNA_EMBEDDINGS, sennaEmbedding,
                EventSurfaceSimilarityAnnotator.PARAM_SENNA_WORDLIST, sennaWordlist,
                EventSurfaceSimilarityAnnotator.PARAM_DO_WORDNET, doWordNet);

        AnalysisEngineDescription eventSyntacticRelationEngine = AnalysisEngineFactory.createEngineDescription(
                EventDirectSyntaticRelationAnnotator.class, typeSystemDescription);

        AnalysisEngineDescription chunkEngine = AnalysisEngineFactory.createEngineDescription(
                ExtendedChunkerAnnotator.class, typeSystemDescription);

        AnalysisEngineDescription entityUnifyEngine = AnalysisEngineFactory.createEngineDescription(
                SudokuEntityUnifier.class, typeSystemDescription);

        // Semantic Role related
        AnalysisEngineDescription roleEngine = AnalysisEngineFactory.createEngineDescription(
                IntegratedAgentPatientAnnotator.class, typeSystemDescription,
                IntegratedAgentPatientAnnotator.PARAM_TARGET_EVENT_TYPE, targetEventType);

        AnalysisEngineDescription locationEngine = AnalysisEngineFactory.createEngineDescription(
                IntegratedLocationAnnotator.class, typeSystemDescription,
                IntegratedLocationAnnotator.PARAM_WORLD_GAZETEER_PATH, wordGazetteer);

        AnalysisEngineDescription xcompEngine = AnalysisEngineFactory.createEngineDescription(
                APLFillerByXcompSharingAnnotator.class, typeSystemDescription);

        AnalysisEngineDescription adjacentEngine = AnalysisEngineFactory.createEngineDescription(
                APLFillerAdjacentEntities.class, typeSystemDescription);

        AnalysisEngineDescription numberEngine = AnalysisEngineFactory.createEngineDescription(
                NumberAnnotator.class, typeSystemDescription,
                NumberAnnotator.NUMBER_NORMALIZER_STRING_RULE, stringRuleFile);

        AnalysisEngineDescription droppedSemanticRoleFiller = AnalysisEngineFactory.createEngineDescription(
                DroppedSemanticRoleFiller.class, typeSystemDescription);

        AnalysisEngineDescription featureContainerEngine = AnalysisEngineFactory.createEngineDescription(
                PairwiseEventFeatureContainerGenerator.class, typeSystemDescription,
                PairwiseEventFeatureContainerGenerator.PARAM_GOLD_STANDARD_VIEWNAME, goldStandardViewName);

        AnalysisEngineDescription featureGenEngine = AnalysisEngineFactory.createEngineDescription(
                FullCoreferencePairwiseFeatureAnnotator.class, typeSystemDescription,
                FullCoreferencePairwiseFeatureAnnotator.PARAM_DO_FILTERING, DoFilteringWhenFeatureGeneration,
                FullCoreferencePairwiseFeatureAnnotator.PARAM_TARGET_COMPONENT_ID, targetComponentId
        );

        AnalysisEngineDescription writer = ReaderWriterFactory
                .createXmiWriter(parentOutputDir, baseOutputDirName, outputStepNumber, outputFileSuffix);

        AnalysisEngineDescription[] defaultEngines = new AnalysisEngineDescription[]{
                goldStandardLuEngine,
                stanfordEngine,
                autoLuEngine,
                fanseEngine,
                semaforAnnotator,
                tokenAlignmentEngine,
                goldEventMentionEngine,
                eventMorphaEngine,
                eventMentionHeadwordEngine,
                argumentExtractor,
                naiveReportingEventTypeAnnotator,
                eventSurfaceEngine,
                eventSyntacticRelationEngine,
                chunkEngine,
                entityUnifyEngine,
                roleEngine,
                locationEngine,
                xcompEngine,
                adjacentEngine,
                numberEngine,
                droppedSemanticRoleFiller,
                featureContainerEngine,
                featureGenEngine,
                writer
        };

        for (AnalysisEngineDescription description : defaultEngines) {
            logger.debug("Using analysis engine : " + description.getAnnotatorImplementationName());
        }

        return defaultEngines;
    }
}
