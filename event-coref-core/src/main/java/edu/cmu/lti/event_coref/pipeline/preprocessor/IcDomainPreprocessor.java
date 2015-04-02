package edu.cmu.lti.event_coref.pipeline.preprocessor;

import edu.cmu.lti.event_coref.DefaultConfigs;
import edu.cmu.lti.event_coref.corpus.ic.IcDomainDataDetagger;
import edu.cmu.lti.event_coref.corpus.ic.IcDomainDataToXmiConverter;
import edu.cmu.lti.event_coref.io.ReaderWriterFactory;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

/**
 * Created with IntelliJ IDEA.
 * Date: 3/25/15
 * Time: 8:07 PM
 *
 * @author Zhengzhong Liu
 */
public class IcDomainPreprocessor extends AbstractPreprocesserBuilder {
    private static final String encoding = "UTF-8";
    private static final String[] textSuffix = new String[]{".txt"};
    private static final String icDomainGoldStandardFileExtension = ".blk.tok.stp.tbf.xml.pub";

    private String corpusDir;

    public IcDomainPreprocessor(String corpusDir) {
        this.corpusDir = corpusDir;
    }

    @Override
    public CollectionReaderDescription buildCollectionReader() throws ResourceInitializationException {
        return ReaderWriterFactory.createPlainTextReader(DefaultConfigs.inputViewName,
                DefaultConfigs.srcDocInfoViewNames, corpusDir, encoding, textSuffix);
    }

    @Override
    public AnalysisEngineDescription[] buildPreprocessers() throws ResourceInitializationException {
        AnalysisEngineDescription[] preprocessors;

        TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory.createTypeSystemDescription(DefaultConfigs.TypeSystemDescriptorName);

        AnalysisEngineDescription converter = AnalysisEngineFactory.createEngineDescription(
                IcDomainDataToXmiConverter.class, typeSystemDescription,
                IcDomainDataToXmiConverter.PARAM_GOLD_STANDARD_INPUT_DIR, corpusDir,
                IcDomainDataToXmiConverter.PARAM_GOLD_STANDARD_FILE_EXTENSION, icDomainGoldStandardFileExtension,
                IcDomainDataToXmiConverter.PARAM_GOLD_STANDARD_VIEW_NAME, DefaultConfigs.goldStandardViewName
        );


        AnalysisEngineDescription detagger = AnalysisEngineFactory.createEngineDescription(
                IcDomainDataDetagger.class, typeSystemDescription,
                IcDomainDataDetagger.PARAM_INPUT_VIEW_NAME, DefaultConfigs.inputViewName,
                IcDomainDataDetagger.PARAM_GOLD_STANDARD_VIEW_NAME, DefaultConfigs.goldStandardViewName
        );

        preprocessors = new AnalysisEngineDescription[]{detagger, converter};
        return preprocessors;
    }
}