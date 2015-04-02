package edu.cmu.lti.event_coref.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.utils.type.ComponentAnnotation;
import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import net.ricecode.similarity.DiceCoefficientStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityServiceImpl;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.util.JCasUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * A helper class that perform various similarity measures But it might be a bad idea to put to much
 * things here
 *
 * @author Zhengzhong Liu, Hector
 */
public class SimilarityCalculator {
    private static final Logger logger = LoggerFactory.getLogger(SimilarityCalculator.class);

    static WordNetSimilarityCalculator wnsc;

    static diff_match_patch dmp = new diff_match_patch();

    static boolean verbose = false;

    // better idea is to create a file also to repeat experiments
    private static Table<String, String, Double> wordNetCache = HashBasedTable.create();

    private static SimilarityStrategy strategy = new DiceCoefficientStrategy();

    private static StringSimilarityServiceImpl service = new StringSimilarityServiceImpl(strategy);

    public SimilarityCalculator() {
        logger.info("Initializing row filler...");
        wnsc = new WordNetSimilarityCalculator();
        logger.info("Similarity calculator initialized.");
    }

    public SimilarityCalculator(boolean noWordNet) {
        if (noWordNet)
            logger.info("Similarity calculator initilized without wordnet loaded");
        else
            throw new IllegalArgumentException("Well, it must be true if you use this one");
    }

    public double checkExactMatch(List<ComponentAnnotation> annoList1,
                                  List<ComponentAnnotation> annoList2) {
        for (ComponentAnnotation anno1 : annoList1) {
            for (ComponentAnnotation anno2 : annoList2) {
                if (anno1.getCoveredText().equals(anno2.getCoveredText()))
                    return 1.0;
            }
        }
        return 0.0;
    }

    public boolean checkEntityCorference(EntityMention em1, EntityMention em2) {
        EntityCoreferenceCluster cluster1 = ClusterUtils.getEntityFullClusterSystem(em1);
        EntityCoreferenceCluster cluster2 = ClusterUtils.getEntityFullClusterSystem(em2);
        if (cluster1 != null && cluster2 != null && cluster1 == cluster2) {
            if (verbose)
                logger.info(String.format("  - [%s] and [%s] corefers", em1.getCoveredText(),
                        em2.getCoveredText()));
            return true;
        }
        return false;
    }

    /**
     * Check the surface similarity using the cluster mentions that contains at least one noun
     *
     * @param component1
     * @param component2
     * @return
     */
    public double checkClusterSurfaceSimilarity(EntityBasedComponent component1,
                                                EntityBasedComponent component2) {
        double maxClusterSimilarity = 0;
        for (EntityMention mention1 : FSCollectionFactory.create(
                component1.getContainingEntityMentions(), EntityMention.class)) {
            for (EntityMention mention2 : FSCollectionFactory.create(
                    component2.getContainingEntityMentions(), EntityMention.class)) {
                double sim = getClosestInterClusterStringSimilarity(mention1, mention2);
                if (sim > maxClusterSimilarity) {
                    maxClusterSimilarity = sim;
                }
            }
        }
        return maxClusterSimilarity;
    }

    /**
     * Check the surface similarity using the cluster mentions that contains at least one noun
     *
     * @param em1
     * @param em2
     * @return
     */
    public double getClosestInterClusterStringSimilarity(EntityMention em1, EntityMention em2) {
        EntityCoreferenceCluster cluster1 = ClusterUtils.getEntityFullClusterSystem(em1);
        EntityCoreferenceCluster cluster2 = ClusterUtils.getEntityFullClusterSystem(em2);

        List<EntityMention> em1Alternatives = new LinkedList<EntityMention>();
        List<EntityMention> em2Alternatives = new LinkedList<EntityMention>();

        if (cluster1 != null) {
            for (EntityMention emFromCluster1 : FSCollectionFactory.create(cluster1.getEntityMentions(),
                    EntityMention.class)) {
                if (!emFromCluster1.equals(em1) && containsNoun(emFromCluster1)) {
                    em1Alternatives.add(emFromCluster1);
                }
            }
        }

        if (cluster2 != null) {
            for (EntityMention emFromCluster2 : FSCollectionFactory.create(cluster2.getEntityMentions(),
                    EntityMention.class)) {
                if (!emFromCluster2.equals(em2) && containsNoun(emFromCluster2)) {
                    em2Alternatives.add(emFromCluster2);
                }
            }
        }

        double maxSim = 0.0;
        for (EntityMention emAlter1 : em1Alternatives) {
            for (EntityMention emAlter2 : em2Alternatives) {
                if (verbose)
                    logger.info(String.format("Comparing their alternative forms : [%s] - [%s]",
                            emAlter1.getCoveredText(), emAlter2.getCoveredText()));

                double simScore = relaxedDiceTest(emAlter1.getCoveredText(), emAlter2.getCoveredText());
                if (simScore > maxSim)
                    maxSim = simScore;
            }
        }
        return maxSim;
    }

