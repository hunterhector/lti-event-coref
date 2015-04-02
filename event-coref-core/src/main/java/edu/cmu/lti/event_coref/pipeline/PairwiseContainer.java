package edu.cmu.lti.event_coref.pipeline;

import edu.cmu.lti.event_coref.analysis_engine.features.PairwiseEventFeatureContainerGenerator;
import edu.cmu.lti.event_coref.io.ReaderWriterFactory;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Date: 4/1/15
 * Time: 2:15 PM
 *
 * @author Zhengzhong Liu
 */
public class PairwiseContainer {
    private static final String TypeSystemDescriptorName = "EventCoreferenceAllTypeSystem";
    private static TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory
            .createTypeSystemDescription(TypeSystemDescriptorName);

    public static void main(String[] argv) throws UIMAException, IOException {
        String paramParentInputDir = "data/processed/IC_domain/IC_domain_65_articles";
        String paramBaseInitialInputDirName = "xmi_processed";

        CollectionReaderDescription reader = ReaderWriterFactory.createXmiReader(typeSystemDescription,
                paramParentInputDir, paramBaseInitialInputDirName, 1, false);

        AnalysisEngineDescription featureGenEngine = AnalysisEngineFactory.createEngineDescription(
                PairwiseEventFeatureContainerGenerator.class, typeSystemDescription,
                PairwiseEventFeatureContainerGenerator.PARAM_GOLD_STANDARD_VIEWNAME, "GoldStandard"
        );

        AnalysisEngineDescription writer = ReaderWriterFactory
                .createXmiWriter(paramParentInputDir, "feature_generated", 2, null);

        SimplePipeline.runPipeline(reader, featureGenEngine, writer);
    }
}
