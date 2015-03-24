package edu.cmu.lti.util.uima;

import org.uimafit.component.JCasAnnotator_ImplBase;

import edu.cmu.lti.util.type.ComponentAnnotation;
import edu.cmu.lti.util.type.ComponentTOP;

/**
 * An implementation of an abstract analysis engine. This analysis engine assumes you to use uimaFIT
 * in your analysis engine inherited from it.
 * 
 * @author Jun Araki
 */
public abstract class BaseAnalysisEngine extends JCasAnnotator_ImplBase {

  public void setGoldStandardComponentId(ComponentAnnotation ann) {
    ann.setComponentId(UimaConstants.ComponentId.GOLD_STANDARD.toString());
  }

  public void setGoldStandardComponentId(ComponentTOP ann) {
    ann.setComponentId(UimaConstants.ComponentId.GOLD_STANDARD.toString());
  }

  public void setSystemComponentId(ComponentAnnotation ann) {
    ann.setComponentId(UimaConstants.ComponentId.SYSTEM.toString());
  }

  public void setSystemComponentId(ComponentTOP ann) {
    ann.setComponentId(UimaConstants.ComponentId.SYSTEM.toString());
  }

  public boolean isGoldStandardAnnotation(ComponentAnnotation ann) {
    String componentId = ann.getComponentId();
    if (UimaConstants.ComponentId.GOLD_STANDARD.toString().equals(componentId)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isGoldStandardAnnotation(ComponentTOP ann) {
    String componentId = ann.getComponentId();
    if (UimaConstants.ComponentId.GOLD_STANDARD.toString().equals(componentId)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isSystemAnnotation(ComponentAnnotation ann) {
    String componentId = ann.getComponentId();
    if (UimaConstants.ComponentId.SYSTEM.toString().equals(componentId)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isSystemAnnotation(ComponentTOP ann) {
    String componentId = ann.getComponentId();
    if (UimaConstants.ComponentId.SYSTEM.toString().equals(componentId)) {
      return true;
    } else {
      return false;
    }
  }

}
