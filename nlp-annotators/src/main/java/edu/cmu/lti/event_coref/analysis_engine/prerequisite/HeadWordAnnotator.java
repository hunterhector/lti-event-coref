package edu.cmu.lti.event_coref.analysis_engine.prerequisite;

import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.Word;
import edu.cmu.lti.event_coref.util.EventMentionUtils;
import edu.cmu.lti.event_coref.util.FanseDependencyUtils;
import edu.cmu.lti.util.uima.UimaConvenience;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This analysis engine simply annotates a head word for each event mention using FANSE
 * dependencies.
 */
public class HeadWordAnnotator extends JCasAnnotator_ImplBase {

    private static final Logger logger = LoggerFactory.getLogger(HeadWordAnnotator.class);


    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        UimaConvenience.printProcessLog(aJCas);

        for (EventMention evm : EventMentionUtils.getNonImplicitEvents(aJCas)) {
            Word headWord = FanseDependencyUtils.findHeadWordFromDependency(evm);
            if (headWord == null) {
                logger.warn("Not found a head word for the event mention: " + evm.getCoveredText());
            }

            evm.setHeadWord(headWord);
        }
    }

}
