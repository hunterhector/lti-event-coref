package edu.cmu.lti.event_coref.analysis_engine.syntatic;

import com.google.common.collect.Iterables;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.utils.general.StringUtils;
import edu.cmu.lti.utils.model.AnnotationCondition;
import edu.cmu.lti.utils.model.Span;
import edu.cmu.lti.utils.type.ComponentAnnotation;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

public class ExtendedChunkerAnnotator extends JCasAnnotator_ImplBase {
    Set<String> usefulPunctuations = new HashSet<String>();

    private static final Logger logger = LoggerFactory.getLogger(ExtendedChunkerAnnotator.class);

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        UimaConvenience.printProcessLog(aJCas, logger);

        usefulPunctuations.addAll(Arrays.asList("(", ")", "[", "]"));

        Map<StanfordCorenlpToken, Word> token2Word = new HashMap<StanfordCorenlpToken, Word>();
        for (StanfordToken2WordAlignment alignment : JCasUtil.select(aJCas,
                StanfordToken2WordAlignment.class)) {
            token2Word.put(alignment.getToken(), alignment.getWord());
        }

        // 1. first merge NPs that are separated by 's
        AnnotationCondition npChunk = new AnnotationCondition() {
            @Override
            public Boolean check(TOP aAnnotation) {
                OpennlpChunk chunk = (OpennlpChunk) aAnnotation;
                if (chunk.getTag().equals("NP")) {
                    return true;
                }
                return false;
            }
        };

        List<OpennlpChunk> npChunks = UimaConvenience.getAnnotationListWithFilter(aJCas,
                OpennlpChunk.class, npChunk);
        List<Word> allWords = UimaConvenience.getAnnotationList(aJCas, Word.class);

        List<OpennlpChunk> posMergedChunks = new ArrayList<OpennlpChunk>();

        OpennlpChunk previousChunk = null;
        boolean skipNext = false;

