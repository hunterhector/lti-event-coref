package edu.cmu.lti.event_coref.model;

public class TemporalConstants {

  public enum TemporalConjunction {
    BEFORE   ("before"),
    AFTER    ("after"),
    DURING   ("during"),
    WHILE    ("while"),
    FOLLOWING ("following");

    private final String value;

    // Constructor.
    TemporalConjunction(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

}
