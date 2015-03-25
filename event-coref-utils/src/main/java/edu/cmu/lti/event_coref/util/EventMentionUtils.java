package edu.cmu.lti.event_coref.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.util.EventCoreferenceConstants.EventCoreferenceRelationType;
import edu.cmu.lti.event_coref.util.EventCoreferenceConstants.EventType;
import edu.cmu.lti.event_coref.util.SemanticRoleConstants.SemanticRole;
import edu.cmu.lti.util.general.*;
import edu.cmu.lti.util.uima.UimaConvenience;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.uimafit.util.FSCollectionFactory;
import org.uimafit.util.JCasUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class provides useful utilities specifically for event mentions.
 *
 * @author Jun Araki
 */
public class EventMentionUtils {

    /**
     * Returns a two-dimensional array specifying pairwise gold standard data.
     *
     * @param aJCas
     * @param corefType
     * @param eventMentions
     * @param eccList
     * @return a two-dimensional array specifying pairwise gold standard data
     */
    public static PairwiseEventCoreferenceEvaluation[][] getPairwiseGoldStandard(JCas aJCas,
                                                                                 EventCoreferenceConstants.EventCoreferenceRelationType corefType, List<EventMention> eventMentions,
                                                                                 List<EventCoreferenceCluster> eccList) {

        int numOfEventMentions = eventMentions.size();
        PairwiseEventCoreferenceEvaluation[][] goldStandard = new PairwiseEventCoreferenceEvaluation[numOfEventMentions][numOfEventMentions];

        for (int i = 0; i < numOfEventMentions; i++) {
            for (int j = 0; j < numOfEventMentions; j++) {
                if (j <= i) {
                    goldStandard[i][j] = null;
                    continue;
                }

                EventMention evmI = eventMentions.get(i);
                EventMention evmJ = eventMentions.get(j);

                PairwiseEventCoreferenceEvaluation pece = new PairwiseEventCoreferenceEvaluation(aJCas);
                pece.setEventMentionI(evmI);
                pece.setEventMentionJ(evmJ);

                if (EventCoreferenceConstants.EventCoreferenceRelationType.FULL.equals(corefType)) {
                    // In the case of full coreference
                    if (EventCoreferenceClusterUtils.inSameEventCluster(eccList, evmI, evmJ)) {
                        pece.setEventCoreferenceRelationGoldStandard(EventCoreferenceConstants.EventCoreferenceRelationType.FULL
                                .toString());
                    } else {
                        pece.setEventCoreferenceRelationGoldStandard(EventCoreferenceConstants.EventCoreferenceRelationType.NO.toString());
                    }
                } else if (EventCoreferenceRelationType.SUBEVENT.equals(corefType)) {
                    if (EventCoreferenceClusterUtils.hasParentChildRelation(eccList, evmI, evmJ)) {
                        pece.setEventCoreferenceRelationGoldStandard(EventCoreferenceRelationType.SUBEVENT
                                .toString());
                    } else {
                        pece.setEventCoreferenceRelationGoldStandard(EventCoreferenceRelationType.NO.toString());
                    }
                } else if (EventCoreferenceRelationType.MEMBER.equals(corefType)) {
                    if (EventCoreferenceClusterUtils.hasParentChildRelation(eccList, evmI, evmJ)) {
                        pece.setEventCoreferenceRelationGoldStandard(EventCoreferenceRelationType.MEMBER
                                .toString());
                    } else {
                        pece.setEventCoreferenceRelationGoldStandard(EventCoreferenceRelationType.NO.toString());
                    }
                }

                goldStandard[i][j] = pece;
            }
        }

        return goldStandard;
    }

    /**
     * Check whether the specified two event mentions are identical.
     *
     * @param evmI
     * @param evmJ
     * @return true if the specified two event mentions are identical; false otherwise
     */
    public static boolean isSameEventMention(EventMention evmI, EventMention evmJ) {
        if (evmI == null || evmJ == null) {
            return false;
        }

        if (evmI.getAddress() == (evmJ.getAddress())) {
            return true;
        }

        return false;
    }

    public static boolean containsEventMention(Collection<EventMention> eventMentions,
                                               EventMention evm) {
        boolean isFound = false;
        for (EventMention eventMention : eventMentions) {
            if (isSameEventMention(eventMention, evm)) {
                isFound = true;
            }
        }

        return isFound;
    }

    public static boolean isSameEventMentionCollection(Collection<EventMention> eventMentionsI,
                                                       Collection<EventMention> eventMentionsJ) {

        if (eventMentionsI == null && eventMentionsJ != null) {
            return false;
        }
        if (eventMentionsI != null && eventMentionsJ == null) {
            return false;
        }

        if (eventMentionsI.size() != eventMentionsJ.size()) {
            return false;
        }

        for (EventMention evmI : eventMentionsI) {
            if (!containsEventMention(eventMentionsJ, evmI)) {
                return false;
            }
        }

        return true;
    }

