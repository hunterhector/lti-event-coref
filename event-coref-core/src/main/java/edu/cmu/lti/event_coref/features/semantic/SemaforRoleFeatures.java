/**
 * 
 */
package edu.cmu.lti.event_coref.features.semantic;

import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.type.EntityBasedComponent;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.EventMentionArgumentLink;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import edu.cmu.lti.event_coref.utils.SimilarityCalculator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.uimafit.util.FSCollectionFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
/**
 * @author Zhengzhong Liu, Hector
 * 
 */
public class SemaforRoleFeatures extends PairwiseFeatureGenerator {
  SimilarityCalculator calc;

  public SemaforRoleFeatures(SimilarityCalculator calc) {
    this.calc = calc;
  }

  @Override
  public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
          EventMention event2) {
    List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();

    Map<String, EntityBasedComponent> evm1Arguments = getArguments(event1);
    Map<String, EntityBasedComponent> evm2Arguments = getArguments(event2);

    for (Entry<String, EntityBasedComponent> evm1Entry : evm1Arguments.entrySet()) {
      String role = evm1Entry.getKey();
      if (evm2Arguments.containsKey(role)) { // compare the same role
        EntityBasedComponent argEvm1 = evm1Entry.getValue();
        EntityBasedComponent argEvm2 = evm2Arguments.get(role);

        String str1 = argEvm1.getCoveredText();
        String str2 = argEvm2.getCoveredText();

        // PairwiseEventFeature surfaceMatch = null;
        // if (str1.equals(str2)) {
        // surfaceMatch = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
        // "semafor_surfaceMatch_" + role, true, false);
        // } else {
        // surfaceMatch = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
        // "semafor_surfaceMatch_" + role, false, false);
        // }

        PairwiseEventFeature dice = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                "semafor_surfaceDice_" + role, calc.getDiceCoefficient(str1, str2), false);

        double entityScore = calc.checkClusterSurfaceSimilarity(argEvm1, argEvm2);

        PairwiseEventFeature entitySim = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                "semafor_entityClusterSimilarity_" + role, entityScore, false);

        PairwiseEventFeature subStringFeature = FeatureUtils.createPairwiseEventBinaryFeature(
                aJCas, "semafor_subString_" + role, calc.subStringTest(str1, str2), false);

        features.add(subStringFeature);
        features.add(entitySim);
        features.add(dice);
        // features.add(surfaceMatch);
      }
    }

    return features;
  }

  private Map<String, EntityBasedComponent> getArguments(EventMention evm) {
    Map<String, EntityBasedComponent> evmArguments = new HashMap<String, EntityBasedComponent>();

    FSList argumentsFs = evm.getArguments();

    if (argumentsFs == null) {
      return evmArguments;
    }

    for (EventMentionArgumentLink link : FSCollectionFactory.create(argumentsFs,
            EventMentionArgumentLink.class)) {
      EntityBasedComponent entityMention = link.getArgument();

      // to reduce the feature only to those containing verb net type arguments
      String roleName = link.getVerbNetRoleName();
      if (roleName != null) {
        evmArguments.put(roleName, entityMention);
      }
    }

    return evmArguments;
  }

}