    private boolean containsNoun(ComponentAnnotation anno) {

        try {
            List<Word> emWords = JCasUtil.selectCovered(Word.class, anno);

            for (Word word : emWords) {
                if (word.getPartOfSpeech().startsWith("N")) {
                    return true;
                }
            }
        } catch (Exception e) {
            // "Probably words are not annotated with part of speech, use stanford instead"
        }

        List<StanfordCorenlpToken> emTokens = JCasUtil.selectCovered(StanfordCorenlpToken.class, anno);
        for (StanfordCorenlpToken token : emTokens) {
            if (token.getPos().startsWith("N")) {
                return true;
            }
        }
        return false;
    }

    public boolean subStringTest(String str1, String str2) {
        if (str1.contains(str2) || str2.contains(str1))
            return true;
        else
            return false;
    }

    public Double getEditDistance(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return null;
        }

        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();

        // use some method to calculate anno text based similarity
        // text based comparison
        LinkedList<Diff> diffs = dmp.diff_main(str1, str2);
        double longerName = Math.max(str1.length(), str2.length());

        double surfaceSim = 1 - dmp.diff_levenshtein(diffs) / longerName;

        return surfaceSim;
    }

    public Double cannonicDiceTest(ComponentAnnotation anno1, ComponentAnnotation anno2) {
        String str1 = getLemma(anno1);
        String str2 = getLemma(anno2);
        return getDiceCoefficient(str1, str2);
    }

    public Double relaxedDiceTest(String str1, String str2) {
        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();

        return Math.max(getSuffixDiceCoefficient(str1, str2),
                Math.max(getDiceCoefficient(str1, str2), getPrefixDiceCoefficient(str1, str2)));
    }

    public Double getDiceCoefficient(String text1, String text2) {
        return service.score(text1.toLowerCase(), text2.toLowerCase());
    }

    public Double getPrefixDiceCoefficient(String text1, String text2) {
        int n = Math.min(text1.length(), text2.length());
        return service.score(text1.substring(0, n), text2.substring(0, n));
    }

    public Double getSuffixDiceCoefficient(String text1, String text2) {
        int len1 = text1.length();
        int len2 = text2.length();
        int n = Math.min(len1, len2);
        return service.score(text1.substring(len1 - n, len1), text2.substring(len2 - n, len2));
    }

    public double getLemmaWordNetSimilarity(List<Word> words1, List<Word> words2) {
        String emIWords = getLemma(words1);
        String emJWords = getLemma(words2);

        return getWordNetSimilarity(emIWords, emJWords);
    }

    public double getLemmaWordNetSimilarity(Word word1, Word word2) {
        String emIWord = getLemma(word1);
        String emJWord = getLemma(word2);
        return getWordNetSimilarity(emIWord, emJWord);
    }

    // this method was cached to improve speed
    public double getWordNetSimilarity(String str1, String str2) {
        String smallWord = null;
        String largeWord = null;
        if (str1.compareTo(str2) > 0) {
            smallWord = str2.trim();
            largeWord = str1.trim();
        } else {
            smallWord = str1.trim();
            largeWord = str2.trim();
        }

        double simScore;
        if (wordNetCache.contains(smallWord, largeWord)) {
            simScore = wordNetCache.get(smallWord, largeWord);
        } else {
            if (str1.equals("") || str2.equals("")) {
                simScore = 0;
            } else {
                wnsc.setWordLemmas(str1, str2);
                wnsc.calcWordNetSimilarity();
                simScore = wnsc.getWordNetSimilarityScore();
            }
            wordNetCache.put(smallWord, largeWord, simScore);
        }

        return simScore;
    }

    public String getLemma(ComponentAnnotation anno) {
        if (anno instanceof Word) {
            return ((Word) anno).getLemma();
        } else {
            return getLemma(JCasUtil.selectCovered(Word.class, anno));
        }
    }

    public String getLemma(List<Word> words) {
        if (words == null || words.isEmpty()) {
            return "";
        }

        StringBuffer buf = new StringBuffer();
        for (Word word : words) {
            buf.append(word.getLemma());
            buf.append(" ");
        }

        return buf.toString().trim();
    }
}
