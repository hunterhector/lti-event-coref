package edu.cmu.lti.event_coref.utils.eval;

import edu.cmu.lti.event_coref.model.EventCorefConstants;
import edu.cmu.lti.event_coref.type.PairwiseEventCoreferenceEvaluation;

public class CorefChecker {

  public static String getCorefTypeGolden(PairwiseEventCoreferenceEvaluation pece) {
    String golden = pece.getEventCoreferenceRelationGoldStandard();
    if (golden == null) {
      return EventCorefConstants.NO_COREFERENCE_TYPE;
    }
    return golden;
  }

  public static String getCorefTypeSystem(PairwiseEventCoreferenceEvaluation pece) {
    String system = pece.getEventCoreferenceRelationSystem();
    if (system == null) {
      return EventCorefConstants.NO_COREFERENCE_TYPE;
    }
    return system;
  }

  public static boolean isMembership(String corefType) {
    return corefType.equals(EventCorefConstants.MEMBER_COREFRENCE_TYPE);
  }

  public static boolean isSubevent(String corefType) {
    return corefType.equals(EventCorefConstants.SUB_COREFERENCE_TYPE);
  }

  public static boolean isFull(String corefType) {
    return corefType.equals(EventCorefConstants.FULL_COREFERENCE_TYPE_IN_PECE);
  }

  public static boolean isMembershipGolden(PairwiseEventCoreferenceEvaluation pece) {
    String golden = getCorefTypeGolden(pece);
    return isMembership(golden);
  }

  public static boolean isSubGolden(PairwiseEventCoreferenceEvaluation pece) {
    String golden = getCorefTypeGolden(pece);
    return isSubevent(golden);
  }

  public static boolean isFullGolden(PairwiseEventCoreferenceEvaluation pece) {
    String golden = getCorefTypeGolden(pece);
    return isFull(golden);
  }

  public static boolean isAnyCorefGolden(String goldenType) {
    return isMembership(goldenType) || isSubevent(goldenType) || isFull(goldenType);
  }

  public static boolean isAnyCorefGolden(PairwiseEventCoreferenceEvaluation pece) {
    return isMembershipGolden(pece) || isSubGolden(pece) || isFullGolden(pece);
  }

  public static boolean noCoreferenceGolden(PairwiseEventCoreferenceEvaluation pece) {
    return !isAnyCorefGolden(pece);
  }

  public static boolean isMembershipSystem(PairwiseEventCoreferenceEvaluation pece) {
    String system = getCorefTypeSystem(pece);
    return isMembership(system);
  }

  public static boolean isSubeventSystem(PairwiseEventCoreferenceEvaluation pece) {
    String system = getCorefTypeSystem(pece);
    return isSubevent(system);
  }

  public static boolean isFullSystem(PairwiseEventCoreferenceEvaluation pece) {
    String system = getCorefTypeSystem(pece);
    return isFull(system);
  }

  public static boolean isAnyCorefSystem(PairwiseEventCoreferenceEvaluation pece) {
    return isMembershipSystem(pece) || isSubeventSystem(pece) || isFullSystem(pece);
  }

  public static boolean noCoreferenceSystem(PairwiseEventCoreferenceEvaluation pece) {
    return !isAnyCorefSystem(pece);
  }

  public static boolean coreferenceSystemNotSet(PairwiseEventCoreferenceEvaluation pece) {
    return pece.getEventCoreferenceRelationSystem() == null;
  }
}
