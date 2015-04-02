package edu.cmu.lti.event_coref.analysis_engine.semantic;

import com.google.common.collect.ArrayListMultimap;
import edu.cmu.lti.event_coref.model.EventCorefConstants;
import edu.cmu.lti.event_coref.model.EventMentionRow;
import edu.cmu.lti.event_coref.model.EventMentionTable;
import edu.cmu.lti.event_coref.model.TextualCellRecord;
import edu.cmu.lti.event_coref.model.TextualCellRecord.CELLTYPE;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.APLUtils;
import edu.cmu.lti.event_coref.utils.ClusterUtils;
import edu.cmu.lti.event_coref.utils.EventMentionUtils;
import edu.cmu.lti.event_coref.utils.UimaJava5;
import edu.cmu.lti.utils.model.AnnotationCondition;
import edu.cmu.lti.utils.model.Span;
import edu.cmu.lti.utils.type.ComponentAnnotation;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

public class APLFillerFromSimilarSlots extends JCasAnnotator_ImplBase {
    private Map<EventMention, EventMentionRow> sudokuTable;

    private static final String ANNOTATOR_COMPONENT_ID = "System-APL-similar-slots";

    private boolean debug = false;

    private static final Logger logger = LoggerFactory.getLogger(APLFillerFromSimilarSlots.class);

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        String articleTitle = UimaConvenience.getShortDocumentName(aJCas);
        logger.info(String.format("Processing article: %s with [%s]", articleTitle, this.getClass()
                .getSimpleName()));

        AnnotationCondition basicCondition = new AnnotationCondition() {
            @Override
            public Boolean check(TOP aAnnotation) {
                ComponentAnnotation anno = (ComponentAnnotation) aAnnotation;
                return anno.getBegin() > 0 && anno.getEnd() > 0;
            }
        };

        List<EventMention> allEvents = UimaConvenience.getAnnotationListWithFilter(aJCas,
                EventMention.class, basicCondition);

        List<Sentence> allSentences = UimaConvenience.getAnnotationList(aJCas, Sentence.class);
        ArrayListMultimap<Sentence, ExtendedNPChunk> sentToNPsMap = ArrayListMultimap.create();

        for (Sentence sent : allSentences) {
            List<ExtendedNPChunk> nounPhrases = JCasUtil
                    .selectCovered(aJCas, ExtendedNPChunk.class, sent);
            sentToNPsMap.putAll(sent, nounPhrases);
        }

        // build up sudoku table
        EventMentionTable sTable = new EventMentionTable(aJCas);
        sudokuTable = sTable.getTableView();

        // Use span can make sure annotation reuse
        Map<Span, EntityBasedComponent> entitySet = new HashMap<Span, EntityBasedComponent>();

        // Map<Span, Agent> agentSet = new HashMap<Span, Agent>();
        // Map<Span, Patient> patientSet = new HashMap<Span, Patient>();
        Map<Span, Location> locationSet = new HashMap<Span, Location>();

        // keep records of what surface forms are presented as a field
        Map<TextualCellRecord, EventMentionRow> agentSurfaceFormRecords = new LinkedHashMap<TextualCellRecord, EventMentionRow>();
        Map<TextualCellRecord, EventMentionRow> patientSurfaceFormRecords = new LinkedHashMap<TextualCellRecord, EventMentionRow>();
        Map<TextualCellRecord, EventMentionRow> locationSurfaceFormRecords = new LinkedHashMap<TextualCellRecord, EventMentionRow>();

        // keep the ambiguous agent in titles
        Set<EntityBasedComponentLink> ambiguousTitleAgentLinks = new HashSet<EntityBasedComponentLink>();

