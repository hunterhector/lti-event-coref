package edu.cmu.lti.event_coref.analysis_engine.semantic;

import com.google.common.collect.Iterables;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.APLUtils;
import edu.cmu.lti.event_coref.utils.EventMentionUtils;
import edu.cmu.lti.event_coref.utils.FanseDependencyUtils;
import edu.cmu.lti.event_coref.utils.UimaJava5;
import edu.cmu.lti.utils.model.Span;
import edu.cmu.lti.utils.type.ComponentAnnotation;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * An annotator that uses possible resources to detect agent and patient, such as syntatic parsing,
 * semantic parsing, and with the combination of some rules. For nominal events which lack of
 * semantic role labeling, some rules are use, similar but richer than the Stanford system method.
 * The purpose of this annotator is to extract more reliable agent, patients. Rules here are
 * supposed to be more accurate than other annotators.
 * <p/>
 * TODO refactors needed to make this more readable
 * This code is going to be replaced by modularized parts
 *
 * @author Zhengzhong Liu, Hector
 */
public class IntegratedAgentPatientAnnotator extends JCasAnnotator_ImplBase {

    public static final String PARAM_TARGET_EVENT_TYPE = "targetEventType";

    @ConfigurationParameter(name = PARAM_TARGET_EVENT_TYPE)
    private String targetEventType;

    private static final String ANNOTATOR_COMPONENT_ID = "System-integerated-agent-patient";

    private Map<Word, EntityBasedComponent> word2EntityIndex;

    private Map<Span, Word> fanseToken2Word;

    private Map<Span, Word> stanfordToken2Word;

    private static final Logger logger = LoggerFactory.getLogger(IntegratedAgentPatientAnnotator.class);

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        logger.info(String.format("Processing article: %s with [%s]",
                UimaConvenience.getShortDocumentName(aJCas), this.getClass().getSimpleName()));

        // 0. preparation
        fanseToken2Word = new HashMap<Span, Word>();
        stanfordToken2Word = new HashMap<Span, Word>();

        Collection<EventMention> allEvents = JCasUtil.select(aJCas, EventMention.class);
        Map<Word, EventMention> word2Events = new HashMap<Word, EventMention>();
        for (EventMention eevm : allEvents) {
            for (Word word : JCasUtil.selectCovered(Word.class, eevm)) {
                word2Events.put(word, eevm);
            }
        }

        for (StanfordToken2WordAlignment align : JCasUtil.select(aJCas,
                StanfordToken2WordAlignment.class)) {
            stanfordToken2Word.put(toSpan(align.getToken()), align.getWord());
        }

        for (FanseToken2WordAlignment align : JCasUtil.select(aJCas, FanseToken2WordAlignment.class)) {
            fanseToken2Word.put(toSpan(align.getToken()), align.getWord());
        }

        // System.out.println(fanseToken2Word.size());

        // create a index from word to chunks
        // we assume chunks are non-overlapped.
        Collection<ExtendedNPChunk> npChunks = JCasUtil.select(aJCas, ExtendedNPChunk.class);
        Map<Word, ExtendedNPChunk> word2NPChunkIndex = new HashMap<Word, ExtendedNPChunk>();
        for (ExtendedNPChunk chunk : npChunks) {
            for (Word word : JCasUtil.selectCovered(Word.class, chunk)) {
                word2NPChunkIndex.put(word, chunk);
            }
        }

        // create a index from word to the short chunks
        Collection<OpennlpChunk> shortNpChunks = JCasUtil.select(aJCas, OpennlpChunk.class);
        Map<Word, OpennlpChunk> wrod2ShortNpChunkIndex = new HashMap<Word, OpennlpChunk>();
        for (OpennlpChunk chunk : shortNpChunks) {
            for (Word word : JCasUtil.selectCovered(Word.class, chunk)) {
                wrod2ShortNpChunkIndex.put(word, chunk);
            }
        }

        // find NP pairs by conj_and from FanseDependencies
        // put the pair of conjunction words into a map
        Collection<FanseToken> allFanseTokens = JCasUtil.select(aJCas, FanseToken.class);
        Map<Word, Word> ccWordCrossOverIndex = new HashMap<Word, Word>();

        word2EntityIndex = new HashMap<Word, EntityBasedComponent>();

