package edu.cmu.lti.event_coref.features.semantic;

import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.utils.ml.FeatureConstants;
import edu.cmu.lti.event_coref.utils.ml.FeatureConstants.PairwiseEventFeatureInfo;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import edu.cmu.lti.utils.general.EnglishUtils;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * This analysis engine extracts temporal features from event pairs.
 * 
 * Prerequisite annotations: EntityMention.
 * 
 * @author Jun Araki
 */
public class TemporalFeatures extends PairwiseFeatureGenerator {

  @Override
  public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
          EventMention event2) {
    List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();

    addFeaturePresentOrPastVerbMatch(features, aJCas, event1, event2);
    // TODO: implement the following
    // addFeaturePosOfTwoPrecedingEvents
    // addFeaturePosOfOnePrecedingEvent
    // addFeatureBothHaveWill

    return features;
  }

  /**
   * Adds the feature 'TenseMatch'.
   * TODO This sounds like syntactic feature and it is considered duplicated with WordFormFeatures
   * (-Hector)
   * 
   * @param features
   * @param aJCas
   * @param event1
   * @param event2
   */
  private void addFeaturePresentOrPastVerbMatch(List<PairwiseEventFeature> features, JCas aJCas,
          EventMention event1, EventMention event2) {
    String pos1 = event1.getHeadWord().getPartOfSpeech();
    String pos2 = event2.getHeadWord().getPartOfSpeech();

    double score = FeatureConstants.NEGATIVE_BINARY_FEATURE_VALUE;
    if (EnglishUtils.isVerb(pos1) && EnglishUtils.isVerb(pos2)) {
      if (EnglishUtils.isPresentVerb(pos1) && EnglishUtils.isPresentVerb(pos2)) {
        score = FeatureConstants.POSITIVE_BINARY_FEATURE_VALUE;
      }
      if (EnglishUtils.isPastVerb(pos1) && EnglishUtils.isPastVerb(pos2)) {
        score = FeatureConstants.POSITIVE_BINARY_FEATURE_VALUE;
      }
    }

    features.add(FeatureUtils.createPairwiseEventFeature(aJCas,
            PairwiseEventFeatureInfo.PRESENT_OR_PAST_VERB_MATCH, score, false));
  }

}
