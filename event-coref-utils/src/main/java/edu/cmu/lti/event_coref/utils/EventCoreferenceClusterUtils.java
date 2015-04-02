package edu.cmu.lti.event_coref.utils;

import edu.cmu.lti.event_coref.model.EventCorefConstants;
import edu.cmu.lti.event_coref.type.EventCoreferenceCluster;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.utils.EventCoreferenceConstants.EventCoreferenceClusterType;
import edu.cmu.lti.utils.general.MathUtils;
import org.apache.uima.jcas.JCas;
import org.uimafit.util.FSCollectionFactory;
import org.uimafit.util.JCasUtil;

import java.util.*;

/**
 * This class provides useful utilities specifically for event coreference clusters. It assumes that
 * child event mentions do not contain a parent event mention.
 * 
 * @author Jun Araki
 */
public class EventCoreferenceClusterUtils {

  /**
   * Checks whether the specified two event corefernece clusters are identical.
   * 
   * @param eccI
   * @param eccJ
   * @return true if the specified two event corefernece clusters are identical; false otherwise
   */
  public static boolean isSameEventCoreferenceCluster(EventCoreferenceCluster eccI,
          EventCoreferenceCluster eccJ) {
    if (eccI == null || eccJ == null) {
      return false;
    }

    if (!eccI.getClusterType().equals(eccJ.getClusterType())) {
      return false;
    }

    if (!EventMentionUtils.isSameEventMentionCollection(getChildEventMentions(eccI),
            getChildEventMentions(eccJ))) {
      return false;
    }

    EventMention parentEventMentionI = eccI.getParentEventMention();
    EventMention parentEventMentionJ = eccJ.getParentEventMention();
    if (parentEventMentionI == null && parentEventMentionJ == null) {
      return true;
    }

    if (!EventMentionUtils.isSameEventMention(parentEventMentionI, parentEventMentionJ)) {
      return false;
    }

    return true;
  }

