package edu.cmu.lti.event_coref.features;

import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import org.apache.uima.jcas.JCas;

import java.util.List;

public abstract class PairwiseFeatureGenerator {
  /**
   * This method take the JCas and a pair of events, it will generate a list of features for this.
   * It is expected that different feature generator implementation just override this method to
   * give a consistent interface
   * 
   * @param aJCas
   * @param event1
   * @param event2
   * @return
   */
  public abstract List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
          EventMention event2);
}
