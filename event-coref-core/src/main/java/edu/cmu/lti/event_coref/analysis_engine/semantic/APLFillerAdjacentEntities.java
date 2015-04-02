package edu.cmu.lti.event_coref.analysis_engine.semantic;

import com.google.common.collect.Iterables;
import edu.cmu.lti.event_coref.model.EventCorefConstants;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.APLUtils;
import edu.cmu.lti.utils.model.Span;
import edu.cmu.lti.utils.type.ComponentAnnotation;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.EmptyFSList;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class APLFillerAdjacentEntities extends JCasAnnotator_ImplBase {
    private static final String ANNOTATOR_COMPONENT_ID = "System-APL-adjacent-entities";

    private final int slack = 1;

    private static final Logger logger = LoggerFactory.getLogger(APLFillerAdjacentEntities.class);

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException {
        super.initialize(aContext);
        // sudokuTableWriter = CsvFactory
        // .getCSVWriter("../edu.cmu.lti.event_coref.system/doc/Hector/sudokutableAfterAdjacentEntities.csv");
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        String articleTitle = UimaConvenience.getShortDocumentName(aJCas);

        logger.info(String.format("Processing article: %s with [%s]",
                articleTitle, this.getClass().getSimpleName()));

        List<Sentence> allSentences = UimaConvenience.getAnnotationList(aJCas,
                Sentence.class);

        for (Sentence sent : allSentences) {
            List<EventMention> eventsInSent = JCasUtil.selectCovered(
                    EventMention.class, sent);
            List<ExtendedNPChunk> npInSent = JCasUtil.selectCovered(aJCas,
                    ExtendedNPChunk.class, sent);

            for (EventMention evm : eventsInSent) {
                boolean noAgent = false;
                boolean noPatient = false;
                FSList agentLinksFS = evm.getAgentLinks();
                FSList patientLinksFS = evm.getPatientLinks();
                if (agentLinksFS == null || agentLinksFS instanceof EmptyFSList) {
                    noAgent = true;
                }
                if (patientLinksFS == null
                        || patientLinksFS instanceof EmptyFSList) {
                    noPatient = true;
                }

                if (noAgent || noPatient) {
                    ExtendedNPChunk cloestLeftNounPhrase = getCloestLeftEntity(
                            aJCas, evm, npInSent);

                    if (cloestLeftNounPhrase != null) {
                        boolean isAlreadyLinkedAsPatient = isAlreadyLinked(
                                cloestLeftNounPhrase, evm.getPatientLinks());
                        boolean isAlreadyLinkedAsAgent = isAlreadyLinked(
                                cloestLeftNounPhrase, evm.getAgentLinks());

                        if (!isAlreadyLinkedAsPatient
                                && !isAlreadyLinkedAsAgent) {

                            Word headWord = evm.getHeadWord();
                            boolean isPastParticiple = headWord
                                    .getPartOfSpeech()
                                    .equals(EventCorefConstants.VERB_PAST_PARTICIPLE);

                            if (isPastParticiple && noPatient) {
                                // new patient
                                EntityBasedComponent adjacentCompoenent = createEntityComponentFromNounPhrase(
                                        aJCas, cloestLeftNounPhrase);
                                logger.info(String.format(
                                        "New Patient: [%s] for event [%s]",
                                        adjacentCompoenent.getCoveredText(),
                                        evm.getCoveredText()));
                                List<EntityBasedComponentLink> agentLinks = new LinkedList<EntityBasedComponentLink>();
                                agentLinks.add(APLUtils.createLink(aJCas, evm,
                                        adjacentCompoenent,
                                        APLUtils.AGENT_LINK_TYPE,
                                        ANNOTATOR_COMPONENT_ID));
                                evm.setAgentLinks(FSCollectionFactory.createFSList(aJCas, agentLinks));
                            } else if (noAgent) {
                                // new agent
                                EntityBasedComponent adjacentCompoenent = createEntityComponentFromNounPhrase(
                                        aJCas, cloestLeftNounPhrase);
                                logger.info(String.format(
                                        "New Agent: [%s] for event [%s]",
                                        adjacentCompoenent.getCoveredText(),
                                        evm.getCoveredText()));
                                List<EntityBasedComponentLink> agentLinks = new LinkedList<EntityBasedComponentLink>();
                                agentLinks.add(APLUtils.createLink(aJCas, evm,
                                        adjacentCompoenent,
                                        APLUtils.AGENT_LINK_TYPE,
                                        ANNOTATOR_COMPONENT_ID));
                                evm.setAgentLinks(FSCollectionFactory
                                        .createFSList(aJCas, agentLinks));
                            }
                        }
                    }
                }
            }
        }

        APLUtils.updateComponentEntityMentions(aJCas, ANNOTATOR_COMPONENT_ID);
        // EventMentionTable sTable = new EventMentionTable(aJCas);
        // sTable.writeSudokuTableAsCSV(sudokuTableWriter, articleTitle, false);
    }

    /**
     * Check whether the given span have any other events or punctuation
     *
     * @param aJCas
     * @param begin
     * @param end
     * @return
     */
    private boolean checkBetween(JCas aJCas, int begin, int end) {
        // 1. should not contain another event mention in between
        List<EventMention> eventsInBetween = JCasUtil.selectCovered(aJCas,
                EventMention.class, begin, end);
        if (!eventsInBetween.isEmpty())
            return false;

        // 2. should not contain punctuations in between
        List<Word> words = JCasUtil
                .selectCovered(aJCas, Word.class, begin, end);
        for (Word word : words) {
            if (Pattern.matches("\\p{Punct}", word.getCoveredText())) {
                return false;
            }
        }

        return true;
    }

    // /**
    // * Check whether the annotation contains any PERSON or ORGANIZATION
    // *
    // * @param anno
    // * @return
    // */
    // private boolean checkCandidate(ComponentAnnotation anno) {
    // List<com.ibm.racr.EntityMention> entitiesInBetween =
    // JCasUtil.selectCovered(
    // com.ibm.racr.EntityMention.class, anno);
    // int possibleAgentCount = 0;
    // for (EntityMention entity : entitiesInBetween) {
    // // logger.info(entity.getCoveredText() + " " + entity.getMentionType());
    // if (entity.getMentionType().equals("PEOPLE")
    // || entity.getMentionType().equals("ORGANIZATION")
    // || entity.getMentionType().equals("PERSON")) {
    // possibleAgentCount++;
    // }
    // }
    //
    // if (possibleAgentCount == 0)
    // return false;
    //
    // return true;
    // }

    /**
     * Check whether the annotation contains any Person or Organization entity
     * type It will first check for Sire, if failed, use Stanford
     *
     * @param anno
     * @return
     */

    private boolean checkCandidate(ComponentAnnotation anno) {
        List<StanfordEntityMention> stanfordMentions = JCasUtil.selectCovered(StanfordEntityMention.class, anno);
        int possibleAgentCount = 0;

        for (StanfordEntityMention mention : stanfordMentions) {
            String entityType = mention.getEntityType();
            if (entityType.equals("ORGANIZATION")
                    || entityType.equals("PERSON")) {
                possibleAgentCount++;
            }
        }

        if (possibleAgentCount == 0)
            return false;

        return true;
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException {
        super.collectionProcessComplete();

        // try {
        // sudokuTableWriter.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

    }

    // TODO: Need to handle the eventmention and word match better
    private Span getBeginEndWordIndices(JCas aJCas, ComponentAnnotation anno) {
        List<Word> words = JCasUtil.selectCovered(Word.class, anno);
        if (words.isEmpty()) {
            words = JCasUtil.selectCovering(aJCas, Word.class, anno.getBegin(),
                    anno.getEnd());
            if (words.isEmpty()) {
                return null;
            }
            return new Span(getWordIndex(words.get(0)),
                    getWordIndex(words.get(0)));
        }
        return new Span(getWordIndex(words.get(0)),
                getWordIndex(words.get(words.size() - 1)));
    }

    private int getWordIndex(Word aWord) {
        return Integer.parseInt(aWord.getWordId());
    }

    private EntityBasedComponent createEntityComponentFromNounPhrase(
            JCas aJCas, ExtendedNPChunk nounPhrase) {
        List<EntityBasedComponent> existingEntities = new ArrayList<EntityBasedComponent>();
        for (EntityBasedComponent comp : JCasUtil.selectCovered(
                EntityBasedComponent.class, nounPhrase)) {
            if (!Iterables.getFirst(
                    FSCollectionFactory.create(comp.getComponentLinks(),
                            Location.class), "").equals(
                    APLUtils.LOCATION_LINK_TYPE)) { // detect location, they are
                // not taken into
                // account
                existingEntities.add(comp);
            }
        }

        EntityBasedComponent adjacentEntityComponent;

        if (existingEntities.isEmpty()) {
            adjacentEntityComponent = APLUtils.createEntityBasedComponent(
                    aJCas, nounPhrase.getBegin(), nounPhrase.getEnd(),
                    ANNOTATOR_COMPONENT_ID);
        } else {
            adjacentEntityComponent = existingEntities.get(existingEntities
                    .size() - 1);
        }

        return adjacentEntityComponent;
    }

    private ExtendedNPChunk getCloestLeftEntity(JCas aJCas, EventMention evm,
                                                List<ExtendedNPChunk> candidateNounPhrases) {
        ExtendedNPChunk cloestLeftNounPhrase = null;

        Span evmSpan = getBeginEndWordIndices(aJCas, evm);
        if (evmSpan == null)
            return null;

        int evmWordBegin = evmSpan.getBegin();

        for (ExtendedNPChunk np : candidateNounPhrases) {
            Span npWordSpan = getBeginEndWordIndices(aJCas, np);
            int npEnd = npWordSpan.getEnd();

            if (npEnd > evmWordBegin)
                break;

            if (evmWordBegin - npEnd <= slack + 1 && evmWordBegin - npEnd > 0) {
                if (checkBetween(aJCas, np.getEnd(), evm.getBegin()))
                    if (checkCandidate(np))
                        cloestLeftNounPhrase = np;
            }
        }

        return cloestLeftNounPhrase;
    }

    private boolean isAlreadyLinked(ComponentAnnotation anno, FSList linksFS) {
        Span candidateSpan = new Span(anno.getBegin(), anno.getEnd());

        // make sure that it is not already been linked to the event
        boolean isAlreadyLinked = false;
        if (linksFS != null) {
            Collection<EntityBasedComponentLink> patientLinks = FSCollectionFactory
                    .create(linksFS, EntityBasedComponentLink.class);
            for (EntityBasedComponentLink patientLink : patientLinks) {
                EntityBasedComponent patient = patientLink.getComponent();
                Span patientSpan = new Span(patient.getBegin(),
                        patient.getEnd());
                if (patientSpan.checkOverlap(candidateSpan) != 0)
                    isAlreadyLinked = true;
            }
        }

        return isAlreadyLinked;
    }
}
