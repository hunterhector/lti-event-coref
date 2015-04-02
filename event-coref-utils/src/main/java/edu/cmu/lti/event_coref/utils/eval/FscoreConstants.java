package edu.cmu.lti.event_coref.utils.eval;

import java.util.ArrayList;
import java.util.List;

public class FscoreConstants {

  public enum ScoreLabel {
    PRECISION  ("Precision"),
    RECALL     ("Recall"),
    F1         ("F1"),
    FSCORE     ("F-score");

    private final String name;

    ScoreLabel(String name) {
      this.name = name;
    }

    public String toString() {
      return name;
    }

    public static List<String> getLabelListWithF1() {
      List<String> labelList = new ArrayList<String>();
      labelList.add(PRECISION.toString());
      labelList.add(RECALL.toString());
      labelList.add(F1.toString());

      return labelList;
    }
  }

}
