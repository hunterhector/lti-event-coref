package edu.cmu.lti.utils.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A utility class for handling maps.
 * 
 * @author Jun Araki
 */
public class MapUtils {

  /**
   * Returns a sorted list of keys of the specified map.
   * 
   * @param <T>
   *          type of key
   * @param map
   * @return a sorted list of keys of the specified map
   */
  public static <T extends Comparable<? super T>> List<T> getSortedKeys(Map<T, ?> map) {
    List<T> sortedKeys = new ArrayList<T>(map.keySet());
    Collections.sort(sortedKeys);
    return sortedKeys;
  }

  /**
   * Returns a sorted list of values of the specified map.
   * 
   * @param <T>
   *          type of value
   * @param map
   * @return a sorted list of values of the specified map
   */
  public static <T extends Comparable<? super T>> List<T> getSortedValues(Map<?, T> map) {
    List<T> sortedValues = new ArrayList<T>(map.values());
    Collections.sort(sortedValues);
    return sortedValues;
  }

  /**
   * Returns a sorted list of distinct values of the specified map.
   * 
   * @param <T>
   *          type of value
   * @param map
   * @return a sorted list of distinct values of the specified map
   */
  public static <T extends Comparable<? super T>> List<T> getDistinctSortedValues(Map<?, T> map) {
    List<T> distinctSortedValues = new ArrayList<T>(new HashSet<T>(map.values()));
    Collections.sort(distinctSortedValues);
    return distinctSortedValues;
  }

  /**
   * Returns a map with sorted keys in the ascending order, given the specified map.
   * 
   * @param <K>
   *          type of key
   * @param <V>
   *          type of value
   * @param map
   * @return a map with sorted keys in the ascending order
   */
  public static <K extends Comparable<? super K>, V> Map<K, V> getSortedMapByKeyInAscendingOrder(
          Map<K, V> map) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
        return (o1.getKey()).compareTo(o2.getKey());
      }
    });

    Map<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  /**
   * Returns a map with sorted keys in the descending order, given the specified map.
   * 
   * @param <K>
   *          type of key
   * @param <V>
   *          type of value
   * @param map
   * @return a map with sorted keys in the descending order
   */
  public static <K extends Comparable<? super K>, V> Map<K, V> getSortedMapByKeyInDescendingOrder(
          Map<K, V> map) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
        return (o2.getKey()).compareTo(o1.getKey());
      }
    });

    Map<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  /**
   * Returns a map with sorted values in the ascending order, given the specified map.
   * 
   * @param <K>
   *          type of key
   * @param <V>
   *          type of value
   * @param map
   * @return a map with sorted values in the ascending order
   */
  public static <K, V extends Comparable<? super V>> Map<K, V> getSortedMapByValueInAscendingOrder(
          Map<K, V> map) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
        return (o1.getValue()).compareTo(o2.getValue());
      }
    });

    Map<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  /**
   * Returns a map with sorted values in the descending order, given the specified map.
   * 
   * @param <K>
   *          type of key
   * @param <V>
   *          type of value
   * @param map
   * @return a map with sorted values in the descending order
   */
  public static <K, V extends Comparable<? super V>> Map<K, V> getSortedMapByValueInDescendingOrder(
          Map<K, V> map) {
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
        return (o2.getValue()).compareTo(o1.getValue());
      }
    });

    Map<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  /**
   * Increments a count value for the specified key, given a counter created for the purpose of
   * counting something.
   * 
   * @param counterMap
   */
  public static <K> void incrementCounter(Map<K, Integer> counterMap, K key) {
    if (counterMap.containsKey(key)) {
      counterMap.put(key, counterMap.get(key) + 1);
    } else {
      counterMap.put(key, 1);
    }
  }

  /**
   * Returns the total count of the specified counter.
   * 
   * @param counterMap
   * @return the total count of the specified counter
   */
  public static <K> int getTotalCount(Map<K, Integer> counterMap) {
    int totalCount = 0;
    for (K key : counterMap.keySet()) {
      totalCount += counterMap.get(key);
    }

    return totalCount;
  }

}
