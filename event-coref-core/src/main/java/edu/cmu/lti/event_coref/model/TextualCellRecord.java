package edu.cmu.lti.event_coref.model;


import edu.cmu.lti.utils.type.ComponentAnnotation;

public class TextualCellRecord {
  public enum CELLTYPE {
    AGENT, PATIENT, LOCATION
  };

  private CELLTYPE cellType;
  private ComponentAnnotation annotation;
  
  public TextualCellRecord(ComponentAnnotation annotation, CELLTYPE cellType) {
    this.cellType = cellType;
    this.annotation = annotation;
  }

  public CELLTYPE getCellType() {
    return cellType;
  }

  public String getSurfaceForm() {
    return annotation.getCoveredText();
  }
  
  public ComponentAnnotation getAnnotation(){
    return annotation;
  }

  public void setCellType(CELLTYPE cellType) {
    this.cellType = cellType;
  }

  public void setAnnotation(ComponentAnnotation annotation) {
    this.annotation = annotation;
  }
}
