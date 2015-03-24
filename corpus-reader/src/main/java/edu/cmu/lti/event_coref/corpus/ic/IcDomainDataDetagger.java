package edu.cmu.lti.event_coref.corpus.ic;

import edu.cmu.lti.event_coref.util.io.PlainTextUtils;
import edu.cmu.lti.util.uima.BaseAnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.descriptor.ConfigurationParameter;

public class IcDomainDataDetagger extends BaseAnalysisEngine {
    private static final Logger logger = LoggerFactory.getLogger(IcDomainDataDetagger.class);

    public static final String PARAM_INPUT_VIEW_NAME = "InputViewName";

    public static final String PARAM_GOLD_STANDARD_VIEW_NAME = "GoldStandardViewName";

    @ConfigurationParameter(name = PARAM_INPUT_VIEW_NAME, mandatory = true)
    private String inputViewName;

    @ConfigurationParameter(name = PARAM_GOLD_STANDARD_VIEW_NAME, mandatory = true, description = "This view is going to store golden standard")
    private String goldStandardViewName;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        JCas inputView = null;
        JCas goldStandardView = null;
        try {
            goldStandardView = aJCas.getView(goldStandardViewName);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        try {
            inputView = aJCas.getView(inputViewName);
        } catch (Exception e) {
            logger.error("The view for original documents is not specified.");
            throw new AnalysisEngineProcessException(e);
        }

        String originalText = inputView.getDocumentText();

        // Removes the article date from the original document.
        String datatimeTagAnn = PlainTextUtils.findTagAnnotation(originalText, "DATETIME", false);
        String originalTextWithoutDatetime = PlainTextUtils.replaceWithWhiteSpace(originalText,
                datatimeTagAnn);

        String detaggedText = PlainTextUtils.detag(originalTextWithoutDatetime);

        aJCas.setDocumentText(detaggedText);

        // Add the same text to both the default view and the gold standard view
        goldStandardView.setDocumentText(detaggedText);
    }

}
