package edu.cmu.lti.event_coref.utils;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.*;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class WordNetSimilarityCalculator {

    private static ILexicalDatabase db;

    private Map<String, RelatednessCalculator> rcs;

    private Map<String, Double> wordnetSimilarityScores;

    private String lemma1;

    private String lemma2;

    private double wordnetSimilarityScore;

    private double definitionBasedScore;

    private static final String SIM_KEY_HIRST = "HirstStOnge";

    private static final String SIM_KEY_LEACOCK = "LeacockChodorow";

    private static final String SIM_KEY_LESK = "Lesk";

    private static final String SIM_KEY_WUPALMER = "WuPalmer";

    private static final String SIM_KEY_RESNIK = "Resnik";

    private static final String SIM_KEY_JIANG = "Jiang";

    private static final String SIM_KEY_LIN = "Lin";

    private static final String SIM_KEY_PATH = "Path";

    public WordNetSimilarityCalculator() {
        db = new NictWordNet();
        rcs = new HashMap<String, RelatednessCalculator>();
        wordnetSimilarityScores = new HashMap<String, Double>();

        // Some metrics are removed because they are not normalized between 0 and 1.
        rcs.put(SIM_KEY_HIRST, new HirstStOnge(db));
        rcs.put(SIM_KEY_LEACOCK, new LeacockChodorow(db));
        rcs.put(SIM_KEY_LESK, new Lesk(db));
        rcs.put(SIM_KEY_WUPALMER, new WuPalmer(db));
        rcs.put(SIM_KEY_RESNIK, new Resnik(db));
        rcs.put(SIM_KEY_JIANG, new JiangConrath(db));
        rcs.put(SIM_KEY_LIN, new Lin(db));
        rcs.put(SIM_KEY_PATH, new Path(db));
    }

    public void setWordLemmas(String lemma1, String lemma2) {
        this.lemma1 = lemma1;
        this.lemma2 = lemma2;
    }

    public void calcWordNetSimilarity() {
        WS4JConfiguration.getInstance().setMFS(true);
        double score = 0.0;
        for (String simKey : rcs.keySet()) {
            RelatednessCalculator rc = rcs.get(simKey);
            score = rc.calcRelatednessOfWords(lemma1, lemma2);
            // LogUtils.log(simKey + "\t" + score);
            if (score > 1) {
                score = 1.0;// a bug in the WN4J
            }

            wordnetSimilarityScores.put(simKey, score);
        }

        wordnetSimilarityScore = wordnetSimilarityScores.get(SIM_KEY_WUPALMER);
        definitionBasedScore = wordnetSimilarityScores.get(SIM_KEY_LESK);
    }

    public double getWordNetSimilarityScore() {
        return wordnetSimilarityScore;
    }

    public double getDefinitionBasedScore() {
        return definitionBasedScore;
    }

    public boolean isSameWord() {
        if (lemma1 == null || lemma2 == null) {
            return false;
        }

        if (lemma1.equals(lemma2)) {
            return true;
        }
        return false;
    }

    public Map<String, Double> getWordnetSimilarityScores() {
        return wordnetSimilarityScores;
    }

    private boolean isNormalizedScore(Double score) {
        if (score >= 0.0 && score <= 1.0) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        WordNetSimilarityCalculator wnsc = new WordNetSimilarityCalculator();

        Map<String, String> testMap = new HashMap<String, String>();
        // testMap.put("halt", "suspend");
        // testMap.put("explode", "bombing");
        // testMap.put("bomb", "explode");
        // testMap.put("fire", "shot");
        // testMap.put("check into", "enter");
        // testMap.put("Firestone", "he");
        testMap.put("fight", "attack");
        testMap.put("injured", "wounded people");
        // testMap.put("gunbattle","shootout");
        // testMap.put( "battle","shootout");
        // testMap.put( "gun","shootout");
        for (Entry<String, String> wordEntry : testMap.entrySet()) {
            String word1 = wordEntry.getKey();
            String word2 = wordEntry.getValue();
            wnsc.setWordLemmas(word1, word2);
            wnsc.calcWordNetSimilarity();
            for (Entry<String, Double> keyScore : wnsc.wordnetSimilarityScores.entrySet()) {
                String key = keyScore.getKey();
                Double score = keyScore.getValue();
                System.out.println(String.format("[%s] score for %s and %s : %s", key, word1, word2, score));
            }
        }
    }
}
