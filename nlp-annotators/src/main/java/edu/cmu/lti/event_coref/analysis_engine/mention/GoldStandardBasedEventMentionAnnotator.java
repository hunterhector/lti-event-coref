package edu.cmu.lti.event_coref.analysis_engine.mention;

import edu.cmu.lti.event_coref.type.EventMention;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

/**
 * This is a rather simple annotator that only annotate event mention from the gold standard to the
 * default view. Cluster information is not annotated, event type is also not annotated, we expect
 * to have some other system to do these. This is a fundamental step so we wanna keep it simple, if
 * you would like to annotate event type from gold standard,write another annotater instead
 * 
 * @author Zhengzhong Liu, Hector
 * 
 */
public class GoldStandardBasedEventMentionAnnotator extends JCasAnnotator_ImplBase {
  private final String ANNOTATOR_COMPONENT_ID = "System-auto-EventMention";

  public static final String PARAM_GOLD_STANDARD_VIEWNAME = "GoldStandardViewName";

  public static final String PARAM_USE_DEFAULT_EVENT_TYPE = "UseDefaultGold";

  private final String DEFAULT_EVNET_TYPE = "event";

  @ConfigurationParameter(mandatory = true, name = PARAM_GOLD_STANDARD_VIEWNAME)
  private String goldViewName;

  @ConfigurationParameter(mandatory = true, name = PARAM_USE_DEFAULT_EVENT_TYPE)
  private boolean useDefaultEventType;

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    try {
      JCas goldStandardView = aJCas.getView(goldViewName);

      int indexCount = 0;
      for (EventMention evmGold : JCasUtil.select(goldStandardView, EventMention.class)) {
        EventMention evm = new EventMention(aJCas);
        evm.addToIndexes();
        evm.setComponentId(ANNOTATOR_COMPONENT_ID);
        if (useDefaultEventType) {
          System.err
                  .println("WARN: Using default event types for all events, disable this if you need to");
          evm.setEventType(DEFAULT_EVNET_TYPE);
        } else {
          evm.setEventType(evmGold.getEventType());
        }
        evm.setBegin(evmGold.getBegin());
        evm.setEnd(evmGold.getEnd());

        evm.setGoldStandardEventMentionId(Integer.toString(indexCount)); // this id does not seems
                                                                         // to be that useful

        indexCount++;
      }

    } catch (CASException e) {
      e.printStackTrace();
    }

  }
}
