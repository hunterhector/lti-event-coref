package edu.cmu.lti.event_coref.utils.eval;

public interface IFscore {

  public Double getFscore(double beta);

  public Double getF1();

  public Double getPrecision();

  public Double getRecall();

}
