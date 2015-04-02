package edu.cmu.lti.event_coref.analysis_engine.semantic;

import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.APLUtils;
import edu.cmu.lti.event_coref.utils.EventCoreferenceConstants;
import edu.cmu.lti.event_coref.utils.UimaJava5;
import edu.cmu.lti.utils.model.Span;
import edu.cmu.lti.utils.type.ComponentAnnotation;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Zhengzhong Liu, Hector
 *         <p/>
 *         This annotator use Xcomp to share agent and patient
 *         <p/>
 *         Note that from Stanford Dependencies Mannual, purpcl is currently not distinguished from
 *         Xcomp in the system
 */
public class APLFillerByXcompSharingAnnotator extends JCasAnnotator_ImplBase {
    private static final String ANNOTATOR_COMPONENT_ID = "System-Xcomp-sharing";

    private static final Logger logger = LoggerFactory.getLogger(APLFillerByXcompSharingAnnotator.class);


    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        // TODO: We can use xcomp from Fanse semantic analysis
        logger.info(String.format("Processing article: %s with [%s]",
                UimaConvenience.getShortDocumentName(aJCas), this.getClass().getSimpleName()));

        Map<Word, EventMention> word2EventMention = new HashMap<Word, EventMention>();
        Map<Word, EntityMention> word2EntityMention = new HashMap<Word, EntityMention>();

        for (EventMention eevm : JCasUtil.select(aJCas, EventMention.class)) {
            for (Word word : JCasUtil.selectCovered(Word.class, eevm)) {
                word2EventMention.put(word, eevm);
            }
        }

        Set<Span> newAgentController = new HashSet<Span>();

        for (EntityMention em : JCasUtil.select(aJCas, EntityMention.class)) {
            for (Word word : JCasUtil.selectCovered(Word.class, em)) {
                word2EntityMention.put(word, em);
            }
        }

        for (StanfordDependencyRelation sdr : JCasUtil.select(aJCas, StanfordDependencyRelation.class)) {
            if (sdr.getRelationType().equals("xcomp")) {
                StanfordDependencyNode head = sdr.getHead();
                StanfordDependencyNode child = sdr.getChild();
                Word headWord = JCasUtil.selectCovered(Word.class, head).get(0);
                Word childWord = JCasUtil.selectCovered(Word.class, child).get(0);

                if (word2EventMention.containsKey(headWord) && word2EventMention.containsKey(childWord)) {
                    EventMention headEventMention = word2EventMention.get(headWord);
                    EventMention childEventMention = word2EventMention.get(childWord);

                    if (headEventMention.getEventType().equals(EventCoreferenceConstants.EventType.REPORTING.name())){
                        //should not transfer event from reporting to its child
                        continue;
                    }

                    ComponentAnnotation subjectCandidate = detectSubjectBetween(aJCas, headWord, childWord);

                    if (UimaJava5.fsListSize(childEventMention.getAgentLinks()) == 0) {
                        // 1. if there are no subject candidates for the child event,
                        // they can share the agent
                        if (subjectCandidate == null) {
                            Collection<EntityBasedComponentLink> headAgentLinks = FSCollectionFactory.create(
                                    headEventMention.getAgentLinks(), EntityBasedComponentLink.class);
                            List<EntityBasedComponentLink> childAgentLinks = new ArrayList<EntityBasedComponentLink>(
                                    headAgentLinks.size());
                            for (EntityBasedComponentLink headAgentLink : headAgentLinks) {
                                EntityBasedComponent headAgent = headAgentLink.getComponent();
                                EntityBasedComponentLink childAgentLink = APLUtils.createLink(aJCas,
                                        childEventMention, headAgent, APLUtils.AGENT_LINK_TYPE,
                                        ANNOTATOR_COMPONENT_ID);
                                headAgent.setComponentLinks(UimaConvenience.appendFSList(aJCas,
                                        headAgent.getComponentLinks(), childAgentLink,
                                        EntityBasedComponentLink.class));
                                childAgentLinks.add(childAgentLink);
                            }
                            childEventMention.setAgentLinks(UimaConvenience.appendAllFSList(aJCas,
                                    childEventMention.getAgentLinks(), childAgentLinks,
                                    EntityBasedComponentLink.class));
                        } else {
                            // 2. if there are some candidates, we probably need to get the noun
                            // phrase as subject (thus agent), possibly with
                            // some other conditions
                            int begin = subjectCandidate.getBegin();
                            int end = subjectCandidate.getEnd();
                            Span candSpan = new Span(begin, end);
                            if (newAgentController.contains(candSpan))
                                continue;

                            newAgentController.add(candSpan);

                            logger.debug(String.format("Candidate as agent [%s] for event [%d-%s] ",
                                    subjectCandidate.getCoveredText(), childEventMention.getEventMentionIndex(),
                                    childEventMention.getCoveredText()));

                            List<EntityBasedComponent> existingEntities = JCasUtil.selectCovered(
                                    EntityBasedComponent.class, subjectCandidate);

                            EntityBasedComponent agentToAttach;
                            if (existingEntities.size() == 0) {
                                agentToAttach = new EntityBasedComponent(aJCas, begin, end);
                                agentToAttach.addToIndexes(aJCas);
                                agentToAttach.setComponentId(ANNOTATOR_COMPONENT_ID);
                            } else {
                                agentToAttach = existingEntities.get(0);
                            }

                            EntityBasedComponentLink agentLink = APLUtils.createLink(aJCas, childEventMention,
                                    agentToAttach, APLUtils.AGENT_LINK_TYPE, ANNOTATOR_COMPONENT_ID);
                            agentToAttach.setComponentLinks(UimaConvenience.appendFSList(aJCas,
                                    agentToAttach.getComponentLinks(), agentLink,
                                    EntityBasedComponentLink.class));
                            childEventMention.setAgentLinks(UimaConvenience.appendFSList(aJCas,
                                    childEventMention.getAgentLinks(), agentLink,
                                    EntityBasedComponentLink.class));
                        }
                    }
                }
            }
        }

    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        super.collectionProcessComplete();
    }

    private ComponentAnnotation detectSubjectBetween(JCas aJCas, Word head, Word child) {
        int begin = head.getEnd();
        int end = child.getBegin();
        List<OpennlpChunk> chunks = JCasUtil.selectCovered(aJCas, OpennlpChunk.class, begin, end);

        // Currently I adopt a simple solution, if there is an noun pharse
        // between them which satisfies
        // condition 1: is specific type of entity
        // condition 2: have dependencies with the head verb

        for (OpennlpChunk chunk : chunks) {
            int conditionCount = 0;
            if (chunk.getTag().equals("NP")) {
                for (EntityMention em : JCasUtil.selectCovered(EntityMention.class, chunk)) {
                    String entityType = em.getEntityType();
                    if ((entityType != null && entityType.equals("PER") && entityType.equals("ORG"))) {
                        conditionCount++;
                    }
                }
            }

            StanfordDependencyNode headNode = JCasUtil.selectCovered(aJCas, StanfordDependencyNode.class,
                    head).get(0);

            for (StanfordDependencyNode node : JCasUtil.selectCovered(aJCas,
                    StanfordDependencyNode.class, chunk)) {
                for (StanfordDependencyRelation headRelation : FSCollectionFactory.create(
                        node.getHeadRelations(), StanfordDependencyRelation.class)) {
                    if (headRelation.getHead().equals(headNode)) {
                        conditionCount++;
                    }
                }
            }

            if (conditionCount >= 2)
                return chunk;
        }

        return null;
    }

}
