package edu.cmu.lti.utils.uima;

public class UimaConstants {

  public enum ComponentId {
    GOLD_STANDARD ("GoldStandard"),
    SYSTEM        ("System");

    private String value;

    ComponentId(String value) {
      this.value = value;
    }

    public String toString() {
      return value;
    }
  }

}
