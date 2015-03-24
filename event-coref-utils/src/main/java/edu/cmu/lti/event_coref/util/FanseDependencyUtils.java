package edu.cmu.lti.event_coref.util;

import edu.cmu.lti.event_coref.type.FanseDependencyRelation;
import edu.cmu.lti.event_coref.type.FanseToken;
import edu.cmu.lti.event_coref.type.Word;
import edu.cmu.lti.util.type.ComponentAnnotation;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.FSCollectionFactory;
import org.uimafit.util.JCasUtil;

import java.util.*;
import java.util.Map.Entry;

/**
 * Utility class that handle Fanse dependency parsing tree, to use this utility class Fanse
 * Annotation need to be done.
 * 
 * @author Zhengzhong Liu, Hector
 * 
 */
public class FanseDependencyUtils {

  public static String FANSE_ROOT_NODE = "ROOT";

  public static Word findHeadWordFromDependency(Annotation anno) {
    List<Word> words = JCasUtil.selectCovered(Word.class, anno);
    if (words.size() == 1) {
      return words.get(0);
    } else {
      int headPosition = getHeadWordPositionFromDependency(anno);

      if (headPosition == -1)
        return null;
      else
        // turn headNode into word
        return words.get(headPosition);
    }
  }

  public static FanseToken findHeadTokenFromDependency(Annotation anno) {
    List<FanseToken> tokens = JCasUtil.selectCovered(FanseToken.class, anno);
    if (tokens.size() == 1) {
      return tokens.get(0);
    } else {
      int headPosition = getHeadWordPositionFromDependency(anno);

      // turn headNode into word
      return tokens.get(headPosition);
    }
  }

  public static int getHeadWordPositionFromDependency(Annotation anno) {
    List<FanseToken> nodes = JCasUtil.selectCovered(FanseToken.class, anno);

    if (nodes.size() == 0)
      return -1;

    int headPosition = 0;
    FanseToken headNode = nodes.get(headPosition);
    for (int i = 0; i < nodes.size(); i++) {
      FanseToken node = nodes.get(i);
      FSList headsFS = headNode.getHeadDependencyRelations();
      if (headsFS != null) {
        Collection<FanseDependencyRelation> headRelations = FSCollectionFactory.create(headsFS,
                FanseDependencyRelation.class);
        for (FanseDependencyRelation headRelation : headRelations) {
          if (headRelation.getDependency().equals(FANSE_ROOT_NODE)) {
            headPosition = i;
            return headPosition;
          }

          if (headRelation.getHead().equals(node)) {
            headPosition = i;
            return headPosition;
          }
        }
      }
    }

    return headPosition;
  }

  public static Map<FanseToken, Integer> getHeadDependenciesWithDepth(ComponentAnnotation anno) {
    Word annoHead = findHeadWordFromDependency(anno);

    FanseToken headNode = JCasUtil.selectCovered(FanseToken.class, annoHead).get(0);

    return getHeadDependenciesWithDepth(headNode, 1);
  }

  /**
   * Find the lowest common ancestor for two annotations, if not found, will return null
   * 
   * @param anno1
   * @param anno2
   * @return
   */
  public static FanseToken findLowestCommonAncenstor(ComponentAnnotation anno1,
          ComponentAnnotation anno2) {
    Word annoHead1 = findHeadWordFromDependency(anno1);

    FanseToken headNode1 = JCasUtil.selectCovered(FanseToken.class, annoHead1).get(0);

    Word annoHead2 = findHeadWordFromDependency(anno2);

    FanseToken headNode2 = JCasUtil.selectCovered(FanseToken.class, annoHead2).get(0);

    Map<FanseToken, Integer> shorterAncenstors = getHeadDependenciesWithDepth(headNode1, 1);
    Map<FanseToken, Integer> longerAncenstors = getHeadDependenciesWithDepth(headNode2, 1);

    if (shorterAncenstors.size() > longerAncenstors.size()) {
      Map<FanseToken, Integer> tempAncenstors = longerAncenstors;
      longerAncenstors = shorterAncenstors;
      shorterAncenstors = tempAncenstors;
    }

    FanseToken lowestCommonAncenstor = null;
    Integer lowestHeight = null;

    for (Entry<FanseToken, Integer> ancestorEntry : shorterAncenstors.entrySet()) {
      FanseToken ancenstor = ancestorEntry.getKey();
      if (longerAncenstors.containsKey(ancenstor)) {
        int height = ancestorEntry.getValue();
        if (height < lowestHeight) {
          lowestHeight = height;
          lowestCommonAncenstor = ancenstor;
        }
      }
    }

    return lowestCommonAncenstor;
  }

  public static Map<FanseToken, Integer> getHeadDependenciesWithDepth(FanseToken token, int height) {
    Map<FanseToken, Integer> headTokens = new HashMap<FanseToken, Integer>();

    FSList headDependenciesFS = token.getHeadDependencyRelations();
    for (FanseDependencyRelation dependency : FSCollectionFactory.create(headDependenciesFS,
            FanseDependencyRelation.class)) {
      FanseToken parentNode = dependency.getHead();

      if (dependency.getDependency().equals(FANSE_ROOT_NODE)) {
        break;
      }

      headTokens.putAll(getHeadDependenciesWithDepth(parentNode, height + 1));

      headTokens.put(parentNode, height);
    }

    return headTokens;
  }

  public static int depthInDependencyTree(ComponentAnnotation anno) {
    Word annoHead = findHeadWordFromDependency(anno);

    FanseToken headNode = JCasUtil.selectCovered(FanseToken.class, annoHead).get(0);
    return getNodeDependencyDepth(headNode);
  }

  private static int getNodeDependencyDepth(FanseToken node) {
    FSList headDependenciesFS = node.getHeadDependencyRelations();
    int maxParentDepth = 0;
    for (FanseDependencyRelation dependency : FSCollectionFactory.create(headDependenciesFS,
            FanseDependencyRelation.class)) {
      FanseToken parentNode = dependency.getHead();

      if (dependency.getDependency().equals(FANSE_ROOT_NODE)) {
        return 0;
      }

      int parentDepth = getNodeDependencyDepth(parentNode);
      if (parentDepth > maxParentDepth) {
        maxParentDepth = parentDepth;
      }
    }
    return maxParentDepth + 1;
  }

  public static List<Word> getAllChildrenWords(Word word) {
    FanseToken fToken = JCasUtil.selectCovered(FanseToken.class, word).get(0);
    List<Word> allChildrenWords = new ArrayList<Word>();
    for (FanseToken childToken : getChildTokens(fToken)) {
      Word childWord = JCasUtil.selectCovered(Word.class, childToken).get(0);

      allChildrenWords.add(childWord);
    }

    return allChildrenWords;
  }

  private static List<FanseToken> getChildTokens(FanseToken fToken) {
    List<FanseToken> childTokens = new ArrayList<FanseToken>();
    FSList childRelationFS = fToken.getChildDependencyRelations();
    if (childRelationFS == null) {
      return childTokens;
    } else {
      for (FanseDependencyRelation dependency : FSCollectionFactory.create(childRelationFS,
              FanseDependencyRelation.class)) {
        FanseToken childToken = dependency.getChild();
        childTokens.add(childToken);
        childTokens.addAll(getChildTokens(childToken));
      }
    }
    return childTokens;
  }
}