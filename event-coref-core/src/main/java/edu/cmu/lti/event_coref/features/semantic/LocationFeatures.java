package edu.cmu.lti.event_coref.features.semantic;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.model.EventMentionRow;
import edu.cmu.lti.event_coref.model.EventMentionTable;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import edu.cmu.lti.event_coref.utils.SimilarityCalculator;
import edu.cmu.lti.utils.type.ComponentAnnotation;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringList;

import java.util.*;
import java.util.Map.Entry;

public class LocationFeatures extends PairwiseFeatureGenerator {

  Map<EventMention, EventMentionRow> domainTable;

  SimilarityCalculator calc;

  Set<String> lowConfidentAnnotatorNames;

  public LocationFeatures(EventMentionTable sTable, SimilarityCalculator calc,
          Set<String> lowConfidentAnnotatorNames) {
    domainTable = sTable.getDomainOnlyTableView();
    this.calc = calc;
    this.lowConfidentAnnotatorNames = lowConfidentAnnotatorNames;
  }

  @Override
  public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
          EventMention event2) {
    List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();
    EventMentionRow row1 = domainTable.get(event1);
    EventMentionRow row2 = domainTable.get(event2);

    features.addAll(createLocationFeatures(aJCas, row1, row2, calc, lowConfidentAnnotatorNames));
    return features;
  }

  public List<PairwiseEventFeature> createLocationFeatures(JCas aJCas, EventMentionRow row1,
          EventMentionRow row2, SimilarityCalculator rowFiller, Set<String> lowConfidentAnnotatorNames) {
    List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();
    // prepare to add location scores
    ArrayListMultimap<String, Location> locationsFromDifferentAnnotators1 = splitEntityBasedComponentsByAnnotator(
            row1.getLocationLinks(), Location.class);
    ArrayListMultimap<String, Location> locationsFromDifferentAnnotators2 = splitEntityBasedComponentsByAnnotator(
            row2.getLocationLinks(), Location.class);

    List<Location> allLocationAnnotations1 = row1.getLocations();
    List<Location> allLocationAnnotations2 = row2.getLocations();

    List<Location> confidentLocationAnnotations1 = new ArrayList<Location>();
    List<Location> confidentLocationAnnotations2 = new ArrayList<Location>();

    for (String annotatorName : locationsFromDifferentAnnotators1.keySet()) {
      // LogUtils.log("From annotator: " + annotatorName);
      if (!lowConfidentAnnotatorNames.contains(annotatorName)) {
        List<Location> theseLocations = locationsFromDifferentAnnotators1.get(annotatorName);
        confidentLocationAnnotations1.addAll(theseLocations);
      }
    }

    for (String annotatorName : locationsFromDifferentAnnotators2.keySet()) {
      // LogUtils.log("From annotator: " + annotatorName);
      if (!lowConfidentAnnotatorNames.contains(annotatorName)) {
        List<Location> theseLocations = locationsFromDifferentAnnotators2.get(annotatorName);
        confidentLocationAnnotations2.addAll(theseLocations);
      }
    }

    Map<String, Double> confidentLocationScores = checkLocationSimilarity(
            confidentLocationAnnotations1, confidentLocationAnnotations2);
    Map<String, Double> allLocationScores = checkLocationSimilarity(allLocationAnnotations1,
            allLocationAnnotations2);

    // // Add a few scores for confident locations
    // for (Entry<String, Double> scoreEntry : confidentLocationScores.entrySet()) {
    // String scoreName = scoreEntry.getKey();
    // Double score = scoreEntry.getValue();
    //
    // if (scoreName.equals("exactSurfaceMath")) {
    // PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventFeature(aJCas,
    // "confidentLocationExactSurfaceMatch", score);
    // features.add(aFeature);
    // }
    // if (scoreName.equals("maxEntitySim")) {
    // PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventFeature(aJCas,
    // "confidentLocationMaxEntitySimilarity", score);
    // features.add(aFeature);
    // }
    // if (scoreName.equals("dice")) {
    // PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventFeature(aJCas,
    // "confidentLocationRelaxedDice", score);
    // features.add(aFeature);
    // }
    // if (scoreName.equals("substring")) {
    // PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventFeature(aJCas,
    // "confidentLocationSubString", score);
    // features.add(aFeature);
    // }
    // if (scoreName.equals("maxAlternativeNameSimilarity")) {
    // PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventFeature(aJCas,
    // "confidentLocationMaxAlternativeNameSimilarity", score);
    // features.add(aFeature);
    //
    // }
    // }

    // Add a few scores for all locations
    for (Entry<String, Double> scoreEntry : allLocationScores.entrySet()) {
      String scoreName = scoreEntry.getKey();
      Double score = scoreEntry.getValue();

      if (scoreName.equals("exactSurfaceMath")) {
        String featureName = "allLocationExactSurfaceMatch";
        PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                featureName, score, false);
        features.add(aFeature);
      }
      if (scoreName.equals("maxEntityStringSim")) {
        String featureName = "allLocationMaxEntitySimilarity";
        PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                featureName, score, false);
        features.add(aFeature);
      }

      if (scoreName.equals("surfaceDice")) {
        String featureName = "allLocationDice";
        PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                featureName, score, false);
        features.add(aFeature);
      }
      if (scoreName.equals("surfaceRelaxedDice")) {
        String featureName = "allLocationRelaxedDice";
        PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                featureName, score, false);
        features.add(aFeature);
      }

      if (scoreName.equals("maxAlternativeNameSimilarity")) {
        String featureName = "allLocationMaxAlternativeNameSimilarity";
        PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                featureName, score, false);
        features.add(aFeature);
      }

      if (scoreName.equals("entityCoref")) {
        String featureName = "allLocationEntityCoref";
        PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                featureName, score == 1.0, false);
        features.add(aFeature);
      }
      if (scoreName.equals("substring")) {
        String featureName = "allLocationSubString";
        PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                featureName, score == 1.0, false);
        features.add(aFeature);
      }
      if (scoreName.equals("locationContainment")) {
        String featureName = "allLocationContainment";
        PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                featureName, score == 1.0, false);
        features.add(aFeature);
      }
    }
    return features;
  }

  private <T extends EntityBasedComponentLink, K extends EntityBasedComponent> ArrayListMultimap<String, K> splitEntityBasedComponentsByAnnotator(
          List<T> componentLinks, final Class<K> componentClass) {
    ArrayListMultimap<String, K> annotationByAnnotator = ArrayListMultimap.create();
    for (T anno : componentLinks) {
      String annotatorName = anno.getComponentId();
      annotationByAnnotator.put(annotatorName, (K) anno.getComponent());
    }

    return annotationByAnnotator;
  }

  private <T extends ComponentAnnotation> String annotations2Surface(List<T> annos) {
    Joiner spaceJoiner = Joiner.on(" ").skipNulls();

    List<String> stringList = Lists.transform(annos, new Function<T, String>() {
      public String apply(T anno) {
        return anno.getCoveredText();
      }
    });
    return spaceJoiner.join(stringList);
  }

  private Map<String, Double> checkLocationSimilarity(List<Location> locationList1,
          List<Location> locationList2) {
    Map<String, Double> scoreMap = new HashMap<String, Double>();
    for (Location location1 : locationList1) {
      for (Location location2 : locationList2) {
        String str1 = location1.getCoveredText();
        String str2 = location2.getCoveredText();

        if (str1.equals(str2))
          scoreMap.put("exactSurfaceMath", 1.0);
        else
          scoreMap.put("exactSurfaceMath", 0.0);

        double maxEntitySim = 0.0;
        boolean isCoref = false;
        for (EntityMention em1 : FSCollectionFactory.create(
                location1.getContainingEntityMentions(), EntityMention.class)) {
          for (EntityMention em2 : FSCollectionFactory.create(
                  location2.getContainingEntityMentions(), EntityMention.class)) {
            double entityScore = calc.getClosestInterClusterStringSimilarity(em1, em2);
            if (entityScore > maxEntitySim) {
              maxEntitySim = entityScore;
            }

            if (calc.checkEntityCorference(em1, em2)) {
              isCoref = true;
            }
          }
        }

        if (!scoreMap.containsKey("maxEntityStringSim")
                || scoreMap.get("maxEntityStringSim") < maxEntitySim) {
          scoreMap.put("maxEntityStringSim", maxEntitySim);
        }

        double dice = calc.relaxedDiceTest(str1, str2);
        if (!scoreMap.containsKey("dice") || scoreMap.get("dice") < dice) {
          scoreMap.put("dice", dice);
        }

        if (isCoref)
          scoreMap.put("entityCoref", 1.0);
        else
          scoreMap.put("entityCoref", 0.0);

        double sub = calc.subStringTest(str1, str2) ? 1.0 : 0.0;
        if (!scoreMap.containsKey("substring") || scoreMap.get("substring") < sub) {
          scoreMap.put("substring", sub);
        }

        if (countryOfTheOther(location1, location2))
          scoreMap.put("countryOfTheOther", 1.0);
        else
          scoreMap.put("countryOfTheOther", 0.0);

        Set<String> alterNames1 = locationRelatedNames(location1);
        Set<String> alterNames2 = locationRelatedNames(location2);

        double maxAlternativeNameSim = 0.0;
        for (String alterName1 : alterNames1) {
          for (String alterName2 : alterNames2) {
            double alterDice = calc.relaxedDiceTest(alterName1, alterName2);
            if (alterDice > maxAlternativeNameSim) {
              maxAlternativeNameSim = alterDice;
            }
          }
        }

        if (!scoreMap.containsKey("maxAlternativeNameSimilarity")
                || scoreMap.get("maxAlternativeNameSimilarity") < maxAlternativeNameSim) {
          scoreMap.put("maxAlternativeNameSimilarity", maxAlternativeNameSim);
        }
      }
    }

    return scoreMap;
  }

  public Set<String> locationRelatedNames(Location aLocation) {
    Set<String> relatedNames = new HashSet<String>();
    relatedNames.add(aLocation.getCoveredText());

    StringList alternativeStringList = aLocation.getNames();
    if (alternativeStringList != null) {
      relatedNames.addAll(FSCollectionFactory.create(alternativeStringList));
    }

    return relatedNames;
  }

  public boolean countryOfTheOther(Location location1, Location location2) {
    String country1 = location1.getCountry();
    String country2 = location2.getCountry();

    List<String> names1 = new ArrayList<String>();
    List<String> names2 = new ArrayList<String>();

    StringList loc1NamesFs = location1.getNames();
    StringList loc2NamesFs = location2.getNames();

    if (loc1NamesFs != null) {
      for (String name : FSCollectionFactory.create(loc1NamesFs)) {
        names1.add(name);
      }
    }

    if (loc2NamesFs != null) {
      for (String name : FSCollectionFactory.create(loc2NamesFs)) {
        names2.add(name);
      }
    }

    // check whether location1 is the country for location2
    if (country1 != null) {
      for (String name : names2) {
        if (calc.getDiceCoefficient(country1, name) > 0.8) {
          return true;
        }
      }
    }

    // check whether location2 is the country for location1
    if (country2 != null) {
      for (String name : names1) {
        if (calc.getDiceCoefficient(country2, name) > 0.8) {
          return true;
        }
      }
    }

    return false;
  }
}