        for (EventMention eevm : allEvents) {
            EventMentionRow skdr = sudokuTable.get(eevm);

            if (skdr.isTitle()) {
                if (skdr.hasAgents()) {// likely to get agent wrong
                    boolean possiblePassive = false;
                    for (StanfordCorenlpToken word : JCasUtil.selectCovered(aJCas,
                            StanfordCorenlpToken.class, eevm)) {
                        if (word.getPos().equals(EventCorefConstants.VERB_PAST_PARTICIPLE)
                                || word.getPos().equals(EventCorefConstants.VERB_PAST_TENSE)) {
                            possiblePassive = true;
                        }
                    }
                    if (possiblePassive) {
                        for (EntityBasedComponentLink agentlink : skdr.getAgentLinks()) {
                            logger.debug("Added ambiguous agent link: "
                                    + agentlink.getComponent().getCoveredText());
                            ambiguousTitleAgentLinks.add(agentlink);
                        }
                    }
                }
                continue;// Don't trust title fields!
            }

            // for all the agent and their containing entity mention and their coreference entities, put
            // them into records
            for (EntityBasedComponent anAgent : skdr.getAgents()) {
                entitySet.put(new Span(anAgent.getBegin(), anAgent.getEnd()), anAgent);
                Collection<EntityMention> entityMentions = FSCollectionFactory.create(
                        anAgent.getContainingEntityMentions(), EntityMention.class);

                if (anAgent.getQuantity() != null) {
                    agentSurfaceFormRecords.put(new TextualCellRecord(anAgent, CELLTYPE.AGENT), skdr);
                }
                for (EntityMention em : entityMentions) {
                    if (isInformativeNamedEntity(em)) {
                        EntityCoreferenceCluster corefCLuster = ClusterUtils.getEntityFullClusterSystem(em);
                        if (corefCLuster != null) {
                            Collection<EntityMention> sfList = FSCollectionFactory.create(
                                    corefCLuster.getEntityMentions(), EntityMention.class);
                            for (EntityMention sf : sfList) {
                                if (isInformativeNamedEntity(sf)) {
                                    agentSurfaceFormRecords.put(new TextualCellRecord(sf, CELLTYPE.AGENT), skdr);
                                }
                            }
                        } else {
                            agentSurfaceFormRecords.put(new TextualCellRecord(em, CELLTYPE.AGENT), skdr);
                        }
                    }
                }
            }

            for (EntityBasedComponent aPatient : skdr.getPatients()) {
                entitySet.put(new Span(aPatient.getBegin(), aPatient.getEnd()), aPatient);
                Collection<EntityMention> entityMentions = FSCollectionFactory.create(
                        aPatient.getContainingEntityMentions(), EntityMention.class);
                if (aPatient.getQuantity() != null) {
                    patientSurfaceFormRecords.put(new TextualCellRecord(aPatient, CELLTYPE.PATIENT), skdr);
                }
                for (EntityMention em : entityMentions) {
                    if (isInformativeNamedEntity(em)) {
                        EntityCoreferenceCluster corefCluster = ClusterUtils.getEntityFullClusterSystem(em);
                        if (corefCluster != null) {
                            for (EntityMention sf : FSCollectionFactory.create(corefCluster.getEntityMentions(),
                                    EntityMention.class)) {
                                if (isInformativeNamedEntity(sf)) {
                                    patientSurfaceFormRecords.put(new TextualCellRecord(sf, CELLTYPE.PATIENT), skdr);
                                }
                            }
                        } else {
                            patientSurfaceFormRecords.put(new TextualCellRecord(em, CELLTYPE.PATIENT), skdr);
                        }
                    }
                }
            }

            for (Location aLocation : skdr.getLocations()) {
                locationSet.put(new Span(aLocation.getBegin(), aLocation.getEnd()), aLocation);
                Collection<EntityMention> entityMentions = FSCollectionFactory.create(
                        aLocation.getContainingEntityMentions(), EntityMention.class);
                for (EntityMention em : entityMentions) {
                    if (isInformativeNamedEntity(em)) {
                        EntityCoreferenceCluster corefCluster = ClusterUtils.getEntityFullClusterSystem(em);
                        if (corefCluster != null) {
                            for (EntityMention sf : FSCollectionFactory.create(corefCluster.getEntityMentions(),
                                    EntityMention.class)) {
                                if (isInformativeNamedEntity(sf)) {
                                    locationSurfaceFormRecords
                                            .put(new TextualCellRecord(sf, CELLTYPE.LOCATION), skdr);
                                }
                            }
                        } else {
                            locationSurfaceFormRecords.put(new TextualCellRecord(em, CELLTYPE.LOCATION), skdr);
                        }
                    }
                }
            }
        }

