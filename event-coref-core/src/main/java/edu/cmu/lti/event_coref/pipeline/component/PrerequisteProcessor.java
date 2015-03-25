package edu.cmu.lti.event_coref.pipeline.component;

import edu.cmu.lti.event_coref.analysis_engine.FanseAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.SemaforAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.StanfordCoreNlpAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.mention.GoldStandardBasedEventMentionAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.mention.TrivialEventTypeAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.prerequisite.GoldStandardBasedBasicLanguageUnitAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.prerequisite.HeadWordAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.prerequisite.StanfordCorenlpBasedBasicLanguageUnitAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.prerequisite.TokenAlignmentAnnotator;
import edu.cmu.lti.event_coref.util.EventCorefAnalysisEngineFactory;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/24/15
 * Time: 2:06 PM
 */
public class PrerequisteProcessor {
    public static void main(String[] args) throws UIMAException, IOException {
        String inputTextDir = "../edu.cmu.lti.event_coref.system/data/corpus/simpson_sample";

        String processedDataDir = "data/processed/IC_domain/IC_domain_65_articles";

        String paramTypeSystemDescriptor = "EventCoreferenceAllTypeSystems";
        String goldStandardViewName = "GoldStandard";
        List<String> viewsToAnnotate = new ArrayList<String>();
        viewsToAnnotate.add(CAS.NAME_DEFAULT_SOFA);

        Boolean paramFailUnknown = false;

        // All use the same type system
        TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory
                .createTypeSystemDescription(paramTypeSystemDescriptor);

        String paramEventFrameNamePath = "../edu.cmu.lti.event_coref.system/data/corpus/FrameNet/event_frames.txt";
        String paramVn2FnPath = "../edu.cmu.lti.event_coref.ann.IntegratedSemanticRoleAnnotator/resources/SemLink_1.2.2c/vn-fn/VN-FNRoleMapping.txt";
        String targetEventType = "event";

        String fanseResourceDir = "resources/fanse";
        String paramModelBaseDirectory = "resources/semafor";

        AnalysisEngineDescription goldStandardLuEngine = EventCorefAnalysisEngineFactory.createAnalysisEngine(
                GoldStandardBasedBasicLanguageUnitAnnotator.class, typeSystemDescription,
                GoldStandardBasedBasicLanguageUnitAnnotator.PARAM_GOLD_STANDARD_VIEWNAME,
                goldStandardViewName);

        AnalysisEngineDescription stanfordEngine = EventCorefAnalysisEngineFactory
                .createAnalysisEngine(StanfordCoreNlpAnnotator.class,
                        typeSystemDescription,
                        StanfordCoreNlpAnnotator.PARAM_USE_SUTIME, true);

        AnalysisEngineDescription fanseEngine = EventCorefAnalysisEngineFactory
                .createAnalysisEngine(FanseAnnotator.class,
                        typeSystemDescription,
                        FanseAnnotator.PARAM_MODEL_BASE_DIR,
                        fanseResourceDir);

        AnalysisEngineDescription analsemaforAnnotator = EventCorefAnalysisEngineFactory
                .createAnalysisEngine(SemaforAnnotator.class,
                        typeSystemDescription,
                        SemaforAnnotator.SEMAFOR_MODEL_PATH,
                        paramModelBaseDirectory);

        // annotate language units, this is required before most annotators
        // because it provide the basic units such as sentences
        AnalysisEngineDescription autoLuEngine = EventCorefAnalysisEngineFactory
                .createAnalysisEngine(
                        StanfordCorenlpBasedBasicLanguageUnitAnnotator.class,
                        typeSystemDescription,
                        StanfordCorenlpBasedBasicLanguageUnitAnnotator.PARAM_ANNOTATION_VIEWNAMES,
                        viewsToAnnotate);

        AnalysisEngineDescription tokenAlignmentEngine = EventCorefAnalysisEngineFactory
                .createAnalysisEngine(TokenAlignmentAnnotator.class, typeSystemDescription);

        AnalysisEngineDescription eventMentionEngine = EventCorefAnalysisEngineFactory
                .createAnalysisEngine(GoldStandardBasedEventMentionAnnotator.class,
                        typeSystemDescription,
                        GoldStandardBasedEventMentionAnnotator.PARAM_GOLD_STANDARD_VIEWNAME,
                        goldStandardViewName,
                        GoldStandardBasedEventMentionAnnotator.PARAM_USE_DEFAULT_EVENT_TYPE, true);

        AnalysisEngineDescription eventMentionHeadwordEngine = EventCorefAnalysisEngineFactory
                .createAnalysisEngine(HeadWordAnnotator.class,
                        typeSystemDescription);

        AnalysisEngineDescription trivialEventTypeAnnotator = EventCorefAnalysisEngineFactory
                .createAnalysisEngine(TrivialEventTypeAnnotator.class,
                        typeSystemDescription,
                        TrivialEventTypeAnnotator.PARAM_GIVEN_TYPE_NAME,
                        targetEventType);


    }
}