    public static void writeErrorAnalysisReport(JCas aJCas, String outputFile,
                                                List<EventMention> eventMentions, String[][] errorAnalysis) {
        int evmNum = eventMentions.size();
        SourceDocumentInformation srcDocInfo = JCasUtil.selectSingle(aJCas,
                SourceDocumentInformation.class);

        StringBuilder buf = new StringBuilder();

        // Header
        CsvUtils.appendValueWithComma(buf, "Document URI");
        CsvUtils.appendValueWithLineBreak(buf,
                EventCoreferenceMiscUtils.getShortName(srcDocInfo.getUri()));

        CsvUtils.appendValueWithComma(buf, "Event mention (raw string)");
        CsvUtils.appendValueWithComma(buf, "");
        for (int i = 0; i < evmNum; i++) {
            int eventMentionId = i + 1;
            if (i == evmNum - 1) {
                CsvUtils.appendValueWithLineBreak(buf, eventMentionId);
            } else {
                CsvUtils.appendValueWithComma(buf, eventMentionId);
            }
        }

        // Content
        for (int i = 0; i < evmNum; i++) {
            CsvUtils.appendValueWithComma(buf, eventMentions.get(i).getCoveredText());

            int eventMentionId = i + 1;
            CsvUtils.appendValueWithComma(buf, eventMentionId);

            for (int j = 0; j < evmNum; j++) {
                if (j == evmNum - 1) {
                    CsvUtils.appendValueWithLineBreak(buf, errorAnalysis[i][j]);
                } else {
                    CsvUtils.appendValueWithComma(buf, errorAnalysis[i][j]);
                }
            }
        }

        CsvUtils.appendValueWithLineBreak(buf, "");
        FileUtils.appendFile(outputFile, buf.toString());
    }

    public static boolean isDomainEvent(EventMention evm) {
        return isDomainEventType(evm.getEventType());
    }

    private static boolean isDomainEventType(String eventType) {
        if (StringUtils.isNullOrEmptyString(eventType)) {
            return false;
        }

        if (eventType.equals(EventType.DOMAIN.toString())) {
            return true;
        }

        return false;
    }

    public static boolean isReportingEvent(EventMention evm) {
        return isReportingEventType(evm.getEventType());
    }

    private static boolean isReportingEventType(String eventType) {
        if (StringUtils.isNullOrEmptyString(eventType)) {
            return false;
        }

        if (eventType.equals(EventType.REPORTING.toString())) {
            return true;
        }

        return false;
    }

    public static boolean isImplicitEvent(EventMention evm) {
        if (evm.getBegin() <= 0 || evm.getEnd() <= 0) {
            return true;
        }

        return false;
    }

    public static boolean isNonImplicitEvent(EventMention evm) {
        return !isImplicitEvent(evm);
    }

    public static boolean isNonImplicitDomainEvent(EventMention evm) {
        return (isDomainEvent(evm) && isNonImplicitEvent(evm));
    }

    public static List<EventMention> getNonImplicitEvents(JCas aJCas) {
        List<EventMention> nonImplicitEventMentionList = new ArrayList<EventMention>();
        for (EventMention evm : JCasUtil.select(aJCas, EventMention.class)) {
            if (isNonImplicitEvent(evm)) {
                nonImplicitEventMentionList.add(evm);
            }
        }

        return nonImplicitEventMentionList;
    }

    public static List<EventMention> getNonImplicitDomainEvents(JCas aJCas) {
        List<EventMention> nonImplicitDomainEventMentionList = new ArrayList<EventMention>();
        for (EventMention evm : JCasUtil.select(aJCas, EventMention.class)) {
            if (isNonImplicitDomainEvent(evm)) {
                nonImplicitDomainEventMentionList.add(evm);
            }
        }

        return nonImplicitDomainEventMentionList;
    }

    public static String getCanonicalizedEventMentionString(EventMention evm) {
        Word headWord = evm.getHeadWord();
        ErrorUtils.terminateIfNull(headWord, "No head word for " + evm.getCoveredText());

        String lemma = headWord.getLemma();
        String lowerCaseLemma = lemma.toLowerCase();


        return lowerCaseLemma;
    }

