package edu.cmu.lti.event_coref.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.utils.type.ComponentAnnotation;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.uimafit.util.FSCollectionFactory;
import org.uimafit.util.JCasUtil;

import java.util.*;

//import edu.cmu.lti.event_coref.type.Agent;
//import edu.cmu.lti.event_coref.type.Patient;

public class APLUtils {
  public static final String AGENT_LINK_TYPE = "agent";

  public static final String PATIENT_LINK_TYPE = "patient";

  public static final String LOCATION_LINK_TYPE = "location";

  public static List<EntityBasedComponent> getAgents(EventMention evm) {
    return getComponentsFromLinks(evm.getAgentLinks());
  }

  public static List<EntityBasedComponent> getPatients(EventMention evm) {
    return getComponentsFromLinks(evm.getPatientLinks());
  }

  public static List<EntityBasedComponent> getLocations(EventMention evm) {
    return getComponentsFromLinks(evm.getLocationLinks());
  }

  public static List<EntityBasedComponent> getComponentsFromLinks(FSList componentLinks) {
    List<EntityBasedComponent> components = new ArrayList<EntityBasedComponent>();

    if (componentLinks != null) {
      for (EntityBasedComponentLink link : FSCollectionFactory.create(componentLinks,
              EntityBasedComponentLink.class)) {
        components.add(link.getComponent());
      }
    }

    return components;
  }

  public static <T extends EntityBasedComponent> List<T> getComponentsFromLinks(
          FSList componentLinks, Class<T> clazz) {
    List<T> components = new ArrayList<T>();

    if (componentLinks != null) {
      for (EntityBasedComponentLink link : FSCollectionFactory.create(componentLinks,
              EntityBasedComponentLink.class)) {
        components.add((T) link.getComponent());
      }
    }

    return components;
  }

  public static EntityBasedComponent createEntityBasedComponent(JCas aJCas, int begin, int end,
          String componentId) {
    EntityBasedComponent comp = new EntityBasedComponent(aJCas, begin, end);
    Word headWord = FanseDependencyUtils.findHeadWordFromDependency(comp);
    comp.setHeadWord(headWord);
    comp.setComponentId(componentId);
    comp.addToIndexes(aJCas);
    return comp;
  }

  public static <T extends EntityBasedComponent> List<EntityBasedComponentLink> addMultiLinksToEvent(
          JCas aJCas, EventMention evm, Collection<T> components, String linkType,
          String componentId) {
    List<EntityBasedComponentLink> attachedLinks = new ArrayList<EntityBasedComponentLink>();
    for (T component : components) {
      attachedLinks.add(createLink(aJCas, evm, component, linkType, componentId));
    }
    return attachedLinks;
  }

  public static <T extends EntityBasedComponent> EntityBasedComponentLink createLink(JCas aJCas,
          EventMention evm, T component, String linkType, String componentId) {
    EntityBasedComponentLink pLink = new EntityBasedComponentLink(aJCas);
    pLink.setEventMention(evm);
    pLink.setComponent(component);
    pLink.addToIndexes(aJCas);
    pLink.setComponentId(componentId);
    pLink.setLinkType(linkType);

    component.setComponentLinks(UimaConvenience.appendFSList(aJCas, component.getComponentLinks(),
            pLink, EntityBasedComponentLink.class));

    return pLink;
  }

  public static HashMultimap<Word, EntityMention> getWord2Entities(JCas aJCas) {
    // map from words to the list of surface form of its entity mentions
    HashMultimap<Word, EntityMention> word2EntityMentions = HashMultimap.create();
    for (EntityMention em : UimaConvenience.getAnnotationList(aJCas, EntityMention.class)) {
      List<Word> coveredWords = JCasUtil.selectCovered(Word.class, em);
      for (Word coveredWord : coveredWords) {
        word2EntityMentions.put(coveredWord, em);
      }
    }
    return word2EntityMentions;
  }

  /**
   * Make sure each EntityBasedComponent contains at least one entity
   * 
   * @param aJCas
   * @param componentId
   */
  public static void updateComponentEntityMentions(JCas aJCas, String componentId) {
    HashMultimap<Word, EntityMention> word2EntityMentions = getWord2Entities(aJCas);

    // associate APL to entities
    ArrayListMultimap<EntityBasedComponent, EntityMention> component2Entities = getContainedEntityMentionOrCreateNewForAll(
            aJCas, JCasUtil.select(aJCas, EntityBasedComponent.class), word2EntityMentions,
            componentId);

    for (EntityBasedComponent comp : component2Entities.keySet()) {
      List<EntityMention> entityMentions = component2Entities.get(comp);
      comp.setContainingEntityMentions(FSCollectionFactory.createFSList(aJCas, entityMentions));
    }
  }

