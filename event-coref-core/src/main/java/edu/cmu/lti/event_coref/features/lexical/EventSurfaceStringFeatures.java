package edu.cmu.lti.event_coref.features.lexical;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.EventSurfaceSimilarity;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Annotate a few event surface features to capture the surface similarity between any two events
 *
 * @author Zhengzhong Liu, Hector
 */
public class EventSurfaceStringFeatures extends PairwiseFeatureGenerator {
    Collection<EventSurfaceSimilarity> eventSurfaceSimilarities;

    Table<EventMention, EventMention, EventSurfaceSimilarity> surfaceSimilarityTable;

    public EventSurfaceStringFeatures(JCas aJCas) {
        eventSurfaceSimilarities = JCasUtil.select(aJCas, EventSurfaceSimilarity.class);
        surfaceSimilarityTable = HashBasedTable.create();

        for (EventSurfaceSimilarity ess : eventSurfaceSimilarities) {
            EventMention eevm1 = ess.getEventMentionI();
            EventMention eevm2 = ess.getEventMentionJ();
            surfaceSimilarityTable.put(eevm1, eevm2, ess);
        }
    }

    @Override
    public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
                                                     EventMention event2) {
        List<PairwiseEventFeature> pairFeatures = new ArrayList<PairwiseEventFeature>();

        if (surfaceSimilarityTable.contains(event1, event2)) {
            EventSurfaceSimilarity surfaceSim = surfaceSimilarityTable.get(event1, event2);

            double sennaSim = surfaceSim.getSennaSimilarity();
            double diceSim = surfaceSim.getDiceCoefficient();
            double wordNetSim = adjustWordNetScore(surfaceSim.getWordNetWuPalmer());
            double wordNetMorphaSim = adjustWordNetScore(surfaceSim.getMorphalizedWuPalmer());

            addFeature(aJCas, wordNetSim, "eventSurfaceWordNetSimilarity", pairFeatures);
            addFeature(aJCas, wordNetMorphaSim, "eventSurfaceMorphaWordNetSimilarity", pairFeatures);
            addFeature(aJCas, sennaSim, "eventSurfaceSennaSimilarity", pairFeatures);
            addFeature(aJCas, diceSim, "eventSurfaceDice", pairFeatures);
        }

        return pairFeatures;
    }

    private void addFeature(JCas aJCas, double score, String featureName, List<PairwiseEventFeature> pairFeatures) {
        if (score >= 0) {
            PairwiseEventFeature wordNetFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                    featureName, score, false);
            pairFeatures.add(wordNetFeature);
        }
    }

    private double adjustWordNetScore(double wordNetSim) {
        return wordNetSim > 1.0 ? 1.0 : wordNetSim;
    }

}