    public static String getEventMentionInfoWithArguments(EventMention evm) {
        Multimap<String, EventMentionArgumentLink> argMap = ArrayListMultimap.create();

        List<EventMentionArgumentLink> argLinks = UimaConvenience.convertFSListToList(
                evm.getArguments(), EventMentionArgumentLink.class);
        if (!ListUtils.isNullOrEmptyList(argLinks)) {
            for (EventMentionArgumentLink argLink : argLinks) {
                argMap.put(argLink.getArgumentRole(), argLink);
            }
        }

        StringBuilder buf = new StringBuilder();
        buf.append("Event mention: ");
        buf.append("(" + "E" + evm.getGoldStandardEventMentionId() + ") ");
        buf.append("[");
        buf.append(evm.getCoveredText().replace(System.lineSeparator(), " "));
        buf.append("]");
        buf.append(", pos: [" + evm.getHeadWord().getPartOfSpeech() + "]");
        buf.append(", agent: " + getArgsWithConfidence(argMap.get(SemanticRole.AGENT.name())));
        buf.append(", patient: " + getArgsWithConfidence(argMap.get(SemanticRole.PATIENT.name())));
        buf.append(", location: " + getArgsWithConfidence(argMap.get(SemanticRole.LOCATION.name())));
        buf.append(", time: " + getArgsWithConfidence(argMap.get(SemanticRole.TIME.name())));

        return buf.toString();
    }

    /**
     * Returns a string of the specified collection of argument links.
     *
     * @param argLinks
     * @return
     */
    public static String getArgsWithConfidence(Collection<EventMentionArgumentLink> argLinks) {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        if (!CollectionUtils.isNullOrEmptyCollection(argLinks)) {
            int i = 0;
            for (EventMentionArgumentLink argLink : argLinks) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(argLink.getArgument().getCoveredText());
                buf.append(" (");
                buf.append(MathUtils.getRoundedDecimalNumber(argLink.getConfidence(), 2, true));
                buf.append(")");
                i++;
            }
        }
        buf.append("]");

        return buf.toString();
    }

    public static String getEventMentionInfoWithComponentLinks(EventMention evm) {
        List<EntityBasedComponent> agents = new ArrayList<EntityBasedComponent>();
        List<EntityBasedComponent> patients = new ArrayList<EntityBasedComponent>();
        List<EntityBasedComponent> locations = new ArrayList<EntityBasedComponent>();

        FSList agtLinkList = evm.getAgentLinks();
        if (agtLinkList != null) {
            for (EntityBasedComponentLink agtLink : FSCollectionFactory.create(agtLinkList,
                    EntityBasedComponentLink.class)) {
                agents.add(agtLink.getComponent());
            }
        }

        FSList patLinkList = evm.getPatientLinks();
        if (patLinkList != null) {
            for (EntityBasedComponentLink patLink : FSCollectionFactory.create(patLinkList,
                    EntityBasedComponentLink.class)) {
                patients.add(patLink.getComponent());
            }
        }

        FSList locLinkList = evm.getLocationLinks();
        if (locLinkList != null) {
            for (EntityBasedComponentLink locLink : FSCollectionFactory.create(locLinkList,
                    EntityBasedComponentLink.class)) {
                locations.add(locLink.getComponent());
            }
        }

        return getEventMentionInfoWithComponentLinks(evm, agents, patients, locations);
    }

    public static String getEventMentionInfoWithComponentLinks(EventMention evm,
                                                               List<EntityBasedComponent> agents, List<EntityBasedComponent> patients,
                                                               List<EntityBasedComponent> locations) {
        StringBuilder buf = new StringBuilder();
        buf.append("Event mention: ");
        buf.append("(" + "E" + evm.getGoldStandardEventMentionId() + ") ");
        buf.append("[");
        buf.append(evm.getCoveredText().replace(System.lineSeparator(), " "));
        buf.append("]");
        buf.append(", pos: [" + evm.getHeadWord().getPartOfSpeech() + "]");
        buf.append(", agent: " + UimaConvenience.getAnnotationsStr(agents));
        buf.append(", patient: " + UimaConvenience.getAnnotationsStr(patients));
        buf.append(", location: " + UimaConvenience.getAnnotationsStr(locations));

        return buf.toString();
    }

