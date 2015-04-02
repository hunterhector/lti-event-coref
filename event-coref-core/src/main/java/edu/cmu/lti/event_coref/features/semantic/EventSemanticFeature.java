/**
 *
 */
package edu.cmu.lti.event_coref.features.semantic;

import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import edu.cmu.lti.event_coref.utils.QuantityUtils;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhengzhong Liu, Hector
 */
public class EventSemanticFeature extends PairwiseFeatureGenerator {
    private static final Logger logger = LoggerFactory.getLogger(EventSemanticFeature.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * edu.cmu.lti.event_coref.ml.feature.PairwiseFeatureGenerator#createFeatures(org.apache.uima.
     * jcas.JCas, edu.cmu.lti.event_coref.type.EventMention,
     * edu.cmu.lti.event_coref.type.EventMention)
     */
    @Override
    public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
                                                     EventMention event2) {
        List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();

        // event quantity features
        Double numberMatchResult = null;
        try {
            numberMatchResult = QuantityUtils.numberCompare(event1.getQuantity(),
                    event2.getQuantity());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        if (numberMatchResult != null) {
            PairwiseEventFeature sameQuantityFeature = FeatureUtils.createPairwiseEventBinaryFeature(
                    aJCas, "QuantityFeature_eventMentionSameQuantity", numberMatchResult == 0.0, false);

            PairwiseEventFeature firstSmallerFeature = FeatureUtils.createPairwiseEventBinaryFeature(
                    aJCas, "QuantityFeature_firstEventMentionSmaller", numberMatchResult < 0, false);

            PairwiseEventFeature secondSmallerFeature = FeatureUtils.createPairwiseEventBinaryFeature(
                    aJCas, "QuantityFeature_secondEventMentionSmaller", numberMatchResult > 0, false);

            features.add(sameQuantityFeature);
            features.add(firstSmallerFeature);
            features.add(secondSmallerFeature);
        }

//        // event mention type match features
//        List<EntityMention> sireEntities1 = JCasUtil.selectCovered(EntityMention.class, event1);
//        List<EntityMention> sireEntities2 = JCasUtil.selectCovered(EntityMention.class, event2);
//
//        for (EntityMention sireEntity1 : sireEntities1) {
//            for (EntityMention sireEntity2 : sireEntities2) {
//                if (sireEntity1.getMentionType().equals(sireEntity2.getMentionType())) {
//                    FeatureUtils.createPairwiseEventNumericFeature(aJCas, "EventMentionTypeMatch",
//                            sireEntity1.getMentionTypeConf() * sireEntity2.getMentionTypeConf(), false);
//                }
//            }
//        }

        return features;
    }

}