        for (FanseToken fanseToken : allFanseTokens) {
            if (fanseToken.getCoveredText().toLowerCase().equals("and")
                    && fanseToken.getPos().equals("CC")) {
                Word conjChildHeadWord = null;
                Word ccHeadHeadWord = null;
                ExtendedNPChunk ccHeadNP = null;
                ExtendedNPChunk conjChildNP = null;

                FanseToken andToken = fanseToken;// just make code make semantic sense
                FSList andChildDependenciesFS = andToken.getChildDependencyRelations();
                if (andChildDependenciesFS != null) {
                    for (FanseDependencyRelation andChildDependency : FSCollectionFactory.create(
                            andChildDependenciesFS, FanseDependencyRelation.class)) {
                        if (andChildDependency.getDependency().equals("conj")) {
                            FanseToken andChildToken = andChildDependency.getChild();
                            Word andChildHeadWord = anyToken2Word(andChildToken);
                            conjChildHeadWord = andChildHeadWord;
                            conjChildNP = word2NPChunkIndex.get(andChildHeadWord);
                            break; // assume it only get one
                        }
                    }
                }

                FSList andHeadDependenciesFS = andToken.getHeadDependencyRelations();
                if (andHeadDependenciesFS != null) {
                    for (FanseDependencyRelation andHeadDependency : FSCollectionFactory.create(
                            andHeadDependenciesFS, FanseDependencyRelation.class)) {
                        if (andHeadDependency.getDependency().equals("cc")) {
                            FanseToken andHeadToken = andHeadDependency.getHead();
                            Word andHeadWord = anyToken2Word(andHeadToken);
                            ccHeadHeadWord = andHeadWord;
                            ccHeadNP = word2NPChunkIndex.get(andHeadWord);
                            break;// assum in only have one
                        }
                    }
                }

                if (conjChildNP != null && ccHeadNP != null) {
                    for (Word ccHead : JCasUtil.selectCovered(Word.class, ccHeadNP)) {
                        for (Word conjChild : JCasUtil.selectCovered(Word.class, conjChildNP)) {
                            ccWordCrossOverIndex.put(ccHead, conjChild);
                            ccWordCrossOverIndex.put(conjChild, ccHead);
                            // System.out.println(ccHead.getCoveredText() + " " + conjChild.getCoveredText());
                        }
                    }
                } else if (conjChildHeadWord != null && ccHeadHeadWord != null) {
                    ccWordCrossOverIndex.put(conjChildHeadWord, ccHeadHeadWord);
                    ccWordCrossOverIndex.put(ccHeadHeadWord, conjChildHeadWord);
                    // System.out.println(conjChildHeadWord.getCoveredText() + " "
                    // + ccHeadHeadWord.getCoveredText());
                }
            }
        }

        // step 1.1: use Fanse Semantic Annotations and Syntactic Annotations to find agent, patient for
        // verb based events
        Collection<FanseSemanticRelation> fanseSemanticRelations = JCasUtil.select(aJCas,
                FanseSemanticRelation.class);

        // store links with the token, will be transform to link between agent and entity later
        // store match between agent and token
        Map<Span, ComponentAnnotation> token2AgentHead = new HashMap<Span, ComponentAnnotation>();
        // store match between patient and token
        Map<Span, ComponentAnnotation> token2PatientHead = new HashMap<Span, ComponentAnnotation>();

        Set<Span> agentSpanSet = new HashSet<Span>();
        Set<Span> patientSpanSet = new HashSet<Span>();

        for (FanseSemanticRelation fsr : fanseSemanticRelations) {
            String relation = fsr.getSemanticAnnotation();
            if (relation.startsWith("ARG0")) { // including ARG0, ARG0-INVERTED
                FanseToken eventToken = fsr.getHead();
                FanseToken agentToken = fsr.getChild();
                if (relation.equals("ARG0-INVERTED")) {// so, inverted really means inverted
                    eventToken = fsr.getChild();
                    agentToken = fsr.getHead();
                }

                Span eventTokenSpan = new Span(eventToken.getBegin(), eventToken.getEnd());

                // find if it is "by", and connect
                agentToken = findAgentConnectedByBy(agentToken);

                if (!Pattern.matches("\\p{Punct}", agentToken.getCoveredText())) {
                    // Span agentTokenSpan = new Span(agentToken.getBegin(), agentToken.getEnd());

                    // if (!agentSpanSet.contains(agentTokenSpan)) {
                    if (!token2AgentHead.containsKey(eventTokenSpan)) {
                        token2AgentHead.put(eventTokenSpan, agentToken);
                    }
                    // agentSpanSet.add(agentTokenSpan);
                    // }
                }
            }
            if (relation.startsWith("ARG1")) { // includes ARG1, ARG1-INVERTED, ARG1-REC
                FanseToken eventToken = fsr.getHead();
                FanseToken patientToken = fsr.getChild();

                if (relation.equals("ARG1-INVERTED")) {// so, inverted really means inverted
                    eventToken = fsr.getChild();
                    patientToken = fsr.getHead();
                }

                if (!Pattern.matches("\\p{Punct}", patientToken.getCoveredText())) {
                    Span eventTokenSpan = new Span(eventToken.getBegin(), eventToken.getEnd());
                    Span patientTokenSpan = new Span(patientToken.getBegin(), patientToken.getEnd());
                    // if (!patientSpanSet.contains(patientTokenSpan)) {

                    if (!token2PatientHead.containsKey(eventTokenSpan)) {
                        token2PatientHead.put(eventTokenSpan, patientToken);
                    }
                    // patientSpanSet.add(patientTokenSpan);
                    // }
                }
            }
        }

