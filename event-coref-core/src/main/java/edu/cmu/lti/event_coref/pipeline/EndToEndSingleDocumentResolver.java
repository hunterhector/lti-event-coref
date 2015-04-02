package edu.cmu.lti.event_coref.pipeline;

import edu.cmu.lti.event_coref.pipeline.postprocessor.EventCoreferenceResultPostProcessor;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.Resource;

/**
 * Created with IntelliJ IDEA.
 * Date: 3/26/15
 * Time: 5:12 PM
 *
 * @author Zhengzhong Liu
 */
public class EndToEndSingleDocumentResolver {
    EventCorefProcessor processor;
    AnalysisEngine[] engines;

    public void initialize(AnalysisEngineDescription[] preprocessers, String parentOutputDir, String resourceDir) throws UIMAException {
        //"data/processed/IC_domain/IC_domain_65_articles"
        EventCoreferenceResultPostProcessor postprocessor = new EventCoreferenceResultPostProcessor(parentOutputDir, resourceDir);
        AnalysisEngineDescription[] allProcessors = joinProcessors(preprocessers, processor.getDefaultEngineDescriptors(), postprocessor.buildPostprocessors());
        engines = createEngines(allProcessors);
    }


    public void initialize(AnalysisEngineDescription[] preprocessers,
                           AnalysisEngineDescription[] postprocessors) throws UIMAException {
        AnalysisEngineDescription[] allProcessors = joinProcessors(preprocessers, processor.getDefaultEngineDescriptors(), postprocessors);
        engines = createEngines(allProcessors);
    }

    public void process(JCas jcas) throws AnalysisEngineProcessException {
        for (AnalysisEngine engine : engines) {
            engine.process(jcas);
        }
    }

    public void complete() throws AnalysisEngineProcessException {
        collectionProcessComplete(engines);
        destroy(engines);
    }

    /**
     * Notify a set of {@link AnalysisEngine analysis engines} that the collection process is complete.
     */
    private static void collectionProcessComplete(AnalysisEngine... engines)
            throws AnalysisEngineProcessException {
        for (AnalysisEngine e : engines) {
            e.collectionProcessComplete();
        }
    }

    /**
     * Destroy a set of {@link Resource resources}.
     */
    private static void destroy(Resource... resources) {
        for (Resource r : resources) {
            r.destroy();
        }
    }

    public AnalysisEngine[] createEngines(AnalysisEngineDescription... descs)
            throws UIMAException {
        AnalysisEngine[] engines = new AnalysisEngine[descs.length];
        for (int i = 0; i < engines.length; ++i) {
            if (descs[i].isPrimitive()) {
                engines[i] = AnalysisEngineFactory.createEngine(descs[i]);
            } else {
                engines[i] = AnalysisEngineFactory.createEngine(descs[i]);
            }
        }
        return engines;
    }

    private AnalysisEngineDescription[] joinProcessors(AnalysisEngineDescription[]... allAEs) {
        int size = 0;
        for (AnalysisEngineDescription[] ae : allAEs) {
            size += ae.length;
        }

        AnalysisEngineDescription[] joinedAEs = new AnalysisEngineDescription[size];

        int index = 0;
        for (int i = 0; i < allAEs.length; i++) {
            AnalysisEngineDescription[] aes = allAEs[i];
            for (int j = 0; j < aes.length; j++) {
                joinedAEs[index] = aes[j];
                index++;
            }
        }
        return joinedAEs;
    }
}