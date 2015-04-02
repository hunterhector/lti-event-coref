/**
 * 
 */
package edu.cmu.lti.event_coref.features.syntactic;

import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.type.StanfordCorenlpToken;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhengzhong Liu, Hector
 * 
 *         The correct way to do part of speech is to put each pair of POS as a feature, here the
 *         implementation is unordered
 */
public class PosPairFeatures extends PairwiseFeatureGenerator {

  @Override
  public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
          EventMention event2) {
    List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();

    StanfordCorenlpToken event1HeadWord = JCasUtil.selectCovered(StanfordCorenlpToken.class,
            event1.getHeadWord()).get(0);
    StanfordCorenlpToken event2HeadWord = JCasUtil.selectCovered(StanfordCorenlpToken.class,
            event2.getHeadWord()).get(0);

    String pos1 = event1HeadWord.getPos();
    String pos2 = event2HeadWord.getPos();

    if (pos1 != null && pos2 != null) {
      String smallerPos = null;
      String largerPos = null;

      if (pos1.compareTo(pos2) > 0) {
        largerPos = pos1;
        smallerPos = pos2;
      } else {
        largerPos = pos2;
        smallerPos = pos1;
      }

      FeatureUtils.createPairwiseEventBinaryFeature(aJCas, "pos_" + smallerPos + "_" + largerPos,
              true, true);
    }

    return features;
  }
}
