package edu.cmu.lti.util.general;

import java.util.*;

/**
 * A utility class for handling lists.
 * 
 * @author Jun Araki
 */
public class ListUtils {

  /**
   * Tests whether the specified list is null or empty.
   * 
   * @param <T>
   * @param list
   * @return True if the specified list is null or empty; false otherwise
   */
  public static <T> boolean isNullOrEmptyList(List<T> list) {
    return CollectionUtils.isNullOrEmptyCollection(list);
  }

  /**
   * Initializes a list of the specified size with the specified value.
   * 
   * @param initValue
   * @param size
   * @return a list of the specified size with the specified value
   */
  public static <T> List<T> initializeList(T initValue, int size) {
    List<T> list = new ArrayList<T>();
    for (int i = 0; i < size; i++) {
      list.add(initValue);
    }

    return list;
  }

  /**
   * Returns a list of string values in the specified list in one column.
   * 
   * @param <T>
   * @param values
   * @return a list of string values in the specified list in one column
   */
  public static <T> String getListStringInOneColumn(List<T> values) {
    StringBuilder buf = new StringBuilder();
    for (T value : values) {
      buf.append(value.toString());
      buf.append(System.lineSeparator().toString());
    }

    return buf.toString();
  }

  /**
   * Returns a merged list of the specified lists.
   * 
   * @param <T>
   * @param lists
   * @return a merged list of the specified lists
   */
  public static <T> List<T> mergeLists(List<T>... lists) {
    Set<T> set = new HashSet<T>();
    for (List<T> list : lists) {
      set.addAll(list);
    }

    return new ArrayList<T>(set);
  }

  /**
   * Returns a merged list of the specified set of lists.
   * 
   * @param <T>
   * @param lists
   * @return
   */
  public static <T> List<T> mergeLists(Set<List<T>> lists) {
    Set<T> set = new HashSet<T>();
    for (List<T> list : lists) {
      set.addAll(list);
    }

    return new ArrayList<T>(set);
  }

  /**
   * Returns a sorted list merged from the specified sorted lists.
   * 
   * @param <T>
   * @param sortedLists
   * @param allowDuplicates
   * @return a sorted list merged from the specified sorted lists
   */
  public static <T extends Comparable<? super T>> List<T> mergeSortedLists(
          Set<List<T>> sortedLists, boolean allowDuplicates) {
    int totalSize = 0;
    for (List<T> list : sortedLists) {
      totalSize += list.size();
    }

    List<T> mergedList = new ArrayList<T>(totalSize);
    List<T> lowest;
    Map<T, Boolean> valueMap = new HashMap<T, Boolean>();
    while (mergedList.size() < totalSize) {
      // Find the lowest value
      lowest = null;
      for (List<T> list : sortedLists) {
        if (!ListUtils.isNullOrEmptyList(list)) {
          if (lowest == null) {
            lowest = list;
          } else if (list.get(0).compareTo(lowest.get(0)) <= 0) {
            lowest = list;
          }
        }
      }

      T lowestValue = lowest.get(0);
      if (allowDuplicates) {
        // In the case where we allow any duplicates, add the lowest immediately.
        mergedList.add(lowestValue);
      } else {
        // In the case where we do not allow any duplicates
        if (!valueMap.containsKey(lowestValue)) {
          mergedList.add(lowestValue);
          valueMap.put(lowestValue, true);
        }
      }
      lowest.remove(0);
    }

    return mergedList;
  }

  /**
   * Returns a list from the specified values.
   * 
   * @param <T>
   * @param values
   * @return a list created from the specified values
   */
  public static <T> List<T> createList(T... values) {
    List<T> list = new ArrayList<T>();
    for (T value : values) {
      list.add(value);
    }

    return list;
  }

  public static List<Integer> convertStringListToIntegerList(List<String> sList) {
    List<Integer> iList = new ArrayList<Integer>();
    for (String s : sList) {
      iList.add(StringUtils.convertStringToInteger(s));
    }

    return iList;
  }

  public static List<String> convertIntegerListToStringList(List<Integer> iList) {
    List<String> sList = new ArrayList<String>();
    for (Integer i : iList) {
      sList.add(StringUtils.convertIntegerToString(i));
    }

    return sList;
  }

  public static List<Double> convertStringListToDoubleList(List<String> sList) {
    List<Double> dList = new ArrayList<Double>();
    for (String s : sList) {
      dList.add(StringUtils.convertStringToDouble(s));
    }

    return dList;
  }

  public static List<String> convertDoubleListToStringList(List<Double> dList) {
    List<String> sList = new ArrayList<String>();
    for (Double d : dList) {
      sList.add(StringUtils.convertDoubleToString(d));
    }

    return sList;
  }

}
