/**
 *
 */
package edu.cmu.lti.event_coref.analysis_engine.prerequisite;

import edu.cmu.lti.event_coref.type.Article;
import edu.cmu.lti.event_coref.type.Paragraph;
import edu.cmu.lti.event_coref.type.Sentence;
import edu.cmu.lti.event_coref.type.Word;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
//import org.uimafit.component.JCasAnnotator_ImplBase;
//import org.uimafit.descriptor.ConfigurationParameter;
//import org.uimafit.util.JCasUtil;

/**
 * Copy GoldStandard information if exists
 */
public class GoldStandardBasedBasicLanguageUnitAnnotator extends JCasAnnotator_ImplBase {
    private final String ANNOTATOR_COMPONENT_ID = "GoldStandard";

    public static final String PARAM_GOLD_STANDARD_VIEWNAME = "GoldStandardViewName";

    @ConfigurationParameter(mandatory = true, name = PARAM_GOLD_STANDARD_VIEWNAME)
    private String goldViewName;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        JCas goldStandardView = null;
        try {
            goldStandardView = aJCas.getView(goldViewName);

        } catch (CASException e) {
            e.printStackTrace();
        }

        if (goldStandardView != null) {
            copyWords(goldStandardView, aJCas);
            copySentences(goldStandardView, aJCas);
            copyParagraph(goldStandardView, aJCas);
            copyArticle(goldStandardView, aJCas);
        }
    }

    private void copyWords(JCas fromView, JCas toView) {
        for (Word goldWord : JCasUtil.select(fromView, Word.class)) {
            Word word = new Word(toView);
            word.addToIndexes();
            word.setComponentId(ANNOTATOR_COMPONENT_ID);
            word.setBegin(goldWord.getBegin());
            word.setEnd(goldWord.getEnd());
            word.setLemma(goldWord.getLemma());
            word.setElliptical(goldWord.getElliptical());
            word.setPartOfSpeech(goldWord.getPartOfSpeech());
            word.setWordId(goldWord.getWordId());
        }
    }

    private void copySentences(JCas fromView, JCas toView) {
        for (Sentence goldSent : JCasUtil.select(fromView, Sentence.class)) {
            Sentence sent = new Sentence(toView);
            sent.addToIndexes();
            sent.setComponentId(ANNOTATOR_COMPONENT_ID);
            sent.setBegin(goldSent.getBegin());
            sent.setEnd(goldSent.getEnd());
            sent.setSentenceId(goldSent.getSentenceId());
        }
    }

    private void copyParagraph(JCas fromView, JCas toView) {
        for (Paragraph goldPara : JCasUtil.select(fromView, Paragraph.class)) {
            Paragraph para = new Paragraph(toView);
            para.addToIndexes();
            para.setComponentId(ANNOTATOR_COMPONENT_ID);
            para.setBegin(goldPara.getBegin());
            para.setEnd(goldPara.getEnd());
            para.setParagraphId(goldPara.getParagraphId());
        }
    }

    private void copyArticle(JCas fromView, JCas toView) {
        for (Article goldArt : JCasUtil.select(fromView, Article.class)) {
            Article article = new Article(toView);
            article.addToIndexes();
            article.setComponentId(ANNOTATOR_COMPONENT_ID);
            article.setBegin(goldArt.getBegin());
            article.setEnd(goldArt.getEnd());
            article.setArticleDate(goldArt.getArticleDate());
            article.setArticleId(goldArt.getArticleId());
            article.setArticleName(goldArt.getArticleName());
            article.setLanguage(goldArt.getArticleName());
        }
    }

}