        // compare to the body agent list and patient list to decide the agent and patient for title
        // this only apply when Title is possibly passive voice
        for (EntityBasedComponentLink agentlink : ambiguousTitleAgentLinks) {
            int agentCount = 0;
            int patientCount = 0;

            EntityBasedComponent titleAgent = agentlink.getComponent();
            EventMention evm = agentlink.getEventMention();

            String titleAgentSurface = titleAgent.getCoveredText();
            if (debug)
                logger.info("Title surface: " + titleAgentSurface);

            for (TextualCellRecord record : agentSurfaceFormRecords.keySet()) {
                if (areSimilarEntitiesSurfaceForm(titleAgentSurface, record.getSurfaceForm())) {
                    if (debug)
                        logger.info("One more agent: " + record.getSurfaceForm());
                    agentCount++;
                }
            }

            for (Entry<TextualCellRecord, EventMentionRow> recordEntry : patientSurfaceFormRecords.entrySet()) {
                TextualCellRecord record = recordEntry.getKey();
                EventMentionRow row = recordEntry.getValue();
                if (EventMentionUtils.isTargetEventType(evm, row.getEventMention().getEventType())
                        && areSimilarEntitiesSurfaceForm(titleAgentSurface, record.getSurfaceForm())) {
                    if (debug)
                        logger.info("One more patient: " + record.getSurfaceForm());
                    patientCount++;
                }
            }

            // more are patients, so change annotation
            if (patientCount > agentCount) {
                EntityBasedComponent titlePatient = new EntityBasedComponent(aJCas, titleAgent.getBegin(),
                        titleAgent.getEnd());
                if (debug)
                    logger.info("Set to patient: " + titlePatient.getCoveredText());
                titlePatient.addToIndexes(aJCas);
                titlePatient.setComponentId(ANNOTATOR_COMPONENT_ID);

                EntityBasedComponentLink pLink = APLUtils.createLink(aJCas, evm, titlePatient,
                        APLUtils.PATIENT_LINK_TYPE, ANNOTATOR_COMPONENT_ID);

                // associate to event and patient
                titlePatient.setComponentLinks(UimaConvenience.appendFSList(aJCas,
                        titlePatient.getComponentLinks(), pLink, EntityBasedComponentLink.class));
                FSList newPatientLinks = UimaConvenience.appendFSList(aJCas, evm.getPatientLinks(), pLink,
                        EntityBasedComponentLink.class);
                evm.setPatientLinks(newPatientLinks);

                // remove the agent link
                // remove agent link from the agent
                if (UimaJava5.fsListSize(titleAgent.getComponentLinks()) == 1) {
                    titleAgent.removeFromIndexes(aJCas); // only one link, safe to delete all
                } else {
                    titleAgent.setComponentLinks(UimaConvenience.removeFromFSList(aJCas,
                            titleAgent.getComponentLinks(), agentlink, EntityBasedComponentLink.class));
                }
                // remove agent link from the event
                FSList newAgentLinks = UimaConvenience.removeFromFSList(aJCas, evm.getAgentLinks(),
                        agentlink, EntityBasedComponentLink.class);
                evm.setAgentLinks(newAgentLinks);
                // remove the agent link itself
                agentlink.removeFromIndexes(aJCas);

                // update sudoku table
                EventMentionRow skdr = sudokuTable.get(evm);
                skdr.setAgents(new ArrayList<EntityBasedComponentLink>(FSCollectionFactory.create(
                        newAgentLinks, EntityBasedComponentLink.class)));
                skdr.setPatients(new ArrayList<EntityBasedComponentLink>(FSCollectionFactory.create(
                        newPatientLinks, EntityBasedComponentLink.class)));
            }
        }