        for (EventMention evm : allEvents) {
            List<FanseToken> tokensInEvent = JCasUtil.selectCovered(FanseToken.class, evm);

            ArrayList<EntityBasedComponentLink> agentLinks = new ArrayList<EntityBasedComponentLink>();
            ArrayList<EntityBasedComponentLink> patientLinks = new ArrayList<EntityBasedComponentLink>();

            for (FanseToken token : tokensInEvent) {
                Span eventTokenSpan = new Span(token.getBegin(), token.getEnd());
                // List<ComponentAnnotation> agentHeads = token2AgentHead.get(token);
                // if (agentHeads != null) {
                // for (ComponentAnnotation headWordToken : agentHeads) {
                if (token2AgentHead.containsKey(eventTokenSpan)) {
                    ComponentAnnotation headWordToken = token2AgentHead.get(eventTokenSpan);
                    Word headWord = anyToken2Word(headWordToken);

//                    System.out.println(headWordToken.getCoveredText() + " " + headWordToken.getBegin() + " "
//                            + headWordToken.getEnd());

//                    if (headWord != null) {
//                        System.out.println(headWord.getCoveredText());
//                    }

                    Span span = getNonOverlappingNPSpan(headWord, evm, word2NPChunkIndex);

                    EntityBasedComponent agent = getOrCreateEntityComponent(aJCas, span.getBegin(),
                            span.getEnd(), headWord);

                    addWord2EntityIndex(agent);
                    agentLinks.add(APLUtils.createLink(aJCas, evm, agent, APLUtils.AGENT_LINK_TYPE,
                            ANNOTATOR_COMPONENT_ID + "_Fanse"));
                }

                if (token2PatientHead.containsKey(eventTokenSpan)) {
                    ComponentAnnotation headWordToken = token2PatientHead.get(eventTokenSpan);

                    Word headWord = anyToken2Word(headWordToken);

                    Span span = getNonOverlappingNPSpan(headWord, evm, word2NPChunkIndex);
                    EntityBasedComponent patientLeftToAnd = getOrCreateEntityComponent(aJCas,
                            span.getBegin(), span.getEnd(), headWord);


                    addWord2EntityIndex(patientLeftToAnd);
                    patientLinks.add(APLUtils.createLink(aJCas, evm, patientLeftToAnd,
                            APLUtils.PATIENT_LINK_TYPE, ANNOTATOR_COMPONENT_ID + "_Fanse"));
                }
                // }
                // }
            }
            evm.setAgentLinks(FSCollectionFactory.createFSList(aJCas, agentLinks));
            evm.setPatientLinks(FSCollectionFactory.createFSList(aJCas, patientLinks));
        }

