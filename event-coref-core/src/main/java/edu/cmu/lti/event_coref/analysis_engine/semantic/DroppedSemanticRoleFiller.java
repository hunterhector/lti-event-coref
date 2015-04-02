/**
 *
 */
package edu.cmu.lti.event_coref.analysis_engine.semantic;

import com.google.common.collect.Iterables;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.FanseDependencyRelation;
import edu.cmu.lti.event_coref.type.FanseToken;
import edu.cmu.lti.event_coref.utils.APLUtils;
import edu.cmu.lti.event_coref.utils.FanseDependencyUtils;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.EmptyFSList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This annotator tries to add in the dropped semantic roles back
 *
 * @author Zhengzhong Liu, Hector
 */
public class DroppedSemanticRoleFiller extends JCasAnnotator_ImplBase {

    public static final String ANNOTATOR_COMPONENT_ID = "System_"
            + DroppedSemanticRoleFiller.class.getSimpleName();
    private static final Logger logger = LoggerFactory.getLogger(DroppedSemanticRoleFiller.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(org.apache.uima.jcas.JCas)
     */
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        UimaConvenience.printProcessLog(aJCas, logger);

        Map<FanseToken, EventMention> previousMention2Token = new HashMap<FanseToken, EventMention>();

        for (EventMention evm : JCasUtil.select(aJCas, EventMention.class)) {


            if (evm.getBegin() < 0) {
                continue;
            }

            FanseToken headToken = FanseDependencyUtils.findHeadTokenFromDependency(evm);

            // only apply for those agents unknown
            if (evm.getAgentLinks() instanceof EmptyFSList) {
                String eventType = evm.getEventType();

                FanseDependencyRelation headDependency = Iterables.get(FSCollectionFactory.create(
                        headToken.getHeadDependencyRelations(), FanseDependencyRelation.class), 0);
                FanseToken parentToken = headDependency.getHead();
                FanseToken currentToken = headToken;

                while (parentToken != null) {

                    EventMention parentEvm = previousMention2Token.get(parentToken);

                    if (parentEvm != null) {
                        String parentEventType = parentEvm.getEventType();

                        // during the flow, assign the first concrete event type occurred to unknown events
                        if (eventType.equals("other")) {
                            eventType = parentEventType;
                        }

                        if (!parentEventType.equals(eventType)) {
                            // if the two types don't match, do not transfer agent, we consider the flow breaks
                            // here
                            break;
                        } else if (!(parentEvm.getAgentLinks() instanceof EmptyFSList)) {
                            APLUtils.copyAgents(aJCas, parentEvm, evm, ANNOTATOR_COMPONENT_ID);
                            logger.debug("Transfer agent from " + parentEvm.getCoveredText() + " to "
                                    + evm.getCoveredText());
                            break;
                        }
                    }
                    currentToken = parentToken;
                    parentToken = Iterables.get(
                            FSCollectionFactory.create(currentToken.getHeadDependencyRelations(),
                                    FanseDependencyRelation.class), 0).getHead();
                }

            }

            previousMention2Token.put(headToken, evm);

        }
    }
}