        for (OpennlpChunk chunk : npChunks) {
            if (skipNext == false) {
                if (previousChunk != null) {
                    List<Word> currentWords = JCasUtil.selectCovered(Word.class, chunk);
                    List<Word> previousWords = JCasUtil.selectCovered(Word.class, previousChunk);

//                    if (previousWords.isEmpty()) {
//                        System.out.println(previousChunk.getCoveredText() + " " + previousChunk.getBegin()
//                                + " " + previousChunk.getEnd());
//                    }
//                    if (currentWords.isEmpty()) {
//                        System.out.println(chunk.getCoveredText() + " " + chunk.getBegin() + " "
//                                + chunk.getEnd());
//                    }

                    int previousEndIndex = Integer.parseInt(previousWords.get(previousWords.size() - 1)
                            .getWordId());
                    int currentBeginIndex = Integer.parseInt(currentWords.get(0).getWordId());

                    boolean merged = false;

                    if (currentBeginIndex - previousEndIndex == 2) {
                        int middleIndex = previousEndIndex + 1;
                        String middleWordPos = allWords.get(middleIndex - 1).getPartOfSpeech();

                        // case when the middle POSSESSIVE is omitted
                        if (middleWordPos != null && middleWordPos.equals("POS")) {
                            merged = true;
                            skipNext = true;
                        }

                        Set<Word> currentWordSet = new HashSet<Word>(currentWords);

                        // case when the middle one is of and the two terms are "prep_of" relations
                        for (Word previousWord : previousWords) {
                            List<StanfordDependencyNode> wordsInNode = JCasUtil.selectCovered(
                                    StanfordDependencyNode.class, previousWord);
                            if (wordsInNode.isEmpty())
                                continue;
                            StanfordDependencyNode previousNode = wordsInNode.get(0);
                            FSList previousChildRelationsFSList = previousNode.getChildRelations();
                            if (previousChildRelationsFSList != null) {
                                for (StanfordDependencyRelation previousChildRelation : FSCollectionFactory.create(
                                        previousChildRelationsFSList, StanfordDependencyRelation.class)) {

                                    if (previousChildRelation.getRelationType().equals("prep_of")) {
                                        StanfordDependencyNode previousChild = previousChildRelation.getChild();
                                        Word previousChildWord = token2Word.get(previousChild);
                                        if (currentWordSet.contains(previousChildWord)) {
                                            merged = true;
                                            skipNext = true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // case when the middle POS is attached to the latter NP
                    if (currentBeginIndex - previousEndIndex == 1) {
                        String currentBeginPos = currentWords.get(0).getPartOfSpeech();
                        if (currentBeginPos != null && currentBeginPos.equals("POS")) {
                            merged = true;
                            skipNext = true;
                        }
                    }

                    if (merged) {
                        OpennlpChunk mergedChunk = new OpennlpChunk(aJCas);
                        mergedChunk.setBegin(previousChunk.getBegin());
                        mergedChunk.setEnd(chunk.getEnd());
                        posMergedChunks.add(mergedChunk);
                    }

                    if (!merged) {
                        posMergedChunks.add(previousChunk);
                    }
                }
            } else {
                skipNext = false;
            }
            previousChunk = chunk;
        }

        for (OpennlpChunk chunk : posMergedChunks) {
            Span chunkCleanSpan = createCleanSpan(chunk);
            if (chunkCleanSpan != null) {
                ExtendedNPChunk cleanedNPChunk = new ExtendedNPChunk(aJCas, chunkCleanSpan.getBegin(),
                        chunkCleanSpan.getEnd());
                cleanedNPChunk.addToIndexes(aJCas);
            }
        }
    }

    private Map<StanfordCorenlpToken, Word> buildWord2Token(JCas aJCas) {

        Map<Word, Collection<StanfordCorenlpToken>> wordCoveredToken = JCasUtil.indexCovered(aJCas,
                Word.class, StanfordCorenlpToken.class);

        Map<Word, Collection<StanfordCorenlpToken>> wordCoveringToken = JCasUtil.indexCovering(aJCas,
                Word.class, StanfordCorenlpToken.class);

        Map<StanfordCorenlpToken, Word> token2Word = new HashMap<StanfordCorenlpToken, Word>();

        for (Entry<Word, Collection<StanfordCorenlpToken>> entry : wordCoveredToken.entrySet()) {
            Word word = entry.getKey();
            Collection<StanfordCorenlpToken> coveredTokens = entry.getValue();
            if (coveredTokens.size() > 0) {
                // use the first covering words part of speech
                StanfordCorenlpToken token = Iterables.get(coveredTokens, 0);
                token2Word.put(token, word);
            } else {// in case the token range is larger than the word, use its covering token
                Collection<StanfordCorenlpToken> coveringToken = wordCoveringToken.get(word);
                if (coveringToken.size() == 0) {
                    logger.warn(String.format("The word : %s cannot be associated with a Stanford Token, not assigning POS to it",
                            word.getCoveredText()));
                } else {
                    StanfordCorenlpToken token = Iterables.get(coveringToken, 0);
                    token2Word.put(token, word);
                }
            }
        }
        return token2Word;
    }

    private Span createCleanSpan(ComponentAnnotation anno) {
        List<Word> words = JCasUtil.selectCovered(Word.class, anno);
        List<Word> filteredWords = new ArrayList<Word>();

        for (Word word : words) {
            String wordStr = word.getCoveredText();
            if (!StringUtils.noLetterOrDigit(wordStr) || usefulPunctuations.contains(wordStr)) {
                filteredWords.add(word);
            }
        }

        if (filteredWords.size() == 0) {
            return null;
        }

        int begin = -1;
        int end = -1;

        for (Word word : filteredWords) {
            if (begin == -1)
                begin = word.getBegin();
            int newEnd = word.getEnd();
            if (newEnd > end)
                end = newEnd;
        }

        return new Span(begin, end);
    }
}
