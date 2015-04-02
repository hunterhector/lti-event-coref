package edu.cmu.lti.event_coref.pipeline.postprocessor;

import edu.cmu.lti.event_coref.DefaultConfigs;
import edu.cmu.lti.event_coref.analysis_engine.eval.ConllFormatWriter;
import edu.cmu.lti.event_coref.analysis_engine.resoluter.FullCoreferenceClusterTransitivityCreator;
import edu.cmu.lti.event_coref.analysis_engine.resoluter.SudokuUnificationAnnotator;
import edu.cmu.lti.event_coref.analysis_engine.resoluter.WekaArffBasedClassifier;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 3/26/15
 * Time: 11:06 PM
 *
 * @author Zhengzhong Liu
 */
public class EventCoreferenceResultPostProcessor extends AbstractPostprocesserBuilder {
    final String goldStandardViewName = DefaultConfigs.goldStandardViewName;

    int maxIter = 2;
    TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription(DefaultConfigs.TypeSystemDescriptorName);

    int featurelizedStep = 2;
    int paramOutputStepNumber = featurelizedStep + 1;

    final String paramParentOutputDir;
    // Parameters about generated features
    final String featureNameFile;
    final String paramModelName;

    final Boolean doUnification = true;
    final Boolean needToUpdateFeature = true;
    final Float unificationConfidenceThreshold = (float) 0.5;
    final Integer unificationVerboseLevel = 1;
    final Integer clusterMethod = 0;

    //conll writer
    final String paramConllEvalFileDir = "conll_format";
    final String conllOutputFilePrefix = "txt";
    final String paramGoldViewName = DefaultConfigs.goldStandardViewName;
    final Boolean paramInludeSingleton = true;

    public EventCoreferenceResultPostProcessor(String parentOutputDir, String resourceDir) {
        this.paramParentOutputDir = parentOutputDir;
        this.paramModelName = new File(resourceDir, DefaultConfigs.modelName).getAbsolutePath();
        this.featureNameFile = new File(resourceDir, DefaultConfigs.featureListName).getAbsolutePath();
    }

    @Override
    public AnalysisEngineDescription[] buildPostprocessors() throws ResourceInitializationException {
        List<AnalysisEngineDescription> processors = new ArrayList<>();

        AnalysisEngineDescription resoluterEngine = AnalysisEngineFactory
                .createEngineDescription(
                        WekaArffBasedClassifier.class, typeSystemDescription,
                        WekaArffBasedClassifier.PARAM_FEAUTURE_NAME_PATH, featureNameFile,
                        WekaArffBasedClassifier.PARAM_PRE_SAVED_MODEL_FILE_NAME, paramModelName
                );

        AnalysisEngineDescription uniEngine = AnalysisEngineFactory.createEngineDescription(
                SudokuUnificationAnnotator.class, typeSystemDescription,
                SudokuUnificationAnnotator.PARAM_UPDATE_FEATURES, needToUpdateFeature,
                SudokuUnificationAnnotator.PARAM_UNIFICATION_CONFIDENCE_THRESHOLD, unificationConfidenceThreshold,
                SudokuUnificationAnnotator.PARAM_DO_UNIFICATION, doUnification,
                SudokuUnificationAnnotator.PARAM_VERBOSE_LEVEL, unificationVerboseLevel,
                SudokuUnificationAnnotator.PARAM_CLUSTER_METHOD, clusterMethod
        );

        AnalysisEngineDescription clusterEngine = AnalysisEngineFactory.createEngineDescription(
                FullCoreferenceClusterTransitivityCreator.class, typeSystemDescription);

        AnalysisEngineDescription conllWriter = AnalysisEngineFactory
                .createEngineDescription(ConllFormatWriter.class, typeSystemDescription,
                        ConllFormatWriter.PARAM_PARENT_OUTPUT_PATH, paramParentOutputDir,
                        ConllFormatWriter.PARAM_OUTPUT_STEP_NUMBER, paramOutputStepNumber,
                        ConllFormatWriter.PARAM_BASE_OUTPUT_DIR_NAME, paramConllEvalFileDir,
                        ConllFormatWriter.PARAM_OUTPUT_FILE_SUFFIX, conllOutputFilePrefix,
                        ConllFormatWriter.PARAM_INCLUDE_SINGLETON, paramInludeSingleton,
                        ConllFormatWriter.PARAM_GOLD_STANDARD_VIEWNAME, goldStandardViewName,
                        ConllFormatWriter.PARAM_GOLD_COMPONENT_ID_PREFIX, paramGoldViewName,
                        ConllFormatWriter.PARAM_SYSTEM_COMPONENT_ID_PREFIX, DefaultConfigs.systemComponentPrefix
                );

        for (int iter = 0; iter < maxIter; iter++) {
            processors.add(resoluterEngine);
            processors.add(uniEngine);
        }

        processors.add(clusterEngine);
        processors.add(conllWriter);

        return processors.toArray(new AnalysisEngineDescription[processors.size()]);
    }
}
