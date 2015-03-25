package edu.cmu.lti.event_coref.analysis_engine.prerequisite;

import edu.cmu.lti.event_coref.type.Sentence;
import edu.cmu.lti.event_coref.type.StanfordCorenlpSentence;
import edu.cmu.lti.event_coref.type.StanfordCorenlpToken;
import edu.cmu.lti.event_coref.type.Word;
import edu.cmu.lti.util.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This annotator try to annotate basic language units such as Sentence and Word
 * as a general type in our system. It relies on related annotations. Currently
 * the Standford Corenlp annotation will be used
 *
 * @author Zhengzhong Liu, Hector
 */

public class StanfordCorenlpBasedBasicLanguageUnitAnnotator extends
        JCasAnnotator_ImplBase {
    private static final Logger logger = LoggerFactory.getLogger(StanfordCorenlpBasedBasicLanguageUnitAnnotator.class);

    public final static String PARAM_ANNOTATION_VIEWNAMES = "annotationViewNames";

    @ConfigurationParameter(name = PARAM_ANNOTATION_VIEWNAMES, mandatory = true)
    List<String> annotationViewNames;

    // class name is too long to use
    private final String ANNOTATOR_COMPONENT_ID = "System-Standford-Autounit";

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException {
        super.initialize(aContext);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        logger.info(String.format("Processing article: %s",
                UimaConvenience.getShortDocumentName(aJCas)));

        for (String annotationViewName : annotationViewNames) {
            JCas viewToAnnotate = null;
            try {
                logger.info("Writing basic language units also for "
                        + annotationViewName);
                viewToAnnotate = aJCas.getView(annotationViewName);
            } catch (CASException e) {
                e.printStackTrace();
            }

            if (viewToAnnotate != null) {
                // 1. Annotate word to default view
                annotateWords(aJCas, viewToAnnotate);
                // 2. Annotate the Sentence type
                annotateSentences(aJCas, viewToAnnotate);
            }
        }
    }

    private void annotateWords(JCas aJCas, JCas viewToAnnotate) {
        // 1. Annotate the Word type
        int indexCount = 1;

        if (JCasUtil.select(viewToAnnotate, Word.class).isEmpty()) {
            for (StanfordCorenlpToken token : JCasUtil.select(aJCas,
                    StanfordCorenlpToken.class)) {
                annotateWord(viewToAnnotate, token.getBegin(), token.getEnd(),
                        token.getPos(), token.getLemma(), indexCount);
                indexCount++;
            }
        }
    }

    private void annotateWord(JCas aJCas, int begin, int end, String pos,
                              String lemma, int wordId) {
        Word word = new Word(aJCas);
        word.setComponentId(ANNOTATOR_COMPONENT_ID);
        word.setBegin(begin);
        word.setEnd(end);
        word.setPartOfSpeech(pos);
        // remove trailing backslash from lemma
        if (lemma.endsWith("\\")) {
            lemma = lemma.substring(0, lemma.length() - 1);
        }
        word.setLemma(lemma);

        word.setWordId(Integer.toString(wordId));
        word.addToIndexes();
    }

    private void annotateSentences(JCas aJCas, JCas viewToAnnotate) {
        int indexCount = 1;

        if (JCasUtil.select(viewToAnnotate, Sentence.class).isEmpty()) {
            for (StanfordCorenlpSentence stanfordSent : JCasUtil.select(aJCas,
                    StanfordCorenlpSentence.class)) {
                annotateSentence(viewToAnnotate, stanfordSent.getBegin(),
                        stanfordSent.getEnd(), indexCount);
                indexCount++;
            }
        }
    }

    private void annotateSentence(JCas aJCas, int begin, int end, int SentId) {
        Sentence sent = new Sentence(aJCas);
        sent.addToIndexes();
        sent.setComponentId(ANNOTATOR_COMPONENT_ID);
        sent.setBegin(begin);
        sent.setEnd(end);
        sent.setSentenceId(Integer.toString(SentId));
    }

}
