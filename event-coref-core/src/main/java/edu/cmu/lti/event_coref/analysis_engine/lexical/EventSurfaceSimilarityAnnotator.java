package edu.cmu.lti.event_coref.analysis_engine.lexical;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.EventSurfaceSimilarity;
import edu.cmu.lti.event_coref.type.Word;
import edu.cmu.lti.event_coref.utils.SennaWordSimilarityCalculator;
import edu.cmu.lti.event_coref.utils.SimilarityCalculator;
import edu.cmu.lti.event_coref.utils.WordNetSimilarityCalculator;
import edu.cmu.lti.utils.model.AnnotationCondition;
import edu.cmu.lti.utils.type.ComponentAnnotation;
import edu.cmu.lti.utils.uima.UimaConvenience;
import edu.washington.cs.knowitall.morpha.MorphaStemmer;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This code is refactored from the original wordnet similarity to create
 * similarity measure for event surfaces using different measures.
 * <p/>
 * A recent change consider using the head word only
 * <p/>
 * Currently it includes:
 * <p/>
 * 1. Dice coefficient (for directy string matching) 2. WordNet similarity 3.
 * Senna similarity (distributional similarity) 4. WordNet similarity on
 * demorphied words
 *
 * @author Zhengzhong Liu, Hector
 */
public class EventSurfaceSimilarityAnnotator extends JCasAnnotator_ImplBase {
    private static final Logger logger = LoggerFactory.getLogger(EventSurfaceSimilarityAnnotator.class);

    // *************The parameter handling part******************//
    public static final String PARAM_SENNA_EMBEDDINGS = "Senna_Embeddings_Path";

    public static final String PARAM_SENNA_WORDLIST = "Senna_Wordlist_Path";

    public static final String PARAM_DO_WORDNET = "Do_wordNet_sim";

    @ConfigurationParameter(name = PARAM_SENNA_EMBEDDINGS)
    private String embeddingsPath;

    @ConfigurationParameter(name = PARAM_SENNA_WORDLIST)
    private String sennaWorListPath;

    @ConfigurationParameter(name = PARAM_DO_WORDNET, description = "WordNet lookup is very slow now, you might wanna disable this now")
    private Boolean doWordNetSim;

    // *************The parameter handling part******************//
    public static final String ANNOTATOR_COMPONENT_ID = "system-event-surface";

    private SimilarityCalculator simCal = new SimilarityCalculator();

    // Prepare the similarity calculators
    private SennaWordSimilarityCalculator sennaCal;

    private WordNetSimilarityCalculator wnsc;

    // use a cahce to make the process more efficient
    // it might be more memory friendly to combine the Cache into one table
    private Table<String, String, Double> wordNetCache = HashBasedTable
            .create();