        if (debug) {
            logger.info("Agent records");
            for (Entry<TextualCellRecord, EventMentionRow> entry : agentSurfaceFormRecords.entrySet()) {
                TextualCellRecord record = entry.getKey();
                EventMentionRow row = entry.getValue();
                logger.info(String.format(
                        "Record - Event:[%s], Event type:[%s], Surface:[%s], Record Type:[%s]", row
                                .getEventMention().getCoveredText(), row.getEventMention().getEventType(),
                        record.getSurfaceForm(), record.getCellType()));
            }

            logger.info("Patient records");
            for (Entry<TextualCellRecord, EventMentionRow> entry : patientSurfaceFormRecords.entrySet()) {
                TextualCellRecord record = entry.getKey();
                EventMentionRow row = entry.getValue();
                logger.info(String.format(
                        "Record - Event:[%s], Event type:[%s], Surface:[%s], Record Type:[%s]", row
                                .getEventMention().getCoveredText(), row.getEventMention().getEventType(),
                        record.getSurfaceForm(), record.getCellType()));
            }

            logger.info("Location records");
            for (Entry<TextualCellRecord, EventMentionRow> entry : locationSurfaceFormRecords.entrySet()) {
                TextualCellRecord record = entry.getKey();
                EventMentionRow row = entry.getValue();
                logger.info(String.format(
                        "Record - Event:[%s], Event type:[%s], Surface:[%s], Record Type:[%s]", row
                                .getEventMention().getCoveredText(), row.getEventMention().getEventType(),
                        record.getSurfaceForm(), record.getCellType()));
            }
        }

