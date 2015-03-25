/**
 *
 */
package edu.cmu.lti.event_coref.analysis_engine.prerequisite;

import edu.cmu.lti.event_coref.type.Word;
import edu.washington.cs.knowitall.morpha.MorphaStemmer;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.util.JCasUtil;

/**
 * There is a problem while matching words, the morphological changes of words
 * make it hard to compare wordnet similarities, for example, similarity between
 * an adj and a noun could be difficult. Unifying the morphological change could
 * be beneficial.
 *
 * @author Zhengzhong Liu, Hector
 */
public class WordMorphaAnnotator extends JCasAnnotator_ImplBase {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(org
     * .apache.uima.jcas.JCas)
     */
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        for (Word word : JCasUtil.select(aJCas, Word.class)) {
            String wordText = word.getCoveredText();
//            String posTag = word.getPartOfSpeech();

            String morpha;
            if (wordText.contains(" ")) {
                // morpha cannot handle space
                morpha = word.getLemma();
            } else {
                morpha = MorphaStemmer.stemToken(wordText);
            }
//            if (!wordText.equals(morpha))
//                System.out.println(String.format(
//                        "From lemma %s to morpha %s, pos %s", wordText, morpha,
//                        posTag));
            word.setMorpha(morpha);
        }
    }

}