    private Table<String, String, Double> sennaCache = HashBasedTable.create();

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException {
        super.initialize(aContext);
        sennaCal = new SennaWordSimilarityCalculator(embeddingsPath,
                sennaWorListPath);

        if (doWordNetSim) {
            wnsc = new WordNetSimilarityCalculator();
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        logger.info(String.format("Processing article: %s with [%s]",
                UimaConvenience.getShortDocumentName(aJCas), this.getClass()
                        .getSimpleName()));

        List<EventMention> allEventMentions = new ArrayList<EventMention>(
                UimaConvenience.getAnnotationListWithFilter(aJCas,
                        EventMention.class, new AnnotationCondition() {
                            @Override
                            public Boolean check(TOP aAnnotation) {
                                ComponentAnnotation comp = (ComponentAnnotation) aAnnotation;
                                if (comp.getBegin() > 0 && comp.getEnd() > 0)
                                    return true;
                                return false;
                            }
                        }));

        int evmNum = allEventMentions.size();
        int totalPairs = (1 + evmNum) * evmNum / 2;

        logger.info(String
                .format("Calculating similariteds for totaly %d event mentions and %d event mention pairs: ",
                        evmNum, totalPairs));

        int tenPercent = totalPairs / 10;
        int percentCounter = 0;
        int pairCounter = 0;

        for (int i = 0; i < evmNum; i++) {
            for (int j = i + 1; j < evmNum; j++) {
                pairCounter++;

                EventMention emI = allEventMentions.get(i);
                EventMention emJ = allEventMentions.get(j);

                String emILemma = getLemma(emI);
                String emJLemma = getLemma(emJ);

                String smallWordLemma = "";
                String largeWordLemma = "";
                if (emILemma.compareTo(emJLemma) > 0) {
                    smallWordLemma = emJLemma;
                    largeWordLemma = emILemma;
                } else {
                    smallWordLemma = emILemma;
                    largeWordLemma = emJLemma;
                }

                String emIMorpha = getMorpha(emI);
                String emJMorpha = getMorpha(emJ);

                String smallMorpha = "";
                String largeMorpha = "";
                if (emIMorpha.compareTo(emJMorpha) > 0) {
                    smallMorpha = emJMorpha;
                    largeMorpha = emIMorpha;
                } else {
                    smallMorpha = emIMorpha;
                    largeMorpha = emJMorpha;
                }

                EventSurfaceSimilarity surfaceSimilarity = new EventSurfaceSimilarity(
                        aJCas);
                surfaceSimilarity.setEventMentionI(emI);
                surfaceSimilarity.setEventMentionJ(emJ);
                surfaceSimilarity.addToIndexes();
                surfaceSimilarity.setComponentId(ANNOTATOR_COMPONENT_ID);

                if (doWordNetSim) {
                    double lemmaWP = calWordNetSimilarity(aJCas, wnsc,
                            smallWordLemma, largeWordLemma);
                    double morphaWP = calWordNetSimilarity(aJCas, wnsc,
                            smallMorpha, largeMorpha);

                    surfaceSimilarity.setWordNetWuPalmer(lemmaWP);
                    surfaceSimilarity.setMorphalizedWuPalmer(morphaWP);
                } else {
                    surfaceSimilarity.setWordNetWuPalmer(-1);
                    surfaceSimilarity.setMorphalizedWuPalmer(-1);
                }

                double lemmaSenna = calSennaSimilarity(aJCas, sennaCal,
                        surfaceSimilarity, smallWordLemma, largeWordLemma);
                surfaceSimilarity.setSennaSimilarity(lemmaSenna);
                // dice is more like confirming whether two words are the same,
                // probably useful in
                // misspelling case
                double lemmaDice = calDiceCoefficient(aJCas, surfaceSimilarity,
                        smallWordLemma, largeWordLemma);
                surfaceSimilarity.setDiceCoefficient(lemmaDice);

                if (tenPercent != 0) {
                    if (pairCounter % tenPercent == 0) {
                        percentCounter++;
                        logger.info(String.format("%d0 %s (%d pairs) of the event mention pairs finished",
                                percentCounter, "%", pairCounter));
                    }
                }
            }
        }
    }

    private double calDiceCoefficient(JCas aJCas,
                                      EventSurfaceSimilarity surfaceSimilarity, String str1, String str2) {
        return simCal.getDiceCoefficient(str1, str2);
    }

    private double calSennaSimilarity(JCas aJCas,
                                      SennaWordSimilarityCalculator sennaCal,
                                      EventSurfaceSimilarity surfaceSimilarity, String smallWords,
                                      String largeWords) {
        double sennaScore;
        if (sennaCache.contains(smallWords, largeWords)) {
            sennaScore = sennaCache.get(smallWords, largeWords);
        } else {
            sennaScore = sennaCal.getCosineSimilarity(smallWords, largeWords);
            sennaCache.put(smallWords, largeWords, sennaScore);
        }
        return sennaScore;
    }

    private double calWordNetSimilarity(JCas aJCas,
                                        WordNetSimilarityCalculator wnsc, String smallWord, String largeWord) {
        double wulPalmerScore;
        if (wordNetCache.contains(smallWord, largeWord)) {
            wulPalmerScore = wordNetCache.get(smallWord, largeWord);
        } else {
            wnsc.setWordLemmas(smallWord, largeWord);
            wnsc.calcWordNetSimilarity();
            wulPalmerScore = wnsc.getWordNetSimilarityScore();
            wordNetCache.put(smallWord, largeWord, wulPalmerScore);
        }

        return wulPalmerScore;
    }

    private String getLemma(EventMention evm) {
        String lemma;
        Word word = evm.getHeadWord();
        if (word == null) {
            lemma = MorphaStemmer.stemToken(getCoveredTextWithoutSpace(evm));
        } else {
            lemma = word.getLemma();
        }
        return lemma;
    }

    // very hacky
    private String getCoveredTextWithoutSpace(EventMention evm) {
        String text = evm.getCoveredText();
        if (text.contains(" ")) {
            String[] parts = text.split(" ");
            String longestPart = "";
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (part.length() > longestPart.length()) {
                    longestPart = part;
                }
            }
            return longestPart;
        }
        return text;
    }

    private String getMorpha(EventMention evm) {
        String morpha;
        Word word = evm.getHeadWord();
        if (word == null) {
            morpha = MorphaStemmer.stemToken(getCoveredTextWithoutSpace(evm));
        } else {
            morpha = word.getMorpha();
        }
        return morpha;
    }
}
