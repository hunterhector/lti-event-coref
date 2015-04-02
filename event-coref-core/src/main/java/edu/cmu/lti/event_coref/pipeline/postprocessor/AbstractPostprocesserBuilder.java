package edu.cmu.lti.event_coref.pipeline.postprocessor;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Created with IntelliJ IDEA.
 * Date: 3/25/15
 * Time: 10:10 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class AbstractPostprocesserBuilder {
    public abstract AnalysisEngineDescription[] buildPostprocessors() throws ResourceInitializationException;
}