//    /**
//     * Prints out the specified event mention with its detailed info.
//     *
//     * @param evm
//     */
//    public static void printEventMentionDetail(EventMention evm) {
//        LogUtils.log(getEventMentionInfoWithComponentLinks(evm));
//    }
//
//    /**
//     * Prints out the specified event mention with its detailed info.
//     *
//     * @param evm
//     * @param agents
//     * @param patients
//     * @param locations
//     */
//    public static void printEventMentionDetail(EventMention evm, List<EntityBasedComponent> agents,
//                                               List<EntityBasedComponent> patients, List<EntityBasedComponent> locations) {
//        LogUtils.log(getEventMentionInfoWithComponentLinks(evm, agents, patients, locations));
//    }

    public static EventMention getEventMentionFromDifferentView(JCas view, EventMention evm) {
        int begin = evm.getBegin();
        int end = evm.getEnd();
        if (begin < 0 || end < 0) {
            return null;
        }

        List<EventMention> evmList = JCasUtil.selectCovering(view, EventMention.class, begin, end);
        if (ListUtils.isNullOrEmptyList(evmList)) {
            return null;
        }

        return evmList.get(0);
    }

    /**
     * Return whether given event mention is the specified type.Specified a "null" type means all
     * event mention will match
     *
     * @param evm
     * @param targetType The type specified
     * @return
     */
    public static boolean isTargetEventType(EventMention evm, String targetType) {
        String evmType = evm.getEventType();

        if (evmType == null) {
            if (targetType == null) {
                System.err
                        .println("Event type not initialized, now simply regard it as target event, it is suggested to check!");
                return true;
            } else
                return false;
        }

        if (evmType.equals(targetType)) {
            return true;
        }
        return false;
    }

//    /**
//     * Checks whether the specified two event mentions hold equivalent event mention annotations. By
//     * "equivalent", we mean that the string and offsets of the two event mentions are the same. Note
//     * that two event mentions may come from different views.
//     *
//     * @param evm1
//     * @param evm2
//     * @param printWarning
//     * @return
//     */
//    public static boolean areSameListOfEventMentionAnnotations(EventMention evm1, EventMention evm2,
//                                                               boolean printWarning) {
//        if (evm1 == null && evm2 == null) {
//            if (printWarning) {
//                LogUtils.logWarn("The both event mentions are null.");
//            }
//            return true;
//        } else if (evm1 == null) {
//            if (printWarning) {
//                LogUtils.logWarn("The first event mention is null whereas the second one isn't.");
//            }
//            return false;
//        } else if (evm2 == null) {
//            if (printWarning) {
//                LogUtils.logWarn("The second event mention is null whereas the first one isn't.");
//            }
//            return false;
//        }
//
//        int begin1 = evm1.getBegin();
//        int begin2 = evm2.getBegin();
//        if (begin1 != begin2) {
//            if (printWarning) {
//                LogUtils.logWarn("The begin offsets of event mentions are different: begin1 " + begin1
//                        + ", begin2 " + begin2);
//            }
//            return false;
//        }
//
//        int end1 = evm1.getEnd();
//        int end2 = evm2.getEnd();
//        if (end1 != end2) {
//            if (printWarning) {
//                LogUtils.logWarn("The end offsets of event mentions are different: end1 " + end1
//                        + ", end2 " + end2);
//            }
//            return false;
//        }
//
//        String evm1Str = evm1.getCoveredText();
//        String evm2Str = evm2.getCoveredText();
//        if (!evm1Str.equals(evm2Str)) {
//            if (printWarning) {
//                LogUtils.logWarn("Strings of event mentions are different: evm1 [" + evm1Str + "], evm2 ["
//                        + evm2Str + "]");
//            }
//            return false;
//        }
//
//        return true;
//    }
//
//    /**
//     * Checks whether the specified two lists of event mentions hold a list of equivalent event
//     * mentions annotation. By "equivalent", we mean that the string and offsets of the two event
//     * mentions are the same. Note that two event mentions may come from different views.
//     *
//     * @param evmList1
//     * @param evmList2
//     * @param printWarning
//     * @return
//     */
//    public static boolean areSameListOfEventMentionAnnotations(List<EventMention> evmList1,
//                                                               List<EventMention> evmList2, boolean printWarning) {
//        if (evmList1 == null && evmList2 == null) {
//            if (printWarning) {
//                LogUtils.logWarn("The both lists of event mentions are null.");
//            }
//            return true;
//        } else if (evmList1 == null) {
//            if (printWarning) {
//                LogUtils.logWarn("The first list is null whereas the second one isn't.");
//            }
//            return false;
//        } else if (evmList2 == null) {
//            if (printWarning) {
//                LogUtils.logWarn("The second list is null whereas the first one isn't.");
//            }
//            return false;
//        }
//
//        int evmList1Size = evmList1.size();
//        int evmList2Size = evmList2.size();
//        if (evmList1Size != evmList2Size) {
//            if (printWarning) {
//                LogUtils.logWarn("# of event mentions are different: # of first list " + evmList1Size
//                        + ", # of second list " + evmList2Size);
//            }
//            return false;
//        }
//
//        for (int i = 0; i < evmList1Size; i++) {
//            if (!areSameListOfEventMentionAnnotations(evmList1.get(i), evmList2.get(i), printWarning)) {
//                return false;
//            }
//        }
//
//        return true;
//    }

}