  public static boolean containsEventCoreferenceCluster(List<EventCoreferenceCluster> eccList,
          EventCoreferenceCluster eventCoreferenceCluster) {
    for (EventCoreferenceCluster ecc : eccList) {
      if (isSameEventCoreferenceCluster(ecc, eventCoreferenceCluster)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns the number of event mentions in the specified event coreference cluster.
   * 
   * @param ecc
   * @return the number of event mentions in the specified event coreference cluster
   */
  public static int getClusterSize(EventCoreferenceCluster ecc) {
    int size = getChildEventMentions(ecc).size();
    if (hasParentEventMention(ecc)) {
      size += 1;
    }

    return size;
  }

  /**
   * Returns a collection of child event mentions of the specified event coreference cluster.
   * 
   * @param ecc
   * @return a collection of child event mentions of the specified event coreference cluster
   */
  public static Collection<EventMention> getChildEventMentions(EventCoreferenceCluster ecc) {
    if (ecc == null) {
      return null;
    }

    if (!hasChildEventMention(ecc)) {
      return null;
    }

    return FSCollectionFactory.create(ecc.getChildEventMentions(), EventMention.class);
  }

  /**
   * Returns the number of child event mentions of the specified event coreference cluster.
   * 
   * @param ecc
   * @return the number of child event mentions of the specified event coreference cluster
   */
  public static int getNumOfChildEventMentions(EventCoreferenceCluster ecc) {
    return getChildEventMentions(ecc).size();
  }

  /**
   * Adds the specified event mention to the specified event coreference cluster.
   * 
   * @param aJCas
   * @param ecc
   * @param evm
   * @param isParent
   */
  public static void addEventMention(JCas aJCas, EventCoreferenceCluster ecc, EventMention evm,
          boolean isParent) {
    if (isParent) {
      ecc.setParentEventMention(evm);
      return;
    }

    Collection<EventMention> eventMentions = getChildEventMentions(ecc);
    if (eventMentions == null) {
      eventMentions = new ArrayList<EventMention>();
      eventMentions.add(evm);
    } else {
      Collection<EventMention> newEventMentions = new ArrayList<EventMention>();
      newEventMentions.addAll(eventMentions);
      newEventMentions.add(evm);
      eventMentions = newEventMentions;
    }

    ecc.setChildEventMentions(FSCollectionFactory.createFSList(aJCas, eventMentions));
  }

  /**
   * Adds all the specified event mentions to the specified event coreference cluster.
   * 
   * @param aJCas
   * @param ecc
   * @param evms
   */
  public static void addAllEventMentions(JCas aJCas, EventCoreferenceCluster ecc,
          Collection<EventMention> evms) {
    Collection<EventMention> eventMentions = getChildEventMentions(ecc);
    if (eventMentions == null) {
      eventMentions = new ArrayList<EventMention>();
    }
    eventMentions.addAll(evms);

    ecc.setChildEventMentions(FSCollectionFactory.createFSList(aJCas, eventMentions));
  }

  /**
   * Returns the event coreference cluster that contains the specified event mention.
   * 
   * @param eccs
   * @param evm
   * @return the event coreference cluster that contains the specified event mention
   */
  public static EventCoreferenceCluster findEventCoreferenceClusterContainingEventMention(
          Collection<EventCoreferenceCluster> eccs, EventMention evm) {
    EventCoreferenceCluster ecc = findEventCoreferenceClusterContainingParentEventMention(eccs, evm);
    if (ecc == null) {
      ecc = findEventCoreferenceClusterContainingChildEventMention(eccs, evm);
    }

    return ecc;
  }

  public static EventCoreferenceCluster findEventCoreferenceClusterContainingParentEventMention(
          Collection<EventCoreferenceCluster> eccs, EventMention evm) {
    for (EventCoreferenceCluster ecc : eccs) {
      if (containsParentEventMention(ecc, evm)) {
        return ecc;
      }
    }

    return null;
  }

  public static EventCoreferenceCluster findEventCoreferenceClusterContainingChildEventMention(
          Collection<EventCoreferenceCluster> eccs, EventMention evm) {
    for (EventCoreferenceCluster ecc : eccs) {
      if (containsChildEventMention(ecc, evm)) {
        return ecc;
      }
    }

    return null;
  }

  /**
   * Checks whether the specified two event mentions are in the same event cluster.
   * 
   * @param ecc
   * @param evmI
   * @param evmJ
   * @return true if the specified two event mentions are in the same event cluster; false otherwise
   */
  public static boolean inSameEventCluster(EventCoreferenceCluster ecc, EventMention evmI,
          EventMention evmJ) {
    if (ecc == null || evmI == null || evmJ == null) {
      return false;
    }

    if (containsChildEventMention(ecc, evmI) && containsChildEventMention(ecc, evmJ)) {
      return true;
    }

    return false;
  }

  public static int getNumOfFullEventCorefRelations(EventCoreferenceCluster ecc) {
    if (!isFullCoreferenceCluster(ecc)) {
      return 0;
    }

    // The number of full event coreference relations is the number of combinations of an event
    // mention pair.
    int numOfChildEventMentions = getNumOfChildEventMentions(ecc);
    if (numOfChildEventMentions <= 1) {
      return 0;
    }

    return MathUtils.getCombination(numOfChildEventMentions, 2).intValue();
  }

  public static int getNumOfPartialEventCorefRelations(EventCoreferenceCluster ecc) {
    if (!isPartialCoreferenceCluster(ecc)) {
      return 0;
    }

    // The number of partial event coreference relations is the number of child event mentions.
    return getNumOfChildEventMentions(ecc);
  }

  public static int getNumOfPartialEventCorefSisterRelations(EventCoreferenceCluster ecc) {
    if (!isPartialCoreferenceCluster(ecc)) {
      return 0;
    }

    int numOfChildEventMentions = getNumOfChildEventMentions(ecc);
    if (numOfChildEventMentions <= 1) {
      return 0;
    }

    return MathUtils.getCombination(numOfChildEventMentions, 2).intValue();
  }

  /**
   * Checks whether the specified two event mentions are in the same event cluster within the
   * specified list of event coreference clusters.
   * 
   * @param eccList
   * @param evmI
   * @param evmJ
   * @return true if the specified two event mentions are in the same event cluster within the
   *         specified list of event coreference clusters; false otherwise
   */
  public static boolean inSameEventCluster(List<EventCoreferenceCluster> eccList,
          EventMention evmI, EventMention evmJ) {
    if (eccList == null || evmI == null || evmJ == null) {
      return false;
    }

    for (EventCoreferenceCluster ecc : eccList) {
      if (inSameEventCluster(ecc, evmI, evmJ)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks whether the specified event mention is a singleton.
   * 
   * @param eccList
   * @param evm
   * @return true if the specified event mention is a singleton; false otherwise
   */
  public static boolean isSingleton(List<EventCoreferenceCluster> eccList, EventMention evm) {
    for (EventCoreferenceCluster ecc : eccList) {
      if (containsEventMention(ecc, evm)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Checks whether the specified two event mentions have a parent-child relation. Note that this
   * method does not say which one is a parent and the other is its child.
   * 
   * @param eccList
   * @param evmI
   * @param evmJ
   * @return true if the specified two event mentions have a parent-child relation; false otherwise.
   */
  public static boolean hasParentChildRelation(List<EventCoreferenceCluster> eccList,
          EventMention evmI, EventMention evmJ) {
    for (EventCoreferenceCluster ecc : eccList) {
      if (containsParentAndChildEventMentions(ecc, evmI, evmJ)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks whether the specified two event mentions have a parent-child relation in the specified
   * event coreference cluster. Note that this method does not say which one is a parent and the
   * other is its child.
   * 
   * @param ecc
   * @param evmI
   * @param evmJ
   * @return true if the specified two event mentions have a parent-child relation in the specified
   *         event coreference cluster; false otherwise.
   */
  public static boolean containsParentAndChildEventMentions(EventCoreferenceCluster ecc,
          EventMention evmI, EventMention evmJ) {
    if (containsParentEventMention(ecc, evmI) && containsChildEventMention(ecc, evmJ)) {
      return true;
    }
    if (containsParentEventMention(ecc, evmJ) && containsChildEventMention(ecc, evmI)) {
      return true;
    }

    return false;
  }

  /**
   * Checks whether the specified event coreference cluster contains the specified event mention.
   * 
   * @param ecc
   * @param evm
   * @return true if the specified event coreference cluster contains the specified event mention;
   *         false otherwise
   */
  public static boolean containsEventMention(EventCoreferenceCluster ecc, EventMention evm) {
    return (hasParentEventMention(ecc, evm) || hasChildEventMention(ecc, evm));
  }

  public static boolean containsParentEventMention(EventCoreferenceCluster ecc, EventMention evm) {
    return hasParentEventMention(ecc, evm);
  }

  public static boolean containsChildEventMention(EventCoreferenceCluster ecc, EventMention evm) {
    return hasChildEventMention(ecc, evm);
  }

  /**
   * Checks whether the specified event coreference cluster has the specified event mention as
   * either a parent or a child.
   * 
   * @param ecc
   * @param evm
   * @return true if the specified event coreference cluster has the specified event mention; false
   *         otherwise
   */
  public static boolean hasEventMention(EventCoreferenceCluster ecc, EventMention evm) {
    return (hasParentEventMention(ecc, evm) || hasChildEventMention(ecc, evm));
  }

  /**
   * Checks whether the specified event coreference cluster has any parent event mention.
   * 
   * @param ecc
   * @return true if the specified event coreference cluster has any parent event mention; false
   *         otherwise
   */
  public static boolean hasParentEventMention(EventCoreferenceCluster ecc) {
    if (ecc == null) {
      return false;
    }

    return (ecc.getParentEventMention() != null);
  }

  /**
   * Checks whether the specified event coreference cluster has the specified event mention as its
   * parent.
   * 
   * @param ecc
   * @param evm
   * @return true if the specified event coreference cluster has the specified event mention as its
   *         parent; false otherwise
   */
  public static boolean hasParentEventMention(EventCoreferenceCluster ecc, EventMention evm) {
    if (ecc == null || evm == null) {
      return false;
    }

    EventMention parentEvm = ecc.getParentEventMention();
    if (parentEvm == null) {
      return false;
    }

    if (EventMentionUtils.isSameEventMention(evm, parentEvm)) {
      return true;
    }

    return false;
  }

  /**
   * Checks whether the specified event coreference cluster has any child event mentions.
   * 
   * @param ecc
   * @return
   */
  public static boolean hasChildEventMention(EventCoreferenceCluster ecc) {
    if (ecc == null) {
      return false;
    }

    return (ecc.getChildEventMentions() != null);
  }

  /**
   * Checks whether the specified event coreference cluster has the specified event mention as its
   * child.
   * 
   * @param ecc
   * @param evm
   * @return true if the specified event coreference cluster has the specified event mention as its
   *         child; false otherwise
   */
  public static boolean hasChildEventMention(EventCoreferenceCluster ecc, EventMention evm) {
    if (ecc == null || evm == null) {
      return false;
    }

    Collection<EventMention> childEventMentions = getChildEventMentions(ecc);
    if (childEventMentions == null) {
      return false;
    }

    for (EventMention childEventMention : childEventMentions) {
      if (EventMentionUtils.isSameEventMention(evm, childEventMention)) {
        return true;
      }
    }

    return false;
  }

  public static boolean isFullCoreferenceCluster(EventCoreferenceCluster ecc) {
    return isSystemFullCoreferenceCluster(ecc);
  }

  public static boolean isPartialCoreferenceCluster(EventCoreferenceCluster ecc) {
    return (isMemberCoreferenceCluster(ecc) || isSubeventCoreferenceCluster(ecc));
  }

  public static boolean isMemberCoreferenceCluster(EventCoreferenceCluster ecc) {
    return isSpecifiedSystemCoreferenceCluster(ecc, EventCoreferenceClusterType.MEMBER);
  }

  public static boolean isSubeventCoreferenceCluster(EventCoreferenceCluster ecc) {
    return isSpecifiedSystemCoreferenceCluster(ecc, EventCoreferenceClusterType.SUBEVENT);
  }

  public static boolean isSystemFullCoreferenceCluster(EventCoreferenceCluster ecc) {
    return isSpecifiedSystemCoreferenceCluster(ecc, EventCoreferenceClusterType.FULL);
  }

  public static boolean isSystemPartialCoreferenceCluster(EventCoreferenceCluster ecc) {
    return (isMemberCoreferenceCluster(ecc) || isSubeventCoreferenceCluster(ecc));
  }

  public static boolean isSystemMemberCoreferenceCluster(EventCoreferenceCluster ecc) {
    return isSpecifiedSystemCoreferenceCluster(ecc, EventCoreferenceClusterType.MEMBER);
  }

  public static boolean isSystemSubeventCoreferenceCluster(EventCoreferenceCluster ecc) {
    return isSpecifiedSystemCoreferenceCluster(ecc, EventCoreferenceClusterType.SUBEVENT);
  }

  public static boolean isSpecifiedSystemCoreferenceCluster(EventCoreferenceCluster ecc,
          EventCoreferenceClusterType clusterType) {
    if (!ecc.getComponentId().startsWith("System")) {
      return false;
    }
    return clusterType.getSystemAnnotationStr().equals(ecc.getClusterType());
  }

  public static boolean isGoldStandardFullCoreferenceCluster(EventCoreferenceCluster ecc) {
    return isSpecifiedGoldStandardCoreferenceCluster(ecc, EventCoreferenceClusterType.FULL);
  }

  public static boolean isGoldStandardPartialCoreferenceCluster(EventCoreferenceCluster ecc) {
    return (isGoldStandardMemberCoreferenceCluster(ecc) || isGoldStandardSubeventCoreferenceCluster(ecc));
  }

  public static boolean isGoldStandardMemberCoreferenceCluster(EventCoreferenceCluster ecc) {
    return isSpecifiedGoldStandardCoreferenceCluster(ecc, EventCoreferenceClusterType.MEMBER);
  }

  public static boolean isGoldStandardSubeventCoreferenceCluster(EventCoreferenceCluster ecc) {
    return isSpecifiedGoldStandardCoreferenceCluster(ecc, EventCoreferenceClusterType.SUBEVENT);
  }

  public static boolean isSpecifiedGoldStandardCoreferenceCluster(EventCoreferenceCluster ecc,
          EventCoreferenceClusterType clusterType) {
    if (!ecc.getComponentId().equals("GoldStandard")) {
      return false;
    }
    return clusterType.getGoldStandardAnnotationStr().equals(ecc.getClusterType());
  }

  public static void printEventCoreferenceCluster(EventCoreferenceCluster ecc) {
    Collection<EventMention> eventMentions = getChildEventMentions(ecc);
    if (eventMentions == null) {
      return;
    }

    System.out.print("Event mentions: ");
    if (hasParentEventMention(ecc)) {
      EventMention parentEventMention = ecc.getParentEventMention();
      System.out.print("<");
      System.out.print(parentEventMention.getGoldStandardEventMentionId());
      System.out.print("> ");
    }

    for (EventMention eventMention : eventMentions) {
      if (EventCoreferenceClusterUtils.hasParentEventMention(ecc, eventMention)) {
        continue;
      }
      System.out.print(eventMention.getGoldStandardEventMentionId());
      System.out.print(" ");
    }
    System.out.println("");
  }

  /**
   * Return the sub event clusters annotated in the specified view and the component id match the
   * component id prefix
   * The returned clusters are organized as Map, where the parent is the key of the collection of
   * subevents
   * 
   * @param aJCas
   * @return Subevent clusters represented by Map
   */
  public static Map<EventMention, Collection<EventMention>> getSubEventMentionClusters(JCas aJCas,
          String componentIdPrefix) {
    Map<EventMention, Collection<EventMention>> subEventClusters = new HashMap<EventMention, Collection<EventMention>>();

    for (EventCoreferenceCluster cluster : JCasUtil.select(aJCas, EventCoreferenceCluster.class)) {
      if (cluster.getComponentId().startsWith(componentIdPrefix)
              && cluster.getClusterType().equals(EventCorefConstants.SUB_COREFERENCE_TYPE)) {
        subEventClusters.put(cluster.getParentEventMention(),
                FSCollectionFactory.create(cluster.getChildEventMentions(), EventMention.class));
      }
    }

    // System.out.println(subEventClusters.size() + " clusters found");
    return subEventClusters;
  }

}
