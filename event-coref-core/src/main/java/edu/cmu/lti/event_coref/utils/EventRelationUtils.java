package edu.cmu.lti.event_coref.utils;

import edu.cmu.lti.event_coref.type.EventCoreferenceCluster;
import edu.cmu.lti.event_coref.type.EventCoreferenceRelation;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.utils.EventCoreferenceConstants.DetailedEventCoreferenceRelationType;
import edu.cmu.lti.event_coref.utils.EventCoreferenceConstants.EventCoreferenceRelationType;
import edu.cmu.lti.event_coref.utils.EventCoreferenceConstants.GoldStandardEventCoreferenceRelationType;
import edu.cmu.lti.utils.general.StringUtils;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.cas.FSList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class provides useful utilities specifically for event relations.
 *
 * @author Jun Araki
 */
public class EventRelationUtils {

    /**
     * Returns the relation between the specified two event mentions. Note that relations are defined
     * in a mutually exclusive way.
     *
     * @param fullCoreferenceClusterList
     * @param memberCoreferenceClusterList
     * @param subeventCoreferenceClusterList
     * @param event1
     * @param event2
     * @param getProbagatedRelations
     * @return the relation between the specified two event mentions
     */
    public static DetailedEventCoreferenceRelationType getRelation(
            List<EventCoreferenceCluster> fullCoreferenceClusterList,
            List<EventCoreferenceCluster> memberCoreferenceClusterList,
            List<EventCoreferenceCluster> subeventCoreferenceClusterList, EventMention event1,
            EventMention event2, boolean getProbagatedRelations) {

        if (EventRelationUtils.hasFullCoreferenceRelation(fullCoreferenceClusterList, event1, event2)) {
            return DetailedEventCoreferenceRelationType.FULL;
        }

        if (EventRelationUtils.hasSubeventForwardRelation(fullCoreferenceClusterList,
                subeventCoreferenceClusterList, event1, event2, getProbagatedRelations)) {
            return DetailedEventCoreferenceRelationType.SUBEVENT_FORWARD;
        }

        if (EventRelationUtils.hasSubeventBackwardRelation(fullCoreferenceClusterList,
                subeventCoreferenceClusterList, event1, event2, getProbagatedRelations)) {
            return DetailedEventCoreferenceRelationType.SUBEVENT_BACKWARD;
        }

        if (EventRelationUtils.hasSubeventSisterRelation(fullCoreferenceClusterList,
                subeventCoreferenceClusterList, event1, event2, getProbagatedRelations)) {
            return DetailedEventCoreferenceRelationType.SUBEVENT_SISTER;
        }

        if (EventRelationUtils.hasMemberForwardRelation(fullCoreferenceClusterList,
                memberCoreferenceClusterList, event1, event2, getProbagatedRelations)) {
            return DetailedEventCoreferenceRelationType.MEMBER_FORWARD;
        }

        if (EventRelationUtils.hasMemberBackwardRelation(fullCoreferenceClusterList,
                memberCoreferenceClusterList, event1, event2, getProbagatedRelations)) {
            return DetailedEventCoreferenceRelationType.MEMBER_BACKWARD;
        }

        if (EventRelationUtils.hasMemberSisterRelation(fullCoreferenceClusterList,
                memberCoreferenceClusterList, event1, event2, getProbagatedRelations)) {
            return DetailedEventCoreferenceRelationType.MEMBER_SISTER;
        }

        return DetailedEventCoreferenceRelationType.NO;
    }

