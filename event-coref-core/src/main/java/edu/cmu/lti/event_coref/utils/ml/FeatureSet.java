package edu.cmu.lti.event_coref.utils.ml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This class stores a set of all features. The set representation guarantees that each feature is
 * unique within the set.
 * 
 * @author Jun Araki
 */
public class FeatureSet {

  private Set<Feature> featureSet;

  private String name;

  /**
   * Public constructor.
   */
  public FeatureSet() {
    /** This set preserves its order to convert it to a list */
    featureSet = new LinkedHashSet<Feature>();
  }

  public FeatureSet(String name) {
    this();
    this.name = name;
  }

  public void addFeature(Feature feature) {
    if (!featureSet.contains(feature)) {
      featureSet.add(feature);
    }
  }

  public List<Feature> getSortedFeatureList() {
    return getSortedFeatureList(false);
  }

  /**
   * The first argument specifies whether you want to reassign feature IDs to features.
   * 
   * @param reassignFeatureId
   * @return
   */
  public List<Feature> getSortedFeatureList(boolean reassignFeatureId) {
    List<Feature> featureList = new ArrayList<Feature>(featureSet);
    Collections.sort(featureList);
    if (reassignFeatureId) {
      int featureId = 1;
      for (Feature f : featureList) {
        f.setFeatureId(featureId);
        featureId++;
      }
    }
    return featureList;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<Feature> getFeatureSet() {
    return featureSet;
  }

  public int size() {
    return featureSet.size();
  }

}
