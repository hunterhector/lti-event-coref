/**
 *
 */
package edu.cmu.lti.event_coref.analysis_engine.prerequisite;

import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.utils.type.ComponentAnnotation;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Align different token annotations with the native Word annotation
 *
 * @author Zhengzhong Liu, Hector
 */
public class TokenAlignmentAnnotator extends JCasAnnotator_ImplBase {
    private static final Logger logger = LoggerFactory.getLogger(TokenAlignmentAnnotator.class);

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        logger.info(String.format("Processing article: %s with [%s]",
                UimaConvenience.getShortDocumentName(aJCas), this.getClass().getSimpleName()));

        Map<StanfordCorenlpToken, Word> stanfordToken2Word = getToken2Word(aJCas,
                StanfordCorenlpToken.class);

        for (Entry<StanfordCorenlpToken, Word> entry : stanfordToken2Word.entrySet()) {
            StanfordCorenlpToken token = entry.getKey();
            Word word = entry.getValue();

            StanfordToken2WordAlignment align = new StanfordToken2WordAlignment(aJCas);
            align.setWord(word);
            align.setToken(token);
            align.addToIndexes();
        }

        Map<FanseToken, Word> fanseToken2Word = getToken2Word(aJCas, FanseToken.class);

        for (Entry<FanseToken, Word> entry : fanseToken2Word.entrySet()) {
            FanseToken token = entry.getKey();
            Word word = entry.getValue();
            FanseToken2WordAlignment align = new FanseToken2WordAlignment(aJCas);
            align.setWord(word);
            align.setToken(token);
            align.addToIndexes();
        }
    }

    private <T extends ComponentAnnotation> Map<T, Word> getToken2Word(JCas aJCas, Class<T> clazz) {
        Map<Word, Collection<T>> wordCoveredToken = JCasUtil.indexCovered(aJCas, Word.class, clazz);

        Map<Word, Collection<T>> wordCoveringToken = JCasUtil.indexCovering(aJCas, Word.class, clazz);

        Map<T, Word> token2Word = new HashMap<T, Word>();

        for (Word word : JCasUtil.select(aJCas, Word.class)) {
            if (word.getBegin() == 0 && word.getEnd() == 0 || word.getBegin() < 0)
                continue;

            Collection<T> coveredTokens = wordCoveredToken.get(word);
            if (coveredTokens.size() > 0) {
                for (T token : coveredTokens) {
                    token2Word.put(token, word);
                }
            } else {// in case the token range is larger than the word, use its covering token
                Collection<T> coveringToken = wordCoveringToken.get(word);
                if (coveringToken.size() == 0) {
                    logger.debug(String.format("The word : %s [%d, %d] cannot be associated with a %s",
                            word.getCoveredText(), word.getBegin(), word.getEnd(), clazz.getSimpleName()));
                } else {
                    logger.debug("Use covering");
                    for (T token : coveringToken) {
                        token2Word.put(token, word);
                    }
                }
            }
        }
        return token2Word;
    }
}