        // step 2: use more rules to find agents for events (especially nominal)
        // step 2.1 Use different "prep" relations (replace stanford dep with Fanse dep)
        for (Sentence sent : JCasUtil.select(aJCas, Sentence.class)) {
            List<FanseToken> fTokensInSent = JCasUtil.selectCovered(FanseToken.class, sent);
            for (int i = 0; i < fTokensInSent.size(); i++) {
                FanseToken prepToken = fTokensInSent.get(i);
                if (prepToken.getCoveredText().equalsIgnoreCase("between")) {
                    FSList betweenHeadDependenciesFS = prepToken.getHeadDependencyRelations();
                    if (betweenHeadDependenciesFS != null) {
                        for (FanseDependencyRelation betweenHeadDependency : FSCollectionFactory.create(
                                betweenHeadDependenciesFS, FanseDependencyRelation.class)) {
                            if (betweenHeadDependency.getDependency().equals("prep")) {
                                Word betweenHeadWord = anyToken2Word(betweenHeadDependency.getHead());
                                // expand the word to its chunck.Sometimes the head attachment is on other word in
                                // the chunk
                                ExtendedNPChunk betweenHeadChunk = word2NPChunkIndex.get(betweenHeadWord);
                                List<Word> betweenHeadWordChunkWords;
                                if (betweenHeadChunk != null) {
                                    betweenHeadWordChunkWords = JCasUtil.selectCovered(Word.class, betweenHeadChunk);
                                } else {
                                    betweenHeadWordChunkWords = Arrays.asList(betweenHeadWord);
                                }
                                boolean isLinkedToEvent = false;
                                EventMention betweenEvm = null;
                                for (Word betweenHeadWordChunkWord : betweenHeadWordChunkWords) {
                                    if (word2Events.containsKey(betweenHeadWordChunkWord)) {
                                        isLinkedToEvent = true;
                                        betweenEvm = word2Events.get(betweenHeadWordChunkWord);
                                    }
                                }

                                if (isLinkedToEvent) {
                                    // get the between part first
                                    EntityBasedComponent betweenAgent = null;
                                    FSList betweenChildDependenciesFS = prepToken.getChildDependencyRelations();
                                    if (betweenChildDependenciesFS != null) {
                                        for (FanseDependencyRelation betweenChildDependency : FSCollectionFactory
                                                .create(betweenChildDependenciesFS, FanseDependencyRelation.class)) {
                                            if (betweenChildDependency.getDependency().equals("pobj")) {
                                                FanseToken betweenChildToken = betweenChildDependency.getChild();
                                                Word betweenChildHeadWord = anyToken2Word(betweenChildToken);
                                                Span betweenChildSpan = getNonOverlappingNPSpan(betweenChildHeadWord,
                                                        betweenEvm, word2NPChunkIndex);
                                                betweenAgent = getOrCreateEntityComponent(aJCas,
                                                        betweenChildSpan.getBegin(), betweenChildSpan.getEnd(),
                                                        betweenChildHeadWord);
                                                break; // assume it only get one
                                            }
                                        }
                                    }

                                    Word andChildHeadWord = findConjHeadWord(betweenAgent, ccWordCrossOverIndex,
                                            word2NPChunkIndex);

                                    EntityBasedComponent andAgent = null;
                                    if (andChildHeadWord != null) {
                                        Span andChildSpan = getNonOverlappingNPSpan(andChildHeadWord, betweenEvm,
                                                word2NPChunkIndex);
                                        andAgent = getOrCreateEntityComponent(aJCas, andChildSpan.getBegin(),
                                                andChildSpan.getEnd(), andChildHeadWord);

                                        logger.info("And agent " + andChildHeadWord.getCoveredText() + " "
                                                + andAgent.getCoveredText());
                                    }

                                    // check and split if they system combines "between ... and ... phrase"
                                    // in which case, they are covering the same span, but should be split by "and"
                                    String splitter = " and ";

                                    List<EntityBasedComponent> splitedComponents = splitComponentByString(aJCas,
                                            betweenAgent, andAgent, splitter);
                                    if (!splitedComponents.isEmpty()) {
                                        betweenAgent = splitedComponents.get(0);
                                        andAgent = splitedComponents.get(1);
                                        logger.debug("Update to " + andAgent.getCoveredText());
                                    }

                                    // now attach the agents
                                    ArrayList<EntityBasedComponentLink> agentLinks = new ArrayList<EntityBasedComponentLink>();
                                    logger.info(betweenEvm.getCoveredText());
                                    if (betweenAgent != null)
                                        addWord2EntityIndex(betweenAgent);
                                    agentLinks.add(APLUtils.createLink(aJCas, betweenEvm, betweenAgent,
                                            APLUtils.AGENT_LINK_TYPE, ANNOTATOR_COMPONENT_ID + "_between_and"));
                                    logger.info("[between] " + betweenAgent.getCoveredText());
                                    if (andAgent != null) {
                                        addWord2EntityIndex(andAgent);
                                        logger.info("[And] " + andAgent.getCoveredText());
                                        agentLinks.add(APLUtils.createLink(aJCas, betweenEvm, andAgent,
                                                APLUtils.AGENT_LINK_TYPE, ANNOTATOR_COMPONENT_ID + "_between_and"));
                                    }
                                    betweenEvm.setAgentLinks(UimaConvenience.appendAllFSList(aJCas,
                                            betweenEvm.getAgentLinks(), agentLinks, EntityBasedComponentLink.class));
                                }
                            }
                        }
                    }
                } else if (prepToken.getCoveredText().equalsIgnoreCase("by")) {
                    FSList byHeadDependenciesFS = prepToken.getHeadDependencyRelations();
                    if (byHeadDependenciesFS != null) {
                        for (FanseDependencyRelation byHeadDependency : FSCollectionFactory.create(
                                byHeadDependenciesFS, FanseDependencyRelation.class)) {
                            String byHeadDependencyStr = byHeadDependency.getDependency();
                            if (byHeadDependencyStr.equals("prep") || byHeadDependencyStr.equals("agent")) {
                                Word byHeadWord = anyToken2Word(byHeadDependency.getHead());

                                // Expand the word to its chunk.
                                // Sometimes the head attachment is on other word in the chunk
                                ExtendedNPChunk byHeadChunk = word2NPChunkIndex.get(byHeadWord);
                                List<Word> byHeadWordChunkWords;
                                if (byHeadChunk != null) {
                                    byHeadWordChunkWords = JCasUtil.selectCovered(Word.class, byHeadChunk);
                                } else {
                                    byHeadWordChunkWords = Arrays.asList(byHeadWord);
                                }

                                boolean isLinkedToEvent = false;
                                EventMention byEvm = null;
                                for (Word byHeadWordChunkWord : byHeadWordChunkWords) {
                                    if (word2Events.containsKey(byHeadWordChunkWord)) {
                                        isLinkedToEvent = true;
                                        byEvm = word2Events.get(byHeadWordChunkWord);
                                    }
                                }

                                if (isLinkedToEvent) {
                                    FSList byChildDependenciesFS = prepToken.getChildDependencyRelations();
                                    if (byChildDependenciesFS != null) {
                                        for (FanseDependencyRelation byChildDependency : FSCollectionFactory.create(
                                                byChildDependenciesFS, FanseDependencyRelation.class)) {
                                            if (byChildDependency.getDependency().equals("pobj")) {
                                                FanseToken byChildNode = byChildDependency.getChild();
                                                Span byChildNPSpan = getNonOverlappingNPSpan(anyToken2Word(byChildNode),
                                                        byEvm, word2NPChunkIndex);
                                                ComponentAnnotation testAnno = new ComponentAnnotation(aJCas,
                                                        byChildNPSpan.getBegin(), byChildNPSpan.getEnd());
                                                logger.info(String.format("[%s] by [%s] ", byEvm.getCoveredText(),
                                                        testAnno.getCoveredText()));

                                                EntityBasedComponent byAgent = getOrCreateEntityComponent(aJCas,
                                                        byChildNPSpan.getBegin(), byChildNPSpan.getEnd(),
                                                        anyToken2Word(byChildNode));

                                                addWord2EntityIndex(byAgent);

                                                ArrayList<EntityBasedComponentLink> agentLinks = new ArrayList<EntityBasedComponentLink>();
                                                agentLinks.add(APLUtils.createLink(aJCas, byEvm, byAgent,
                                                        APLUtils.AGENT_LINK_TYPE, ANNOTATOR_COMPONENT_ID + "_by"));

                                                // // find the conj agent if possible
                                                // Word andChildHeadWord = findConjHeadWord(byAgent, ccWordCrossOverIndex,
                                                // word2NPChunkIndex);
                                                // EntityBasedComponent andAgent = null;
                                                // if (andChildHeadWord != null) {
                                                // Span andChildSpan = getNonOverlappingNPSpan(andChildHeadWord, byEvm,
                                                // word2NPChunkIndex);
                                                // andAgent = getOrCreateEntityComponent(aJCas, andChildSpan.getBegin(),
                                                // andChildSpan.getEnd(), andChildHeadWord);
                                                // addWord2EntityIndex(andAgent);
                                                // agentLinks.add(APLUtils.createLink(aJCas, byEvm, andAgent,
                                                // APLUtils.AGENT_LINK_TYPE, ANNOTATOR_COMPONENT_ID));
                                                // }

                                                byEvm.setAgentLinks(FSCollectionFactory.createFSList(aJCas, agentLinks));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (EventMention evm : allEvents) {
            // step 2.2 If the event modify the noun phrase or the noun phrase modify the event
            List<FanseToken> eventFanseNodes = JCasUtil.selectCovered(FanseToken.class, evm);
            HashSet<FanseToken> eventFanseNodeSet = new HashSet<FanseToken>(eventFanseNodes);

            ArrayList<ComponentAnnotation> agentCandidates = new ArrayList<ComponentAnnotation>();

            for (FanseToken eventFanseNode : eventFanseNodes) {
                // where noun phrase is the modifier
                FSList eventFanseNodeChildRelationsFsList = eventFanseNode.getChildDependencyRelations();
                if (eventFanseNodeChildRelationsFsList != null) {
                    Collection<FanseDependencyRelation> eventFanseNodeChildRelations = FSCollectionFactory
                            .create(eventFanseNodeChildRelationsFsList, FanseDependencyRelation.class);

                    for (FanseDependencyRelation eventFanseNodeChildRelation : eventFanseNodeChildRelations) {
                        if (eventFanseNodeChildRelation.getDependency().endsWith("mod")) {
                            FanseToken eventFanseNodeChild = eventFanseNodeChildRelation.getChild();
                            if (!eventFanseNodeSet.contains(eventFanseNodeChild))
                                agentCandidates.add(eventFanseNodeChild);
                        }
                    }
                }

                // where event is the modifier
                FSList eventFanseNodeHeadRelationsFsList = eventFanseNode.getHeadDependencyRelations();
                if (eventFanseNodeHeadRelationsFsList != null) {
                    Collection<FanseDependencyRelation> eventFanseNodeHeadRelations = FSCollectionFactory
                            .create(eventFanseNodeHeadRelationsFsList, FanseDependencyRelation.class);

                    for (FanseDependencyRelation eventFanseNodeHeadRelation : eventFanseNodeHeadRelations) {
                        if (eventFanseNodeHeadRelation.getDependency().endsWith("mod")) {
                            FanseToken eventFanseNodeHead = eventFanseNodeHeadRelation.getHead();
                            if (!eventFanseNodeSet.contains(eventFanseNodeHead))
                                agentCandidates.add(eventFanseNodeHead);
                        }
                    }
                }
            }

            List<EntityBasedComponentLink> modAgentLinks = new ArrayList<EntityBasedComponentLink>();

            for (ComponentAnnotation agentCand : agentCandidates) {
                Word agentCandHead = FanseDependencyUtils.findHeadWordFromDependency(agentCand);
                // expand the search to noun span
                Span npChunkSpan = getNonOverlappingNPSpan(agentCandHead, evm, word2NPChunkIndex);
                List<EntityBasedComponentLink> siredAgentLinks = getAndCreateValidAgentsInRange(aJCas, evm,
                        npChunkSpan.getBegin(), npChunkSpan.getEnd(), word2NPChunkIndex,
                        ANNOTATOR_COMPONENT_ID + "_mod");
                modAgentLinks.addAll(siredAgentLinks);
            }

            if (!modAgentLinks.isEmpty()) {
                evm.setAgentLinks(UimaConvenience.appendAllFSList(aJCas, evm.getAgentLinks(),
                        modAgentLinks, EntityBasedComponentLink.class));
            }

            boolean isNominal = false;
            // we assume NP chunk covers all nouns
            for (Word word : JCasUtil.selectCovered(Word.class, evm)) {
                if (word2NPChunkIndex.containsKey(word)) {
                    isNominal = true;
                    break;
                }
            }

            if (isNominal) {
                // step 2.3 Find the possessor of the event
                List<StanfordDependencyNode> eventStanfordNodes = JCasUtil.selectCovered(
                        StanfordDependencyNode.class, evm);

                SEARCH_POSS_AGENT:
                for (StanfordDependencyNode eventStanfordNode : eventStanfordNodes) {
                    // where we find "poss"
                    FSList eventStanfrodChildRelationsFS = eventStanfordNode.getChildRelations();
                    if (eventStanfrodChildRelationsFS != null) {
                        for (StanfordDependencyRelation sdr : FSCollectionFactory.create(
                                eventStanfrodChildRelationsFS, StanfordDependencyRelation.class)) {
                            String dependencyType = sdr.getRelationType();
                            if (dependencyType.equals("poss") || dependencyType.equals("prep_of")) {
                                StanfordDependencyNode possChild = sdr.getChild();
                                Word possChildWord = Iterables
                                        .get(JCasUtil.selectCovered(Word.class, possChild), 0);

                                OpennlpChunk possChunk = wrod2ShortNpChunkIndex.get(possChildWord);

                                List<EntityBasedComponentLink> possAgentLinks = getAndCreateValidAgentsInRange(
                                        aJCas, evm, possChildWord.getBegin(), possChildWord.getEnd(),
                                        wrod2ShortNpChunkIndex, ANNOTATOR_COMPONENT_ID + "poss");

                                if (possAgentLinks.size() > 0) {
                                    logger.info("[Possessive Agent] "
                                            + possAgentLinks.get(0).getComponent().getCoveredText() + " : "
                                            + evm.getCoveredText());
                                    evm.setAgentLinks(FSCollectionFactory.createFSList(aJCas, possAgentLinks));
                                }
                                break SEARCH_POSS_AGENT;
                            }
                        }
                    }
                }

                // step 2.4 Use noun compound modifier for nominal events
                HashSet<StanfordDependencyNode> eventStanfordNodeSet = new HashSet<StanfordDependencyNode>(
                        eventStanfordNodes);// control the overlap

                for (StanfordDependencyNode eventStanfordNode : eventStanfordNodes) {
                    FSList eventStanfordNodeChildRelationsFSList = eventStanfordNode.getChildRelations();

                    if (eventStanfordNodeChildRelationsFSList != null) {
                        int begin = -1;
                        int end = -1;
                        boolean isFirst = true;
                        Collection<StanfordDependencyRelation> eventStanfordNodeChildRelations = FSCollectionFactory
                                .create(eventStanfordNodeChildRelationsFSList, StanfordDependencyRelation.class);

                        for (StanfordDependencyRelation eventStanfordNodeChildRelation : eventStanfordNodeChildRelations) {
                            StanfordDependencyNode eventStanfordNodeChild = eventStanfordNodeChildRelation
                                    .getChild();
                            if (!eventStanfordNodeSet.contains(eventStanfordNodeChild)) { // don't want to overlap
                                // with event
                                if (eventStanfordNodeChildRelation.getRelationType().equals("nn")) { // noun
                                    // compound
                                    // modifier
                                    if (isFirst) {
                                        begin = eventStanfordNodeChild.getBegin();
                                    }
                                    end = eventStanfordNodeChild.getEnd();
                                }
                            }
                        }

                        List<EntityBasedComponentLink> nnAgentLinks = getAndCreateValidAgentsInRange(aJCas,
                                evm, begin, end, word2NPChunkIndex, ANNOTATOR_COMPONENT_ID + "nn");
                        if (!nnAgentLinks.isEmpty()) {
                            evm.setAgentLinks(FSCollectionFactory.createFSList(aJCas, nnAgentLinks));
                        }
                    }
                }
            }
        }

        // step 3: propagate using prep and conj between events
        // 3.1 Use "conj" first
        Collection<EventRelation> allRelations = JCasUtil.select(aJCas, EventRelation.class);
        for (EventRelation relation : allRelations) {
            EventMention headEvent = relation.getHead();
            EventMention childEvent = relation.getChild();
            if (headEvent.equals(childEvent)) {
                logger.error(childEvent.getCoveredText() + " " + headEvent.getCoveredText());
            }

            if (EventMentionUtils.isTargetEventType(headEvent, targetEventType)
                    && EventMentionUtils.isTargetEventType(childEvent, targetEventType)) {
                String relationType = relation.getRelationType();
                if (relationType.startsWith("conj")) {
                    // Copy Agents
                    FSList headEventAgentsFSList = headEvent.getAgentLinks();
                    FSList childEventAgentsFSList = childEvent.getAgentLinks();
                    if (UimaJava5.fsListSize(headEventAgentsFSList) > 0
                            || UimaJava5.fsListSize(childEventAgentsFSList) > 0) {
                        if (UimaJava5.fsListSize(headEventAgentsFSList) == 0) {
                            APLUtils.copyAgents(aJCas, childEvent, headEvent, ANNOTATOR_COMPONENT_ID
                                    + "_event_conj");
                            logger.info("Copy from " + childEvent.getCoveredText() + " to "
                                    + headEvent.getCoveredText());
                        } else if (UimaJava5.fsListSize(childEventAgentsFSList) == 0) {
                            APLUtils.copyAgents(aJCas, headEvent, childEvent, ANNOTATOR_COMPONENT_ID
                                    + "_event_conj");
                            logger.info("Copy from " + headEvent.getCoveredText() + " to "
                                    + childEvent.getCoveredText());
                        }
                    }

                    // Copy Patients
                    FSList headEventPatientsFSList = headEvent.getPatientLinks();
                    FSList childEventPatientsFSList = childEvent.getPatientLinks();
                    if (UimaJava5.fsListSize(headEventPatientsFSList) > 0
                            || UimaJava5.fsListSize(childEventPatientsFSList) > 0) {
                        if (UimaJava5.fsListSize(headEventPatientsFSList) == 0) {
                            APLUtils.copyPatients(aJCas, childEvent, headEvent, ANNOTATOR_COMPONENT_ID
                                    + "_event_conj");
                        } else if (UimaJava5.fsListSize(childEventPatientsFSList) == 0) {
                            APLUtils.copyPatients(aJCas, headEvent, childEvent, ANNOTATOR_COMPONENT_ID
                                    + "_event_conj");
                        }
                    }

                    // In current method, no need to copy locations
                }
            }
        }
    }

    private List<EntityBasedComponent> splitComponentByString(JCas aJCas, EntityBasedComponent anno1,
                                                              EntityBasedComponent anno2, String splitter) {
        List<EntityBasedComponent> splitedComponents = new ArrayList<EntityBasedComponent>();

        if (anno1 != null && anno2 != null) {
            if (anno1.getCoveredText().equals(anno2.getCoveredText())) {
                String combinedStr = anno1.getCoveredText();
                int splitterRelativeOffset = combinedStr.indexOf(splitter);
                int splitterAbsoluteOffset = anno1.getBegin() + splitterRelativeOffset;
                if (splitterRelativeOffset != -1) {// if found the splitter string, split into two
                    EntityBasedComponent componentLeftToSplitter = getOrCreateEntityComponent(aJCas,
                            anno1.getBegin(), splitterAbsoluteOffset, anno1.getHeadWord());
                    anno1.removeFromIndexes(aJCas);
                    anno1 = componentLeftToSplitter;

                    EntityBasedComponent componentRightToSplitter = getOrCreateEntityComponent(aJCas,
                            splitterAbsoluteOffset + splitter.length(), anno2.getEnd(), anno2.getHeadWord());
                    anno2.removeFromIndexes(aJCas);
                    anno2 = componentRightToSplitter;
                }
            }
            splitedComponents.add(anno1);
            splitedComponents.add(anno2);
        }

        return splitedComponents;
    }

    /**
     * Find possible agent in given range. Multiple new agents could be created and added to JCas.
     *
     * @param aJCas
     * @param evm          The event in that links will be attached upon to
     * @param begin        Begin of the range
     * @param end          End of the range
     * @param word2NPChunk The map from words Noun Phrase
     * @return A list of Links between the agents and the event will be returned
     */
    private <T extends ComponentAnnotation> List<EntityBasedComponentLink> getAndCreateValidAgentsInRange(
            JCas aJCas, EventMention evm, int begin, int end, Map<Word, T> word2NPChunk,
            String componentId) {
        ArrayList<EntityBasedComponentLink> agentLinks = new ArrayList<EntityBasedComponentLink>();

        if (begin > 0 && end > 0 && end > begin) {
            for (Annotation sireEntity : getEntitiesInInterest(aJCas, begin, end)) {
                List<Word> entityWords = JCasUtil.selectCovered(Word.class, sireEntity);

                if (entityWords.isEmpty()) {
                    // in case sire annotation is smaller than word
                    // Select covering is slow, but this case is indeed rare
                    entityWords = JCasUtil.selectCovering(aJCas, Word.class, sireEntity.getBegin(),
                            sireEntity.getEnd());
                    sireEntity.setBegin(entityWords.get(0).getBegin());
                    sireEntity.setEnd(entityWords.get(entityWords.size() - 1).getEnd());
                }

                Word entityHeadWord = FanseDependencyUtils.findHeadWordFromDependency(sireEntity);

                Span entityComponentSpan = getNonOverlappingNPSpan(entityHeadWord, evm, word2NPChunk);

                EntityBasedComponent newAgent = getOrCreateEntityComponent(aJCas,
                        entityComponentSpan.getBegin(), entityComponentSpan.getEnd(), entityHeadWord);

                agentLinks.add(APLUtils.createLink(aJCas, evm, newAgent, APLUtils.AGENT_LINK_TYPE,
                        componentId));
            }
        }

        return agentLinks;
    }

    /**
     * Try to get entity of specific type in range
     *
     * @param aJCas
     * @param begin
     * @param end
     * @return
     */
    private List<Annotation> getEntitiesInInterest(JCas aJCas, int begin, int end) {
        List<Annotation> interestingEntities = new ArrayList<Annotation>();
        for (StanfordEntityMention mention : JCasUtil.selectCovered(aJCas, StanfordEntityMention.class, begin, end)) {
            String entityType = mention.getEntityType();

            if (entityType != null) {
                if (entityType.equals("ORGANIZATION") || entityType.equals("PERSON")) {
                    interestingEntities.add(mention);
                }
            }
        }
        return interestingEntities;
    }

    private FanseToken findAgentConnectedByBy(FanseToken agentToken) {
        // propagate down the ARG0 attached to "by"
        if (agentToken.getCoveredText().toLowerCase().equals("by")) {
            Collection<FanseDependencyRelation> childDependencies = FSCollectionFactory.create(
                    agentToken.getChildDependencyRelations(), FanseDependencyRelation.class);
            // System.err.println("Search this by");

            for (FanseDependencyRelation childDependency : childDependencies) {
                String relationType = childDependency.getDependency();
                if (relationType.equals("pobj")) {
                    // if (relationType.equals("prep_by") || relationType.equals("agent")) {
                    FanseToken byRelationChild = childDependency.getChild();
                    List<FanseToken> corresFanseTokens = JCasUtil.selectCovered(FanseToken.class,
                            byRelationChild);
                    if (corresFanseTokens != null && corresFanseTokens.size() != 0) {
                        agentToken = corresFanseTokens.get(0);
                    }
                    // System.err.println("There is a BY");
                } else {
                    // System.err.println(relationType);
                }
            }
        }
        return agentToken;
    }

    /**
     * Look in the annotation for one side of the conjunction, if found, return the counter part of
     * this conjunction The conjunctinoRelationWordMap is actually pre-indexed.
     *
     * @param anno
     * @param conjunctionRelationWordMap
     * @param word2NPChunkIndex
     * @return
     */
    private Word findConjHeadWord(ComponentAnnotation anno,
                                  Map<Word, Word> conjunctionRelationWordMap, Map<Word, ExtendedNPChunk> word2NPChunkIndex) {
        // use the preindexed "cc" relation to get the "and" part
        Word andChildHeadWord = null;
        for (Word annoWord : JCasUtil.selectCovered(Word.class, anno)) {
            if (conjunctionRelationWordMap.containsKey(annoWord)) {
                andChildHeadWord = conjunctionRelationWordMap.get(annoWord);
                break; // assume only one
            }
        }

        return andChildHeadWord;
    }

    private <T extends ComponentAnnotation> Span getNonOverlappingNPSpan(Word headWord,
                                                                         EventMention evm, Map<Word, T> word2NPChunkIndex) {
        if (headWord == null) {
            throw new IllegalArgumentException("Cannot find for the span for null");
        }

        int begin, end;
        if (word2NPChunkIndex.containsKey(headWord)) {
            ComponentAnnotation coveringNPChunk = word2NPChunkIndex.get(headWord);

            // prevent event and the chunk overlap
            if (covers(coveringNPChunk, evm)) {
                begin = headWord.getBegin();
                end = headWord.getEnd();
            } else {
                begin = coveringNPChunk.getBegin();
                end = coveringNPChunk.getEnd();
            }
        } else {
            begin = headWord.getBegin();
            end = headWord.getEnd();
        }

        return new Span(begin, end);
    }

    /**
     * This method check the index first to avoid duplication, otherwise create a new one
     *
     * @param aJCas
     * @param begin
     * @param end
     * @param headWord
     * @return
     */
    private EntityBasedComponent getOrCreateEntityComponent(JCas aJCas, int begin, int end,
                                                            Word headWord) {
        if (begin >= end) {
            throw new IllegalArgumentException("Begin must be smaller than end");
        }

        if (word2EntityIndex.containsKey(headWord)) {
            EntityBasedComponent temp = word2EntityIndex.get(headWord);
            if (temp.getBegin() == begin && temp.getEnd() == end) {
                return word2EntityIndex.get(headWord);
            }
        }

        return APLUtils.createEntityBasedComponent(aJCas, begin, end, ANNOTATOR_COMPONENT_ID);
    }

    private void addWord2EntityIndex(EntityBasedComponent comp) {
        for (Word word : JCasUtil.selectCovered(Word.class, comp)) {
            word2EntityIndex.put(word, comp);
        }
    }

    private Word anyToken2Word(ComponentAnnotation token) {
        Span tokenSpan = toSpan(token);
        Word word = null;
        if (token instanceof StanfordCorenlpToken) {
            word = stanfordToken2Word.get(tokenSpan);
        } else if (token instanceof FanseToken) {
            word = fanseToken2Word.get(tokenSpan);
        } else if (token instanceof StanfordDependencyNode) {
            // assume stanford node is always corresponding to token
            word = stanfordToken2Word.get(tokenSpan);
        } else {
            logger.debug("Unrecongized token type " + token.getClass().getName());
        }

        return word;
    }

    private Span toSpan(ComponentAnnotation anno) {
        return new Span(anno.getBegin(), anno.getEnd());
    }

    private static boolean covers(Annotation coveringAnnotation, Annotation coveredAnnotation) {
        if (coveringAnnotation.getBegin() <= coveredAnnotation.getBegin() && coveringAnnotation.getEnd() >= coveredAnnotation.getEnd()) {
            return true;
        }
        return false;
    }
}
