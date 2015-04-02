package edu.cmu.lti.event_coref.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class contains 2 SudokuRow
 * The order of two row won't change the comparison result
 * @author Zhengzhong Liu, Hector
 *
 */
public class EventMentionRowPair{
  private final EventMentionRow row1;
  private final EventMentionRow row2;

  private static final Logger logger = LoggerFactory.getLogger(EventMentionRowPair.class);

  public EventMentionRowPair(EventMentionRow row1, EventMentionRow row2){
    this.row1 = row1;
    this.row2 = row2;
  }
  
  public EventMentionRow getRow1(){
    return row1;
  }
  
  public EventMentionRow getRow2(){
    return row2;
  }
  
  @Override
  public int hashCode()
  {
    return row1.hashCode()+row2.hashCode();
  }
  
  public boolean equals( Object obj )
  {
    EventMentionRowPair row = ( EventMentionRowPair )obj;
    if( row1.equals(row.getRow1()) && row2.equals(row.getRow2()))
      return true;
    if( row2.equals(row.getRow1()) && row2.equals(row.getRow1()))
      return true;
    return true;
  }

}
