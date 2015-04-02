package edu.cmu.lti.event_coref.model;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.event_coref.model.TemporalConstants.TemporalConjunction;
import edu.cmu.lti.event_coref.type.TemporalVariable;

/**
 * A class to construct a temporal relation.
 * 
 * @author Jun Araki
 */
public class TemporalRelation {

  TemporalVariable TemporalVariableInLeftHandSide;

  TemporalVariable TemporalVariableInRightHandSide;

  TemporalConjunction temporalConjunction;

  String temporalDifference;

  private final static String DEFAULT_TEMPORAL_DIFFERENCE = "delta";

  /**
   * Constructor.
   * 
   * @param TemporalVariableInLeftHandSide
   * @param TemporalVariableInRightHandSide
   * @param temporalConjunction
   */
  public TemporalRelation(TemporalVariable TemporalVariableInLeftHandSide,
          TemporalVariable TemporalVariableInRightHandSide, TemporalConjunction temporalConjunction) {
    this.TemporalVariableInLeftHandSide = TemporalVariableInLeftHandSide;
    this.TemporalVariableInRightHandSide = TemporalVariableInRightHandSide;
    this.temporalConjunction = temporalConjunction;
    temporalDifference = DEFAULT_TEMPORAL_DIFFERENCE;
  }

  public void setTemporalDifference(String temporalDifference) {
    this.temporalDifference = temporalDifference;
  }

  public List<String> getTemporalRelations() {
    List<String> temporalRelations = new ArrayList<String>();
    StringBuilder buf = new StringBuilder();
    switch (temporalConjunction) {
      case BEFORE:
        buf.append(TemporalVariableInLeftHandSide);
        buf.append(" = ");
        buf.append(TemporalVariableInRightHandSide);
        buf.append(" - ");
        buf.append(temporalDifference);
        temporalRelations.add(buf.toString());
        return temporalRelations;

      case AFTER:
        buf.append(TemporalVariableInLeftHandSide);
        buf.append(" = ");
        buf.append(TemporalVariableInRightHandSide);
        buf.append(" + ");
        buf.append(temporalDifference);
        temporalRelations.add(buf.toString());
        return temporalRelations;

      case DURING:
        buf.append(TemporalVariableInLeftHandSide);
        buf.append(" >= ");
        buf.append("begin(");
        buf.append(TemporalVariableInRightHandSide);
        buf.append(")");
        temporalRelations.add(buf.toString());

        buf = new StringBuilder();
        buf.append(TemporalVariableInLeftHandSide);
        buf.append(" <= ");
        buf.append("end(");
        buf.append(TemporalVariableInRightHandSide);
        buf.append(")");
        temporalRelations.add(buf.toString());
        return temporalRelations;

      default:
        return null;
    }
  }

}