  /**
   * Find the entity mentions associated with annotation. A side effect is that if the entity is not
   * found, the annotation itself will be annotated as a EntityMention
   * 
   * @param annos
   *          A list of annotations
   * @param word2EntityMentions
   *          A map from word to co-referenced resolve entities list
   * @return a map from the annotations to the entities list
   */
  public static <K extends ComponentAnnotation> ArrayListMultimap<K, EntityMention> getContainedEntityMentionOrCreateNewForAll(
          JCas aJCas, Collection<K> annos, HashMultimap<Word, EntityMention> word2EntityMentions,
          String componentId) {
    ArrayListMultimap<K, EntityMention> resultMap = ArrayListMultimap.create();
    for (K anno : annos) {
      resultMap.putAll(anno,
              getContainedEntityMentionOrCreateNew(aJCas, anno, word2EntityMentions, componentId));
    }
    return resultMap;
  }

  public static <K extends ComponentAnnotation> List<EntityMention> getContainedEntityMentionOrCreateNew(
          JCas aJCas, K anno, HashMultimap<Word, EntityMention> word2EntityMentions,
          String componentId) {
    List<Word> annoWords = JCasUtil.selectCovered(Word.class, anno);
    List<EntityMention> entityCandidates = new ArrayList<EntityMention>();
    // so that the entity mention returned are sorted by their appearance sequence
    Set<EntityMention> nonOverlappedMentions = new LinkedHashSet<EntityMention>();

    for (Word annoWord : annoWords) {
      if (word2EntityMentions.containsKey(annoWord)) {
        nonOverlappedMentions.addAll(word2EntityMentions.get(annoWord));
      }
    }
    entityCandidates.addAll(nonOverlappedMentions);

    // create if not exists
    if (entityCandidates.size() == 0) {
      EntityMention em = new EntityMention(aJCas, anno.getBegin(), anno.getEnd());
      em.addToIndexes();
      em.setComponentId(componentId);
      entityCandidates.add(em);

      for (Word emWord : JCasUtil.selectCovered(Word.class, em)) {
        word2EntityMentions.put(emWord, em);
      }
    }

    return entityCandidates;
  }

  public static <K extends ComponentAnnotation> List<EntityMention> getEntityMentionOrCreateNew(
          JCas aJCas, K anno, HashMultimap<Word, EntityMention> word2EntityMentions,
          String componentId) {
    List<Word> annoWords = JCasUtil.selectCovered(Word.class, anno);
    List<EntityMention> entityCandidates = new ArrayList<EntityMention>();
    Set<EntityMention> nonOverlappedMentions = new HashSet<EntityMention>();

    for (Word annoWord : annoWords) {
      if (word2EntityMentions.containsKey(annoWord)) {
        nonOverlappedMentions.addAll(word2EntityMentions.get(annoWord));
      }
    }
    entityCandidates.addAll(nonOverlappedMentions);

    // create if not exists
    if (entityCandidates.size() == 0) {
      EntityMention em = new EntityMention(aJCas, anno.getBegin(), anno.getEnd());
      em.addToIndexes();
      em.setComponentId(componentId);
      entityCandidates.add(em);

      for (Word emWord : JCasUtil.selectCovered(Word.class, em)) {
        word2EntityMentions.put(emWord, em);
      }
    }

    return entityCandidates;
  }

  public static void copyAgents(JCas aJCas, EventMention eventCopyFrom, EventMention eventCopyTo,
          String componentId) {
    FSList eventCopyFromAgentsFSList = eventCopyFrom.getAgentLinks();
    // attach links to event side
    eventCopyTo.setAgentLinks(copyComponentLinks(aJCas, eventCopyFromAgentsFSList, eventCopyTo,
            componentId));
  }

  public static void copyPatients(JCas aJCas, EventMention eventCopyFrom, EventMention eventCopyTo,
          String componentId) {
    FSList eventCopyFromPatientsFSList = eventCopyFrom.getPatientLinks();
    eventCopyTo.setPatientLinks(copyComponentLinks(aJCas, eventCopyFromPatientsFSList, eventCopyTo,
            componentId));
  }

  public static void copyLocations(JCas aJCas, EventMention eventCopyFrom,
          EventMention eventCopyTo, String componentId) {
    FSList eventCopyFromLocationsFSList = eventCopyFrom.getLocationLinks();
    eventCopyTo.setLocationLinks(copyComponentLinks(aJCas, eventCopyFromLocationsFSList,
            eventCopyTo, componentId));
  }

  public static FSList copyComponentLinks(JCas aJCas, FSList event1CopyFromLinksFSList,
          EventMention eventCopyTo, String componentId) {
    if (event1CopyFromLinksFSList != null) {
      Collection<EntityBasedComponentLink> eventCopyFromLinks = FSCollectionFactory.create(
              event1CopyFromLinksFSList, EntityBasedComponentLink.class);
      List<EntityBasedComponentLink> eventCopyToLinks = new LinkedList<EntityBasedComponentLink>();
      for (EntityBasedComponentLink link : eventCopyFromLinks) {
        EntityBasedComponent aComponent = link.getComponent();
        String linkType = link.getLinkType();
        EntityBasedComponentLink copyToLink = APLUtils.createLink(aJCas, eventCopyTo, aComponent,
                linkType, componentId);
        eventCopyToLinks.add(copyToLink);
      }
      return FSCollectionFactory.createFSList(aJCas, eventCopyToLinks);
    }
    return null;
  }
}
