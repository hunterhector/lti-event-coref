package deft.cmucs.hovy.eventcoref;

import adept.common.Document;
import adept.common.HltContentContainer;
import adept.module.AdeptModuleException;
import adept.module.EventCoreferenceResolver;
import adept.module.ModuleConfig;
import deft.cmucs.hovy.eventcoref.uima.PlainTextSingleDocUimaInterface;
import edu.cmu.lti.event_coref.pipeline.EndToEndSingleDocumentResolver;
import edu.cmu.lti.event_coref.pipeline.postprocessor.EventCoreferenceResultPostProcessor;
import edu.cmu.lti.event_coref.pipeline.preprocessor.IcDomainPreprocessor;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import java.io.File;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Zhengzhong Liu
 *         Date: 3/25/15
 *         Time: 5:14 PM
 */
public class HovyEventCoreferenceResolver extends EventCoreferenceResolver {
    PlainTextSingleDocUimaInterface document2Uima;
    EndToEndSingleDocumentResolver resolver;

    @Override
    public void activate(String configFilePath)
            throws InvalidPropertiesFormatException, IOException,
            AdeptModuleException {
        super.activate(configFilePath);
        final ModuleConfig config = getModuleConfig();

        final String artifactDirPath = new File(EventCoreferenceRunner.class.getClassLoader().
                getResource("ext/edu/cmucs/hovy/eventcoref").getFile()).getAbsolutePath();
        final String fanseDir = config.getProperty("fanseModelBase");

        final String corpusDir = "data/corpus/IC_domain/IC_domain_65_articles/gold_standard";

        //TODO config the parameter latter.
        document2Uima = new PlainTextSingleDocUimaInterface("ascii");
        IcDomainPreprocessor preprocessor = new IcDomainPreprocessor(corpusDir);
        EventCoreferenceResultPostProcessor postprocessor = new EventCoreferenceResultPostProcessor();
        resolver = new EndToEndSingleDocumentResolver();
        try {
            resolver.initialize(preprocessor.buildPreprocessers(), postprocessor.buildPostprocessors());
        } catch (UIMAException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deactivate() throws AdeptModuleException {
        //release any resources occupied
        try {
            resolver.complete();
        } catch (AnalysisEngineProcessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HltContentContainer process(Document document,
                                       HltContentContainer hltContentContainerIn) {
        // Process p;
        String docName = document.getUri();
        System.out.println("Processing input file " + docName);

        try {
            resolver.process(document2Uima.getJCas(document));
        } catch (UIMAException e) {
            e.printStackTrace();
        }

        //TODO convert results to HltContentContainer
        System.out.println("Finished processing file. Setting event coreference in HltContentContainer...");
        HltContentContainer hltContentContainerOut = new HltContentContainer();


        return hltContentContainerOut;
    }


}