        // use existing cell filling to guess A/P/L
        for (EventMentionRow scanningRow : sudokuTable.values()) {
            if (debug) {
                logger.info(String.format("Scanning sudoku row for event [%s] to fill up APL", scanningRow
                        .getEventMention().getCoveredText()));
            }
            Sentence sent = scanningRow.getSentence();
            List<ExtendedNPChunk> nounPhrases = sentToNPsMap.get(sent);
            EventMention eventMention = scanningRow.getEventMention();

            if (!scanningRow.hasAgents() || !scanningRow.hasPatients() || !scanningRow.hasLocations()) {
                Set<EntityBasedComponent> inferredAgents = new HashSet<EntityBasedComponent>();
                Set<EntityBasedComponent> inferredPatients = new HashSet<EntityBasedComponent>();
                Set<Location> inferredLocations = new HashSet<Location>();

                if (debug) {
                    logger.info("Candidate noun phrases");
                    for (ExtendedNPChunk np : nounPhrases) {
                        logger.info(np.getCoveredText());
                    }
                }

                if (!scanningRow.hasAgents()) {
                    if (debug) {
                        logger.info(String.format("[%s] is looking for agent", eventMention.getCoveredText()));
                    }
                    for (ExtendedNPChunk np : nounPhrases) {
                        for (Entry<TextualCellRecord, EventMentionRow> entry : agentSurfaceFormRecords.entrySet()) {
                            TextualCellRecord agentSfRecord = entry.getKey();
                            EventMentionRow recordRow = entry.getValue();
                            if (isPossibleField(scanningRow, np, agentSfRecord, recordRow)) {
                                EntityBasedComponent inferredAgent;
                                Span agentSpan = new Span(np.getBegin(), np.getEnd());

                                // we do not want it to conflict with its own patient
                                boolean conflictWithPatient = checkOverlapFromLinks(agentSpan, scanningRow
                                        .getEventMention().getPatientLinks());

                                if (!conflictWithPatient) {
                                    if (!entitySet.containsKey(agentSpan)) {
                                        inferredAgent = new EntityBasedComponent(aJCas, np.getBegin(), np.getEnd());
                                        inferredAgent.addToIndexes(aJCas);
                                        inferredAgent.setComponentId(ANNOTATOR_COMPONENT_ID);
                                        entitySet.put(agentSpan, inferredAgent);
                                    } else {
                                        inferredAgent = entitySet.get(agentSpan);
                                    }
                                    if (debug) {
                                        logger.info(String.format("New Agent: %s from %d to %d",
                                                inferredAgent.getCoveredText(), inferredAgent.getBegin(),
                                                inferredAgent.getEnd()));
                                    }
                                    inferredAgents.add(inferredAgent);
                                }
                            }
                        }
                    }
                }
                if (!scanningRow.hasPatients()) {
                    if (debug) {
                        logger.info(String.format("[%s] is looking for patient", eventMention.getCoveredText()));
                    }
                    for (ExtendedNPChunk np : nounPhrases) {
                        for (Entry<TextualCellRecord, EventMentionRow> entry : patientSurfaceFormRecords.entrySet()) {
                            TextualCellRecord patientSFRecord = entry.getKey();
                            EventMentionRow recordRow = entry.getValue();
                            if (isPossibleField(scanningRow, np, patientSFRecord, recordRow)) {
                                EntityBasedComponent inferredPatient;
                                Span patientSpan = new Span(np.getBegin(), np.getEnd());

                                // we do not want it to conflict with its agent
                                boolean conflictWithAgent = checkOverlapFromLinks(patientSpan, scanningRow
                                        .getEventMention().getAgentLinks());

                                if (conflictWithAgent) {
                                    if (!entitySet.containsKey(patientSpan)) {
                                        inferredPatient = new EntityBasedComponent(aJCas, np.getBegin(), np.getEnd());
                                        inferredPatient.addToIndexes(aJCas);
                                        inferredPatient.setComponentId(ANNOTATOR_COMPONENT_ID);
                                        entitySet.put(patientSpan, inferredPatient);
                                    } else {
                                        inferredPatient = entitySet.get(patientSpan);
                                    }
                                    if (debug) {
                                        logger.info(String.format("New Patient: %s from %d to %d",
                                                inferredPatient.getCoveredText(), inferredPatient.getBegin(),
                                                inferredPatient.getEnd()));
                                    }
                                    inferredPatients.add(inferredPatient);
                                }
                            }
                        }
                    }
                }
                // if (!scanningRow.hasLocations()) {
                // remove this constrain for location, bcuz for location the closer the better
                if (debug) {
                    logger.info(String.format("[%s] is looking for location", eventMention.getCoveredText()));
                }
                for (ExtendedNPChunk np : nounPhrases) {
                    for (Entry<TextualCellRecord, EventMentionRow> entry : locationSurfaceFormRecords.entrySet()) {
                        TextualCellRecord locationSFRecord = entry.getKey();
                        EventMentionRow recordRow = entry.getValue();
                        if (isPossibleField(scanningRow, np, locationSFRecord, recordRow)) {
                            Location inferredLocation;
                            Span locationSpan = new Span(np.getBegin(), np.getEnd());
                            if (!locationSet.containsKey(locationSpan)) {
                                inferredLocation = new Location(aJCas, np.getBegin(), np.getEnd());
                                inferredLocation.addToIndexes(aJCas);
                                inferredLocation.setComponentId(ANNOTATOR_COMPONENT_ID);
                                locationSet.put(locationSpan, inferredLocation);
                            } else {
                                inferredLocation = locationSet.get(locationSpan);
                            }
                            if (debug) {
                                logger.info(String.format("New Location: %s from %d to %d",
                                        inferredLocation.getCoveredText(), inferredLocation.getBegin(),
                                        inferredLocation.getEnd()));

                            }
                            inferredLocations.add(inferredLocation);
                        }
                    }
                }
                // }

                // The rest add the inferred agent, location, patient to the events, we only infer agent
                // patient, location for events that does not contains them before, so don't need to
                // consider appending
                if (!inferredAgents.isEmpty()) {
                    ArrayList<EntityBasedComponentLink> agentLinks = new ArrayList<EntityBasedComponentLink>();
                    for (EntityBasedComponent agent : inferredAgents) {
                        EntityBasedComponentLink aLink = APLUtils.createLink(aJCas, eventMention, agent,
                                APLUtils.AGENT_LINK_TYPE, ANNOTATOR_COMPONENT_ID);

                        agent.setComponentLinks(UimaConvenience.appendFSList(aJCas, agent.getComponentLinks(),
                                aLink, EntityBasedComponentLink.class));

                        agentLinks.add(aLink);
                    }

                    eventMention.setAgentLinks(FSCollectionFactory.createFSList(aJCas, agentLinks));
                    // eventMention.setAgents(FSCollectionFactory.createFSList(aJCas, inferredAgents));
                    scanningRow.setAgents(new ArrayList<EntityBasedComponentLink>(agentLinks));
                }

                if (!inferredPatients.isEmpty()) {
                    ArrayList<EntityBasedComponentLink> patientLinks = new ArrayList<EntityBasedComponentLink>();
                    for (EntityBasedComponent patient : inferredPatients) {
                        EntityBasedComponentLink pLink = APLUtils.createLink(aJCas, eventMention, patient,
                                APLUtils.PATIENT_LINK_TYPE, ANNOTATOR_COMPONENT_ID);

                        patient.setComponentLinks(UimaConvenience.appendFSList(aJCas,
                                patient.getComponentLinks(), pLink, EntityBasedComponentLink.class));

                        patientLinks.add(pLink);
                    }

                    eventMention.setPatientLinks(FSCollectionFactory.createFSList(aJCas, patientLinks));
                    // eventMention.setPatients(FSCollectionFactory.createFSList(aJCas, inferredPatients));
                    scanningRow.setPatients(new ArrayList<EntityBasedComponentLink>(patientLinks));
                }

                if (!inferredLocations.isEmpty()) {
                    // probably only consider reserver the cloesest one
                    List<EntityBasedComponentLink> loclinks = new ArrayList<EntityBasedComponentLink>();
                    for (Location loc : inferredLocations) {
                        EntityBasedComponentLink llink = APLUtils.createLink(aJCas, eventMention, loc,
                                APLUtils.LOCATION_LINK_TYPE, ANNOTATOR_COMPONENT_ID);
                        loc.setComponentLinks(UimaConvenience.appendFSList(aJCas, loc.getComponentLinks(),
                                llink, EntityBasedComponentLink.class));
                        loclinks.add(llink);
                    }

                    eventMention.setLocationLinks(UimaConvenience.appendAllFSList(aJCas,
                            eventMention.getLocationLinks(), loclinks, EntityBasedComponentLink.class));

                    // eventMention.setLocations(FSCollectionFactory.createFSList(aJCas, inferredLocations));
                    scanningRow.setLocations(new ArrayList<EntityBasedComponentLink>(loclinks));
                }
            }
        }