    /**
     * Checks whether the specified two event mentions have a full coreference relation. Note that
     * this method uses just event coreference cluster information.
     *
     * @param fullCoreferenceClusterList
     * @param event1
     * @param event2
     * @return true if the specified two event mentions have a full coreference relation; false
     * otherwise
     */
    public static boolean hasFullCoreferenceRelation(
            List<EventCoreferenceCluster> fullCoreferenceClusterList, EventMention event1,
            EventMention event2) {
        for (EventCoreferenceCluster ecc : fullCoreferenceClusterList) {
            if (EventCoreferenceClusterUtils.hasChildEventMention(ecc, event1)
                    && EventCoreferenceClusterUtils.hasChildEventMention(ecc, event2)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether the specified two event mentions have a member relation. Note that this method
     * uses just event coreference cluster information.
     *
     * @param fullCoreferenceClusterList
     * @param event1
     * @param event2
     * @param getProbagatedRelations
     * @return true if the specified two event mentions have a member relation; false otherwise
     */
    public static boolean hasMemberRelation(List<EventCoreferenceCluster> fullCoreferenceClusterList,
                                            List<EventCoreferenceCluster> memberCoreferenceClusterList, EventMention event1,
                                            EventMention event2, boolean getProbagatedRelations) {
        return (hasMemberForwardRelation(fullCoreferenceClusterList, memberCoreferenceClusterList,
                event1, event2, getProbagatedRelations) || hasMemberBackwardRelation(
                fullCoreferenceClusterList, memberCoreferenceClusterList, event1, event2,
                getProbagatedRelations));
    }

    /**
     * Checks whether the specified two event mentions have a member forward relation. Note that this
     * method uses just event coreference cluster information.
     *
     * @param fullCoreferenceClusterList
     * @param memberCoreferenceClusterList
     * @param event1
     * @param event2
     * @param getProbagatedRelations
     * @return true if the specified two event mentions have a member forward relation; false
     * otherwise
     */
    public static boolean hasMemberForwardRelation(
            List<EventCoreferenceCluster> fullCoreferenceClusterList,
            List<EventCoreferenceCluster> memberCoreferenceClusterList, EventMention event1,
            EventMention event2, boolean getProbagatedRelations) {
        return hasPartialForwardRelation(fullCoreferenceClusterList, memberCoreferenceClusterList,
                event1, event2, getProbagatedRelations);
    }

    /**
     * Checks whether the specified two event mentions have a member backward relation. Note that this
     * method uses just event coreference cluster information.
     *
     * @param fullCoreferenceClusterList
     * @param memberCoreferenceClusterList
     * @param event1
     * @param event2
     * @param getProbagatedRelations
     * @return true if the specified two event mentions have a member backward relation; false
     * otherwise
     */
    public static boolean hasMemberBackwardRelation(
            List<EventCoreferenceCluster> fullCoreferenceClusterList,
            List<EventCoreferenceCluster> memberCoreferenceClusterList, EventMention event1,
            EventMention event2, boolean getProbagatedRelations) {
        return hasPartialBackwardRelation(fullCoreferenceClusterList, memberCoreferenceClusterList,
                event1, event2, getProbagatedRelations);
    }

    /**
     * Checks whether the specified two event mentions have a member sister relation. Note that this
     * method uses just event coreference cluster information.
     *
     * @param fullCoreferenceClusterList
     * @param memberCoreferenceClusterList
     * @param event1
     * @param event2
     * @param getProbagatedRelations
     * @return true if the specified two event mentions have a member sister relation; false otherwise
     */
    public static boolean hasMemberSisterRelation(
            List<EventCoreferenceCluster> fullCoreferenceClusterList,
            List<EventCoreferenceCluster> memberCoreferenceClusterList, EventMention event1,
            EventMention event2, boolean getProbagatedRelations) {
        return hasPartialSisterRelation(fullCoreferenceClusterList, memberCoreferenceClusterList, event1,
                event2, getProbagatedRelations);
    }

    /**
     * Checks whether the specified two event mentions have a subevent relation. Note that this method
     * uses just event coreference cluster information.
     *
     * @param fullCoreferenceClusterList
     * @param subeventCoreferenceClusterList
     * @param event1
     * @param event2
     * @param getProbagatedRelations
     * @return true if the specified two event mentions have a subevent relation; false otherwise
     */
    public static boolean hasSubeventRelation(
            List<EventCoreferenceCluster> fullCoreferenceClusterList,
            List<EventCoreferenceCluster> subeventCoreferenceClusterList, EventMention event1,
            EventMention event2, boolean getProbagatedRelations) {
        return (hasSubeventForwardRelation(fullCoreferenceClusterList, subeventCoreferenceClusterList,
                event1, event2, getProbagatedRelations) || hasSubeventBackwardRelation(
                fullCoreferenceClusterList, subeventCoreferenceClusterList, event1, event2,
                getProbagatedRelations));
    }

    /**
     * Checks whether the specified two event mentions have a subevent forward relation. Note that
     * this method uses just event coreference cluster information.
     *
     * @param fullCoreferenceClusterList
     * @param subeventCoreferenceClusterList
     * @param event1
     * @param event2
     * @param getProbagatedRelations
     * @return true if the specified two event mentions have a subevent forward relation; false
     * otherwise
     */
    public static boolean hasSubeventForwardRelation(
            List<EventCoreferenceCluster> fullCoreferenceClusterList,
            List<EventCoreferenceCluster> subeventCoreferenceClusterList, EventMention event1,
            EventMention event2, boolean getProbagatedRelations) {
        return hasPartialForwardRelation(fullCoreferenceClusterList, subeventCoreferenceClusterList,
                event1, event2, getProbagatedRelations);
    }

    /**
     * Checks whether the specified two event mentions have a subevent backward relation. Note that
     * this method uses just event coreference cluster information.
     *
     * @param fullCoreferenceClusterList
     * @param subeventCoreferenceClusterList
     * @param event1
     * @param event2
     * @param getProbagatedRelations
     * @return true if the specified two event mentions have a subevent backward relation; false
     * otherwise
     */
    public static boolean hasSubeventBackwardRelation(
            List<EventCoreferenceCluster> fullCoreferenceClusterList,
            List<EventCoreferenceCluster> subeventCoreferenceClusterList, EventMention event1,
            EventMention event2, boolean getProbagatedRelations) {
        return hasPartialBackwardRelation(fullCoreferenceClusterList, subeventCoreferenceClusterList,
                event1, event2, getProbagatedRelations);
    }

    /**
     * Checks whether the specified two event mentions have a subevent sister relation. Note that this
     * method uses just event coreference cluster information.
     *
     * @param fullCoreferenceClusterList
     * @param subeventCoreferenceClusterList
     * @param event1
     * @param event2
     * @param getProbagatedRelations
     * @return true if the specified two event mentions have a subevent sister relation; false
     * otherwise
     */
    public static boolean hasSubeventSisterRelation(
            List<EventCoreferenceCluster> fullCoreferenceClusterList,
            List<EventCoreferenceCluster> subeventCoreferenceClusterList, EventMention event1,
            EventMention event2, boolean getProbagatedRelations) {
        return hasPartialSisterRelation(fullCoreferenceClusterList, subeventCoreferenceClusterList,
                event1, event2, getProbagatedRelations);
    }

    public static boolean hasPartialForwardRelation(
            List<EventCoreferenceCluster> fullCoreferenceClusterList,
            List<EventCoreferenceCluster> partialCoreferenceClusterList, EventMention parent,
            EventMention child, boolean getProbagatedRelations) {
        // This is a simple case that the two event mentions are in a partial coreference cluster.
        for (EventCoreferenceCluster ecc : partialCoreferenceClusterList) {
            if (EventCoreferenceClusterUtils.hasParentEventMention(ecc, parent)
                    && EventCoreferenceClusterUtils.hasChildEventMention(ecc, child)) {
                return true;
            }
        }

        if (!getProbagatedRelations) {
            return false;
        }

        // This is another simple case that the two event mentions are in a full coreference cluster.
        for (EventCoreferenceCluster ecc : fullCoreferenceClusterList) {
            if (EventCoreferenceClusterUtils.hasChildEventMention(ecc, parent)
                    && EventCoreferenceClusterUtils.hasChildEventMention(ecc, child)) {
                return false;
            }
        }

        // A complicated case that the two event mentions are in a propagated relation from a
        // combination of full and partial event clusters

        // Here we assume that a child has only one parent.
        EventMention _parent = null;
        for (EventCoreferenceCluster pecc : partialCoreferenceClusterList) {
            if (EventCoreferenceClusterUtils.hasChildEventMention(pecc, child)) {
                // Found a partial coref cluster containing the child.
                _parent = pecc.getParentEventMention();
                break;
            }
        }
        if (_parent == null) {
            return false;
        }

        // Now Looks for a parent that is in a full coref cluster with its parent, if any.
        for (EventCoreferenceCluster fecc : fullCoreferenceClusterList) {
            if (EventCoreferenceClusterUtils.hasChildEventMention(fecc, parent)
                    && EventCoreferenceClusterUtils.hasChildEventMention(fecc, _parent)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasPartialBackwardRelation(
            List<EventCoreferenceCluster> fullCoreferenceClusterList,
            List<EventCoreferenceCluster> partialCoreferenceClusterList, EventMention parent,
            EventMention child, boolean getProbagatedRelations) {
        return hasPartialForwardRelation(fullCoreferenceClusterList, partialCoreferenceClusterList,
                child, parent, getProbagatedRelations);
    }

    public static boolean hasPartialSisterRelation(
            List<EventCoreferenceCluster> fullCoreferenceClusterList,
            List<EventCoreferenceCluster> partialCoreferenceClusterList, EventMention sister1,
            EventMention sister2, boolean getProbagatedRelations) {
        // This is a simple case that the two event mentions are in a partial coreference cluster.
        for (EventCoreferenceCluster ecc : partialCoreferenceClusterList) {
            if (EventCoreferenceClusterUtils.hasChildEventMention(ecc, sister1)
                    && EventCoreferenceClusterUtils.hasChildEventMention(ecc, sister2)) {
                return true;
            }
        }

        if (!getProbagatedRelations) {
            return false;
        }

        // This is another simple case that the two event mentions are in a full coreference cluster.
        for (EventCoreferenceCluster ecc : fullCoreferenceClusterList) {
            if (EventCoreferenceClusterUtils.hasChildEventMention(ecc, sister1)
                    && EventCoreferenceClusterUtils.hasChildEventMention(ecc, sister2)) {
                return false;
            }
        }

        // A complicated case that the two event mentions are in a propagated relation from combinations
        // of full and partial event clusters

        // Here we assume that a child has only one parent.
        EventMention _parent1 = null;
        EventMention _parent2 = null;
        for (EventCoreferenceCluster pecc : partialCoreferenceClusterList) {
            if (EventCoreferenceClusterUtils.hasChildEventMention(pecc, sister1)) {
                // Found a partial coref cluster containing the child.
                _parent1 = pecc.getParentEventMention();
            }
            if (EventCoreferenceClusterUtils.hasChildEventMention(pecc, sister2)) {
                // Found a partial coref cluster containing the child.
                _parent2 = pecc.getParentEventMention();
            }

            if (_parent1 != null && _parent2 != null) {
                break;
            }
        }
        if (_parent1 == null || _parent2 == null) {
            return false;
        }

        // Now Looks for a full coref cluster that contains two parent event mentions.
        for (EventCoreferenceCluster fecc : fullCoreferenceClusterList) {
            if (EventCoreferenceClusterUtils.hasChildEventMention(fecc, _parent1)
                    && EventCoreferenceClusterUtils.hasChildEventMention(fecc, _parent2)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isFullOrPartialCoreferenceRelation(EventCoreferenceRelation relation) {
        return isFullOrPartialEventCoreferenceRelationType(relation.getRelationType());
    }

    public static boolean isFullCoreferenceRelation(EventCoreferenceRelation relation) {
        return isFullEventCoreferenceRelationType(relation.getRelationType());
    }

    public static boolean isPartialCoreferenceRelation(EventCoreferenceRelation relation) {
        return isPartialCoreferenceEventCoreferenceRelationType(relation.getRelationType());
    }

    public static boolean isSubeventCoreferenceRelation(EventCoreferenceRelation relation) {
        return isSubeventCoreferenceEventCoreferenceRelationType(relation.getRelationType());
    }

    public static boolean isMemberCoreferenceRelation(EventCoreferenceRelation relation) {
        return isMemberCoreferenceEventCoreferenceRelationType(relation.getRelationType());
    }

    public static boolean isReportingRelation(EventCoreferenceRelation relation) {
        return isReportingEventCoreferenceRelationType(relation.getRelationType());
    }

    /**
     * Checks whether the specified two event mentions has the specified coreference relation.
     *
     * @param event1
     * @param event2
     * @param relations
     * @return true if the specified two event mentions has the specified coreference relation; false
     * otherwise.
     */
    public static boolean hasCoreferenceRelation(EventMention event1, EventMention event2,
                                                 List<EventCoreferenceRelation> relations) {
        for (EventCoreferenceRelation relation : relations) {
            EventMention fromEvm = relation.getFromEventMention();
            EventMention toEvm = relation.getToEventMention();

            if (EventMentionUtils.isSameEventMention(event1, fromEvm)
                    && EventMentionUtils.isSameEventMention(event2, toEvm)) {
                return true;
            }
            if (EventMentionUtils.isSameEventMention(event2, fromEvm)
                    && EventMentionUtils.isSameEventMention(event1, toEvm)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether the specified two event mentions has the specified coreference relation, using
     * the specified full corefenrece clusters.
     *
     * @param event1
     * @param event2
     * @param fullCorefEventClusters
     * @return true if the specified two event mentions has the expanded subevent coreference
     * relation; false otherwise.
     */
    public static boolean hasExpandedCoreferenceRelation(EventMention event1, EventMention event2,
                                                         List<EventCoreferenceRelation> relations,
                                                         List<EventCoreferenceCluster> fullCorefEventClusters) {

        if (hasCoreferenceRelation(event1, event2, relations)) {
            return true;
        }

        EventCoreferenceCluster clusterI = EventCoreferenceClusterUtils
                .findEventCoreferenceClusterContainingChildEventMention(fullCorefEventClusters, event1);
        EventCoreferenceCluster clusterJ = EventCoreferenceClusterUtils
                .findEventCoreferenceClusterContainingChildEventMention(fullCorefEventClusters, event2);

        if (clusterI == null && clusterJ == null) {
            // There is no expansion.
            return false;
        }

        Collection<EventMention> eventMentionsI, eventMentionsJ;
        eventMentionsI = new ArrayList<EventMention>();
        eventMentionsJ = new ArrayList<EventMention>();
        if (clusterI == null) {
            eventMentionsI.add(event1);
        } else if (clusterJ == null) {
            eventMentionsJ.add(event1);
        } else {
            FSList eventMentionListI = clusterI.getChildEventMentions();
            FSList eventMentionListJ = clusterJ.getChildEventMentions();
            eventMentionsI = FSCollectionFactory.create(eventMentionListI, EventMention.class);
            eventMentionsJ = FSCollectionFactory.create(eventMentionListJ, EventMention.class);
        }

        for (EventMention exEvmI : eventMentionsI) {
            for (EventMention exEvmJ : eventMentionsJ) {
                if (EventMentionUtils.isSameEventMention(exEvmI, event1)
                        && EventMentionUtils.isSameEventMention(exEvmJ, exEvmJ)) {
                    // We already know that this pair does not have the relation
                    continue;
                }

                if (hasCoreferenceRelation(event1, event2, relations)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks whether the specified relation type is of full or partial coreference.
     *
     * @param relationType
     * @return true if the specified coreference type is of full or partial coreference; false
     * otherwise
     */
    public static boolean isFullOrPartialEventCoreferenceRelationType(String relationType) {
        if (isFullEventCoreferenceRelationType(relationType)
                || isPartialCoreferenceEventCoreferenceRelationType(relationType)) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether the specified relation type is of full coreference.
     *
     * @param relationType
     * @return true if the specified coreference type is of full coreference; false otherwise
     */
    public static boolean isFullEventCoreferenceRelationType(String relationType) {
        if (StringUtils.isNullOrEmptyString(relationType)) {
            return false;
        }

        if (relationType.equals(EventCoreferenceRelationType.FULL.toString())) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether the specified coreference type is of partial coreference.
     *
     * @param EventCoreferenceRelationType
     * @return true if the specified coreference type is of partial coreference; false otherwise
     */
    public static boolean isPartialCoreferenceEventCoreferenceRelationType(
            String EventCoreferenceRelationType) {
        if (isSubeventCoreferenceEventCoreferenceRelationType(EventCoreferenceRelationType)
                || isMemberCoreferenceEventCoreferenceRelationType(EventCoreferenceRelationType)) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether the specified relation type is the specified target relation type.
     *
     * @param relationType
     * @param targetEventCoreferenceRelationType
     * @return true if the specified coreference type is the specified target relation type; false
     * otherwise
     */
    public static boolean isSpecifiedEventCoreferenceRelationType(String relationType,
                                                                  GoldStandardEventCoreferenceRelationType targetEventCoreferenceRelationType) {
        if (StringUtils.isNullOrEmptyString(relationType)) {
            return false;
        }

        if (relationType.equals(targetEventCoreferenceRelationType.toString())) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether the specified relation type is of member coreference.
     *
     * @param relationType
     * @return true if the specified coreference type is of member coreference; false otherwise
     */
    public static boolean isMemberCoreferenceEventCoreferenceRelationType(String relationType) {
        return isSpecifiedEventCoreferenceRelationType(relationType,
                GoldStandardEventCoreferenceRelationType.MEMBER);
    }

    /**
     * Checks whether the specified relation type is of subevent coreference.
     *
     * @param relationType
     * @return true if the specified coreference type is of subevent coreference; false otherwise
     */
    public static boolean isSubeventCoreferenceEventCoreferenceRelationType(String relationType) {
        return isSpecifiedEventCoreferenceRelationType(relationType,
                GoldStandardEventCoreferenceRelationType.SUBEVENT);
    }

    /**
     * Checks whether the specified coreference type is of no coreference.
     *
     * @param relationType
     * @return
     */
    public static boolean isNoCoreferenceEventCoreferenceRelationType(String relationType) {
        if (StringUtils.isNullOrEmptyString(relationType)) {
            return false;
        }

        if (!isFullEventCoreferenceRelationType(relationType)
                && !isPartialCoreferenceEventCoreferenceRelationType(relationType)) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether the specified relation type is of reporting.
     *
     * @param relationType
     * @return true if the specified coreference type is of reporting; false otherwise
     */
    public static boolean isReportingEventCoreferenceRelationType(String relationType) {
        return isSpecifiedEventCoreferenceRelationType(relationType,
                GoldStandardEventCoreferenceRelationType.REPORTING);
    }

}
