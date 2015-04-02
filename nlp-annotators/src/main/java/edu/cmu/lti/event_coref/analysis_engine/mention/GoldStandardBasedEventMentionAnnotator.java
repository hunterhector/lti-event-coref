package edu.cmu.lti.event_coref.analysis_engine.mention;

import edu.cmu.lti.event_coref.type.EventMention;
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

/**
 * This is a rather simple annotator that only annotate event mention from the gold standard to the
 * default view. Cluster information is not annotated, event type is also not annotated, we expect
 * to have some other system to do these. This is a fundamental step so we wanna keep it simple, if
 * you would like to annotate event type from gold standard,write another annotater instead
 *
 * @author Zhengzhong Liu, Hector
 */
public class GoldStandardBasedEventMentionAnnotator extends JCasAnnotator_ImplBase {
    private static final String ANNOTATOR_COMPONENT_ID = "Gold-Standard-EventMention";

    public static final String PARAM_GOLD_STANDARD_VIEWNAME = "GoldStandardViewName";

    @ConfigurationParameter(mandatory = true, name = PARAM_GOLD_STANDARD_VIEWNAME)
    private String goldViewName;

    private static final Logger logger = LoggerFactory.getLogger(GoldStandardBasedEventMentionAnnotator.class);

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException {
        super.initialize(aContext);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        try {
            JCas goldStandardView = aJCas.getView(goldViewName);

            int indexCount = 0;
            for (EventMention evmGold : JCasUtil.select(goldStandardView, EventMention.class)) {
                EventMention evm = new EventMention(aJCas);
                evm.setComponentId(ANNOTATOR_COMPONENT_ID);
                evm.setEventType(evmGold.getEventType());
                evm.setBegin(evmGold.getBegin());
                evm.setEnd(evmGold.getEnd());
                evm.setGoldStandardEventMentionId(Integer.toString(indexCount));
                evm.addToIndexes();
                indexCount++;
            }

            logger.debug(String.format("Number of gold standard mention : %d", JCasUtil.select(goldStandardView, EventMention.class).size()));
            logger.debug(String.format("Number of system  mention : %d", JCasUtil.select(aJCas, EventMention.class).size()));
        } catch (CASException e) {
            e.printStackTrace();
        }

    }
}