        APLUtils.updateComponentEntityMentions(aJCas, ANNOTATOR_COMPONENT_ID);
        // writeEachFile(aJCas, sTable, articleTitle);
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        super.collectionProcessComplete();
    }

    /**
     * Check whether the noun phrase is a possible field by comparing with the record of the same
     * type, which the following conditions 1. Textual similarity 2. Same event type 3. Suitable
     * entity type 4. local similarity (same paragraph, same sentence?) 5. Sequence of appearance
     *
     * @param eventRow
     * @param nounPhrase
     * @param record
     * @param recordRow  the row contains other information of this record
     * @return
     */
    private <K extends ComponentAnnotation> boolean isPossibleField(EventMentionRow eventRow, K nounPhrase,
                                                                    TextualCellRecord record, EventMentionRow recordRow) {
        EventMention eventToAttach = eventRow.getEventMention();
        String eventToAttachType = eventToAttach.getEventType();

        String existingRecordSf = record.getSurfaceForm();
        EventMention recordEventMention = recordRow.getEventMention();
        String recordEventType = recordEventMention.getEventType();

        // this surface set contains:
        // 1. the surface itself
        // 2. the entities it contains
        List<String> currentStringSurfaceSet = new ArrayList<String>();
        currentStringSurfaceSet.add(nounPhrase.getCoveredText());

        // so we do not borrow information from later part of file
        // abd we do not use the current sentence
        if (recordRow.getSentenceId() >= eventRow.getSentenceId()) {
            return false;
        }

        // "other" annotation are ignored
        if (eventToAttachType != null && eventToAttachType.equals("other")) {
            return false;
        }

        for (EntityMention em : JCasUtil.selectCovered(EntityMention.class, nounPhrase)) {
            EntityCoreferenceCluster cluster = ClusterUtils.getEntityFullClusterSystem(em);
            if (cluster != null) {
                for (EntityMention corefEm : FSCollectionFactory.create(cluster.getEntityMentions(),
                        EntityMention.class)) {
                    if (isInformativeNamedEntity(corefEm)) {
                        currentStringSurfaceSet.add(corefEm.getCoveredText());
                    }
                }
            }
        }

        for (String surface : currentStringSurfaceSet) {
            if (areSimilarEntitiesSurfaceForm(existingRecordSf, surface)) {
                if (debug) {
                    logger.info(String.format("Surface form of the corresponding nounphase: [%s] ",
                            nounPhrase.getCoveredText()));
                    logger.info(String.format("- Record being compared to: [%s]-[%s]", recordEventType,
                            existingRecordSf));
                    logger.info(String.format(
                            "- This is similar to one of current event's surface forms: [%s]-[%s]",
                            eventToAttachType, surface));
                }
                if (eventToAttachType == recordEventType || eventToAttachType.equals(recordEventType)) {
                    if (debug) {
                        logger.info(String.format(
                                "Infer new record [%s] for event [%s] from an existing event [%s]",
                                nounPhrase.getCoveredText(), eventToAttach.getCoveredText(),
                                recordEventMention.getCoveredText()));
                        logger.info(String.format("  - Surface: [ %s | %s ] ", existingRecordSf,
                                nounPhrase.getCoveredText()));
                        logger.info(String.format("  - Event type: [ %s | %s]", eventToAttachType,
                                recordEventType));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private Boolean checkOverlapFromLinks(Span candidateSpan, FSList componentLinksFS) {
        boolean isConflict = false;
        if (componentLinksFS != null) {
            Collection<EntityBasedComponentLink> componentLinks = FSCollectionFactory.create(
                    componentLinksFS, EntityBasedComponentLink.class);
            for (EntityBasedComponentLink componentLink : componentLinks) {
                EntityBasedComponent patient = componentLink.getComponent();
                Span patientSpan = new Span(patient.getBegin(), patient.getEnd());
                if (patientSpan.checkOverlap(candidateSpan) != 0)
                    isConflict = true;
            }
        }
        return isConflict;
    }

    private Boolean isInformativeNamedEntity(EntityMention mention) {
        if ((mention.getEntityType() == null) || mention.getEntityType().equals("DATE")) {
            return false;
        }
        List<StanfordCorenlpToken> words = JCasUtil.selectCovered(StanfordCorenlpToken.class, mention);
        if (words.size() > 4) // so do not trust long entities.
            return false;

        boolean hasNoun = false;
        for (StanfordCorenlpToken word : words) {
            if (word.getPos().startsWith(EventCorefConstants.NOUN_LABEL_PREFIX)) {
                hasNoun = true;
            }
        }

        if (!hasNoun) {
            return false;
        }

        return true;
    }

    private boolean areSimilarEntitiesSurfaceForm(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        if (s1.contains(s2)) {
            return true;
        }
        if (s2.contains(s1)) {
            return true;
        }

        return false;
    }
}