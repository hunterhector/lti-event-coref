package edu.cmu.lti.event_coref.features.semantic;

import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.type.StanfordCorenlpToken;
import edu.cmu.lti.event_coref.utils.WordNetSimilarityCalculator;
import edu.cmu.lti.event_coref.utils.ml.FeatureConstants.PairwiseEventFeatureInfo;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import edu.cmu.lti.utils.general.ListUtils;
import edu.cmu.lti.utils.general.StringUtils;
import org.apache.uima.jcas.JCas;
import org.javatuples.Pair;
import org.uimafit.util.JCasUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This analysis engine adds WordNet similarity features.
 * <p/>
 * Prerequisite annotations: Token.
 *
 * @author Jun Araki
 */
public class WordNetSimilarityFeatures extends PairwiseFeatureGenerator {

    private final String separator = "#";

    private WordNetSimilarityCalculator wnsc;

    private Map<Pair<String, String>, Map<String, Double>> cache;

    // Upper bound of the similarity score, which was manually determined
    private final static double MAX_SIMILARITY_SCORE = 10.0;

    /**
     * Constructor.
     */
    public WordNetSimilarityFeatures() {
        wnsc = new WordNetSimilarityCalculator();
        cache = new HashMap<Pair<String, String>, Map<String, Double>>();
    }

    @Override
    public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
                                                     EventMention event2) {
        List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();

        addFeatureWordNetSimilarity(features, aJCas, event1, event2);

        return features;
    }

    /**
     * Adds the feature 'WordNet similarity'.
     *
     * @param features
     * @param aJCas
     * @param event1
     * @param event2
     */
    private void addFeatureWordNetSimilarity(List<PairwiseEventFeature> features, JCas aJCas,
                                             EventMention event1, EventMention event2) {
        // Extracts lemma of the head word of a event mention string.
        //String lemma1 = event1.getHeadWord().getLemma();
        //String lemma2 = event2.getHeadWord().getLemma();

        List<StanfordCorenlpToken> token1s = JCasUtil.selectCovered(aJCas, StanfordCorenlpToken.class,
                event1.getBegin(), event1.getEnd());
        List<StanfordCorenlpToken> token2s = JCasUtil.selectCovered(aJCas, StanfordCorenlpToken.class,
                event2.getBegin(), event2.getEnd());

        if (ListUtils.isNullOrEmptyList(token1s) || ListUtils.isNullOrEmptyList(token2s)) {
            return;
        }

        StringBuilder buf1 = new StringBuilder();
        for (int i = 0; i < token1s.size(); i++) {
            if (i != 0) {
                buf1.append(" ");
            }
            buf1.append(token1s.get(i).getLemma());
        }
        String lemma1 = buf1.toString();

        StringBuilder buf2 = new StringBuilder();
        for (int i = 0; i < token2s.size(); i++) {
            if (i != 0) {
                buf1.append(" ");
            }
            buf2.append(token2s.get(i).getLemma());
        }
        String lemma2 = buf2.toString();

        if (StringUtils.isNullOrEmptyString(lemma1) || StringUtils.isNullOrEmptyString(lemma2)) {
            return;
        }

        Pair<String, String> lemmaPair = new Pair<String, String>(lemma1, lemma2);
        if (cache.containsKey(lemmaPair)) {
            // In the case of cache hit
            // LogUtils.logInfo("Cache hit for " + lemmaPair.toString());
            Map<String, Double> wordnetSimilarityScores = cache.get(lemmaPair);
            for (String wordnetSimKey : wordnetSimilarityScores.keySet()) {
                double score = wordnetSimilarityScores.get(wordnetSimKey);

                StringBuilder buf = new StringBuilder();
                buf.append(PairwiseEventFeatureInfo.WORDNET_SIMILARITY.toString());
                buf.append(separator);
                buf.append(wordnetSimKey);
                String featureName = buf.toString();

                PairwiseEventFeature feature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                        featureName, score, false);
                features.add(feature);
            }

            return;
        }

        wnsc.setWordLemmas(lemma1, lemma2);
        wnsc.calcWordNetSimilarity();

        Map<String, Double> wordnetSimScores = new HashMap<String, Double>();
        for (Entry<String, Double> wordnetSimEntry : wnsc.getWordnetSimilarityScores().entrySet()) {
            String wordnetSimKey = wordnetSimEntry.getKey();
            double score = wordnetSimEntry.getValue();
            if (score > MAX_SIMILARITY_SCORE) {
                // Set an upper bound score.
                score = MAX_SIMILARITY_SCORE;
            }

            StringBuilder buf = new StringBuilder();
            buf.append(PairwiseEventFeatureInfo.WORDNET_SIMILARITY.toString());
            buf.append(separator);
            buf.append(wordnetSimKey);
            String featureName = buf.toString();

            // LogUtils.log("Feature name: " + featureName + ", " + "WordNet similarity score: " + score);

            PairwiseEventFeature feature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                    featureName, score, false);

            features.add(feature);

            wordnetSimScores.put(wordnetSimKey, score);
        }
        cache.put(lemmaPair, wordnetSimScores);
    }

}
