package edu.cmu.lti.event_coref.pipeline;

import edu.cmu.lti.event_coref.pipeline.postprocessor.EventCoreferenceResultPostProcessor;
import edu.cmu.lti.event_coref.pipeline.preprocessor.IcDomainPreprocessor;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Date: 3/22/15
 * Time: 9:18 PM
 *
 * @author Zhengzhong Liu
 */
public class EndToEndCollectionResolver {
    public void resolveCollection(CollectionReaderDescription reader,
                                  AnalysisEngineDescription[] preprocessers,
                                  String processDir, String resourceDir) throws UIMAException, IOException {
        EventCorefProcessor processor = new EventCorefProcessor(processDir, resourceDir);
        EventCoreferenceResultPostProcessor postprocessor = new EventCoreferenceResultPostProcessor(processDir, resourceDir);
        AnalysisEngineDescription[] allProcessors = joinProcessors(preprocessers, processor.getDefaultEngineDescriptors(), postprocessor.buildPostprocessors());
        SimplePipeline.runPipeline(reader, allProcessors);
    }

    private AnalysisEngineDescription[] joinProcessors(AnalysisEngineDescription[]... allAEs) {
        int size = 0;
        for (AnalysisEngineDescription[] ae : allAEs) {
            size += ae.length;
        }

        AnalysisEngineDescription[] joinedAEs = new AnalysisEngineDescription[size];

        int index = 0;
        for (AnalysisEngineDescription[] aes : allAEs) {
            for (AnalysisEngineDescription ae : aes) {
                joinedAEs[index] = ae;
                index++;
            }
        }
        return joinedAEs;
    }

    public static void main(String[] args) throws IOException {
        final String corpusDir = "data/corpus/IC_domain/IC_domain_65_articles/gold_standard";
        final String processDir = "data/processed/IC_domain/IC_domain_65_articles";
        final String resourceDir = "data/resources";

        IcDomainPreprocessor preprocessor = new IcDomainPreprocessor(corpusDir);
        EndToEndCollectionResolver resolver = new EndToEndCollectionResolver();
        try {
            resolver.resolveCollection(preprocessor.buildCollectionReader(), preprocessor.buildPreprocessers(), processDir, resourceDir);
        } catch (UIMAException e) {
            e.printStackTrace();
        }
    }
}