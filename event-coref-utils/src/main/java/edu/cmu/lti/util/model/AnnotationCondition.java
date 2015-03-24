package edu.cmu.lti.util.model;

import org.apache.uima.jcas.cas.TOP;

public interface AnnotationCondition{
  public Boolean check(TOP aAnnotation);
}
