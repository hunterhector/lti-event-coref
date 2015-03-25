/**
 *
 */
package edu.cmu.lti.event_coref.analysis_engine.mention;

import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.util.uima.UimaConvenience;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

/**
 * Annotate all event mentions as given type name, this class is only here to
 * avoid 'null' in event type name, which is sometimes required by downstream
 * applications
 *
 * @author Zhengzhong Liu, Hector
 */
public class TrivialEventTypeAnnotator extends JCasAnnotator_ImplBase {

    public static final String PARAM_GIVEN_TYPE_NAME = "givenTypeName";

    public static final String PARAM_OTHER_VIEW_TO_ANNOTATE = "otherViewToAnnotate";

    @ConfigurationParameter(name = PARAM_GIVEN_TYPE_NAME, mandatory = true)
    String givenTypeName;

    @ConfigurationParameter(name = PARAM_OTHER_VIEW_TO_ANNOTATE)
    String otherViewToAnnotate;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(org
     * .apache.uima.jcas.JCas)
     */
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        UimaConvenience.printProcessLog(aJCas);

        for (EventMention evm : JCasUtil.select(aJCas, EventMention.class)) {
            if (evm.getEventType() == null) {
                evm.setEventType(givenTypeName);
            }
        }

        if (otherViewToAnnotate != null) {
            try {
                JCas otherView = aJCas.getView(otherViewToAnnotate);
                for (EventMention evm : JCasUtil.select(otherView,
                        EventMention.class)) {
                    if (evm.getEventType() == null) {
                        evm.setEventType(givenTypeName);
                    }
                }
            } catch (CASException e) {
                e.printStackTrace();
            }
        }

    }

}
