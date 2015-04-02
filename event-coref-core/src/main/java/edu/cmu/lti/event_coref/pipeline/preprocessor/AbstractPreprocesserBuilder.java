package edu.cmu.lti.event_coref.pipeline.preprocessor;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Created with IntelliJ IDEA.
 * Date: 3/25/15
 * Time: 8:08 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class AbstractPreprocesserBuilder {
//    TypeSystemDescription typeSystemDescription;
//
//    public AnalysisEngineDescription[] buildPreprocessers(TypeSystemDescription typeSystemDescription) throws ResourceInitializationException {
//        this.typeSystemDescription = typeSystemDescription;
//        return buildPreprocessers();
//    }

    public abstract CollectionReaderDescription buildCollectionReader() throws ResourceInitializationException;

    public abstract AnalysisEngineDescription[] buildPreprocessers() throws ResourceInitializationException;
}