/**
 *
 */
package edu.cmu.lti.event_coref.features.lexical;

import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.FanseDependencyUtils;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.util.JCasUtil;

import java.util.*;

/**
 * @author Zhengzhong Liu, Hector
 */
public class EventStrictStringFeatures extends PairwiseFeatureGenerator {
    private static final Logger logger = LoggerFactory.getLogger(EventStrictStringFeatures.class);

    Map<EventMention, String> mention2LongChunk = new HashMap<EventMention, String>();

    Map<String, Double> lemma2Tfidf = new HashMap<String, Double>();

    String[] stopwordsRCV = {"a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has",
            "have", "he", "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "will",
            "with", "which"};

    Set<String> minimalStopWord = new HashSet<String>(Arrays.asList(stopwordsRCV));

    public EventStrictStringFeatures(JCas aJCas) {
        for (ExtendedNPChunk chunk : JCasUtil.select(aJCas, ExtendedNPChunk.class)) {
            List<StanfordCorenlpToken> wordsInChunk = JCasUtil.selectCovered(StanfordCorenlpToken.class,
                    chunk);

            // we need something other than determiner, at least one more
            int contentWordCount = 0;
            for (StanfordCorenlpToken word : wordsInChunk) {
                String pos = word.getPos();

                if (pos != null) {
                    if (!(pos.equals("DT") || pos.startsWith("PRP") || pos.equals("IN") || pos.equals("TO"))) {
                        contentWordCount++;
                    }
                } else {
                    logger.warn("No part of speech found for " + word.getCoveredText());
                }
            }

            if (contentWordCount >= 2) {
                for (EventMention evm : JCasUtil.selectCovered(EventMention.class, chunk)) {
                    mention2LongChunk.put(evm, chunk.getCoveredText().toLowerCase().replace("\n", " "));
                }
            }
        }

        Collection<TermFrequencyInfo> tfInfos = JCasUtil.select(aJCas, TermFrequencyInfo.class);
        for (TermFrequencyInfo tfInfo : tfInfos) {
            String lemma = tfInfo.getLemma();
            int df = tfInfo.getDocumentFrequency();
            lemma2Tfidf.put(lemma, 1.0 / df);
        }

    }

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

        createExactChunkMatch(aJCas, features, event1, event2);
        // createSubTreeWordOverlap(aJCas, features, event1, event2);

        return features;
    }

    /*
     * Chunk match is mimic to the stanford multi-sieve exact match (sieve 1), but it is only active
     * for noun phrases, thus only active for nominal events
     */
    private void createExactChunkMatch(JCas aJCas, List<PairwiseEventFeature> features,
                                       EventMention event1, EventMention event2) {
        boolean chunkExactMath = false;

        String chunk1 = mention2LongChunk.get(event1);
        String chunk2 = mention2LongChunk.get(event2);

        String headWord1 = event1.getHeadWord().getCoveredText().toLowerCase();
        String headWord2 = event2.getHeadWord().getCoveredText().toLowerCase();

        if (chunk1 != null && chunk2 != null) {
            if (chunk1.equals(chunk2) && headWord1.equals(headWord2)) {
                chunkExactMath = true;
                logger.debug("Chunk match triggered" + " " + chunk1 + " " + chunk2);
            }
            features.add(FeatureUtils.createPairwiseEventBinaryFeature(aJCas, "ExactLongChunkMatch",
                    chunkExactMath, false));
        }
    }

    private void createSubTreeWordOverlap(JCas aJCas, List<PairwiseEventFeature> features,
                                          EventMention event1, EventMention event2) {
        List<Word> event1ChildWords = FanseDependencyUtils.getAllChildrenWords(event1.getHeadWord());
        List<Word> event2ChildWords = FanseDependencyUtils.getAllChildrenWords(event2.getHeadWord());

        double length1 = 0;
        double length2 = 0;
        double dp = 0;

        for (Word event1ChildWord : event1ChildWords) {
            for (Word event2ChildWord : event2ChildWords) {

                String lemma1 = event1ChildWord.getLemma().toLowerCase();
                String lemma2 = event2ChildWord.getLemma().toLowerCase();

                if (lemma1.endsWith("\\")) {
                    lemma1 = lemma1.substring(0, lemma1.length() - 1);
                }

                if (lemma2.endsWith("\\")) {
                    lemma2 = lemma2.substring(0, lemma2.length() - 1);
                }

                // don't use idf instead
                double idf1 = 1;
                double idf2 = 1;

                if (lemma1.equals(lemma2) && !minimalStopWord.contains(lemma1)) {
                    dp += idf1 * idf2;
                }

                if (!minimalStopWord.contains(lemma1)) {
                    length1 += idf1 * idf1;
                }

                if (!minimalStopWord.contains(lemma1)) {
                    length2 += idf2 * idf2;
                }
            }
        }

        double cos = 0;
        if (length1 != 0 && length2 != 0) {
            // Avoids the undefined value (divided by 0);
            cos = dp / Math.sqrt(length1 * length2);
        } else {
            try {
                throw new Exception("Event contains no children or only stop word children");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        features.add(FeatureUtils.createPairwiseEventNumericFeature(aJCas, "EventSubtreeTfIdf", cos,
                false));
    }
}
