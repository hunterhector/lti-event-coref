package edu.cmu.lti.event_coref.pipeline.component;

import edu.cmu.lti.event_coref.corpus.ic.IcDomainDataDetagger;
import edu.cmu.lti.event_coref.corpus.ic.IcDomainDataToXmiConverter;
import edu.cmu.lti.event_coref.util.EventCorefAnalysisEngineFactory;
import edu.cmu.lti.util.general.ErrorUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/23/15
 * Time: 5:11 PM
 */
public class IcDomainReader {
    private static String className = IcDomainReader.class.getSimpleName();

    private static final Logger logger = LoggerFactory.getLogger(IcDomainReader.class);

    public static void main(String[] args) throws UIMAException {
        logger.info(className + " started...");

        // Parameters for the reader
        String corpusDir = "data/corpus/IC_domain/IC_domain_65_articles/gold_standard";
        int paramInputStepNumber = 0;
        String[] paramSrcDocInfoViewNames = new String[]{"GoldStandard"};
        String paramEncoding = null;
        String[] paramTextSuffix = new String[]{".txt"};


        // Parameters for the analyzer
        String paramTypeSystemDescriptor = "EventCoreferenceAllTypeSystem";
        String paramGoldStandardFileExtension = ".blk.tok.stp.tbf.xml.pub";
        String paramGoldStandardViewName = "GoldStandard";
        String paramInputViewName = "Input";

        // Parameters for the writer
        String paramParentOutputDir = "data/processed/IC_domain/IC_domain_65_articles";
        String paramBaseOutputDirName = "xmi_after_converting_gold_standard_to_xmi";
        String paramOutputFileSuffix = null;
        int paramOutputStepNumber = paramInputStepNumber + 1;
        // ///////////////////////////////////////////////////////////////

        // Instantiate a collection reader to get XMI as input.
        CollectionReaderDescription reader = EventCorefAnalysisEngineFactory.createPlainTextReader(
                paramInputViewName, paramSrcDocInfoViewNames, corpusDir, paramEncoding, paramTextSuffix);

        // Instantiate the analysis engine.
        TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory
                .createTypeSystemDescription(paramTypeSystemDescriptor);

        AnalysisEngineDescription analyzer = EventCorefAnalysisEngineFactory.createAnalysisEngine(
                IcDomainDataToXmiConverter.class, typeSystemDescription,
                IcDomainDataToXmiConverter.PARAM_GOLD_STANDARD_INPUT_DIR, corpusDir,
                IcDomainDataToXmiConverter.PARAM_GOLD_STANDARD_FILE_EXTENSION, paramGoldStandardFileExtension,
                IcDomainDataToXmiConverter.PARAM_GOLD_STANDARD_VIEW_NAME, paramGoldStandardViewName);

        AnalysisEngineDescription detagger = EventCorefAnalysisEngineFactory.createAnalysisEngine(
                IcDomainDataDetagger.class, typeSystemDescription,
                IcDomainDataDetagger.PARAM_INPUT_VIEW_NAME, paramInputViewName,
                IcDomainDataDetagger.PARAM_GOLD_STANDARD_VIEW_NAME, paramGoldStandardViewName);

        // Instantiate a XMI writer to put XMI as output.
        AnalysisEngineDescription writer = EventCorefAnalysisEngineFactory.createXmiWriter(
                paramParentOutputDir, paramBaseOutputDirName, paramOutputStepNumber,
                paramOutputFileSuffix);

        // Run the pipeline.
        try {
            SimplePipeline.runPipeline(reader, analyzer, detagger, writer);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorUtils.terminate(className + " stopped due to an error.");
        }

        logger.info(className + " successfully completed.");
    }
}