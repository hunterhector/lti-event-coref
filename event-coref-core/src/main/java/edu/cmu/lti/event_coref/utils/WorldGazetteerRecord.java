package edu.cmu.lti.event_coref.utils;

import edu.cmu.lti.utils.general.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class WorldGazetteerRecord {

  private String id; // Unique identification number of the geographical

  // object
  private String name; // name of the geographical entity (english name, if

  // available)
  private ArrayList<String> alternativeNames; // alternative names

  private ArrayList<String> originNames; // original names in greek, cyrillic or arabic

  private String geograficalType; // type of geografical object (e.g. country,

  // locality etc)
  private long population; // current population

  private int latitude; // latitude (for places)

  private int longitude; // longitude (for places)

  private String parentCountry; // parent country

  private String division1; // parent administrative division of first level

  private String division2; // parent administrative division of second level

  private String division3; // parent administrative division of third level

  public WorldGazetteerRecord(String[] fields) {
    if (fields.length != 12) {
      throw new IllegalArgumentException(
              "Input fields should contain the exact 12 types defined by World Gazetteer.");
    }
    setId(fields[0]);
    setName(fields[1]);
    setAlternativeNames(fields[2]);
    setOriginNames(fields[3]);
    setGeograficalType(fields[4]);
    setPopulation(fields[5]);
    setLatitude(fields[6]);
    setLongitude(fields[7]);
    setParentCountry(fields[8]);
    setDivision1(fields[9]);
    setDivision2(fields[10]);
    setDivision3(fields[11]);
  }

  @Override
  public String toString() {
    return String
            .format("World Gazetteer Record %s - %s : [type]: [%s], [parent country]: [%s], [alternative names]: %s",
                    id, name, geograficalType, parentCountry, alternativeNames);
  }

  public void setId(String s) {
    if (!(s.equals(null) || s.equals(""))) {
      id = s.replaceAll("-", "");
      id = id.replaceAll("\n", "");
    } else
      id = "";
  }

  public void setName(String s) {
    if (!(s.equals(null) || s.equals("")))
      name = s.toLowerCase();
    else
      name = "";
  }

  public void setAlternativeNames(String s) {
    alternativeNames = new ArrayList<String>();

    if (!(s.equals(null) || s.equals(""))) {
      String[] strs = s.toLowerCase().split(",");
        for (String str : strs) {
          alternativeNames.add(str.trim());
        
      }
      // System.out.println("Alternative Names:"+alternativeName);
    } 
  }

  public void setOriginNames(String s) {
    if (!(s.equals(null) || s.equals(""))) {
      originNames = new ArrayList<String>(Arrays.asList(s.toLowerCase().split(",")));
      // System.out.println("Original Names:"+originNames);
    } else
      originNames = new ArrayList<String>();
  }

  public void setGeograficalType(String s) {
    if (!(s.equals(null) || s.equals("")))
      geograficalType = s;
    else
      geograficalType = "";
  }

  public void setPopulation(String s) {
    if (!(s.equals(null) || s.equals("")))
      population = StringUtils.convertStringToInteger(s);
    else
      population = -1;
  }

  public void setLatitude(String s) {
    if (!(s.equals(null) || s.equals("")))
      latitude = StringUtils.convertStringToInteger(s);
    else
      latitude = -1;
  }

  public void setLongitude(String s) {
    if (!(s.equals(null) || s.equals("")))
      longitude = StringUtils.convertStringToInteger(s);
    else
      longitude = -1;
  }

  public void setParentCountry(String s) {
    if (!(s.equals(null) || s.equals("")))
      parentCountry = s.toLowerCase();
    else
      parentCountry = "";
  }

  public void setDivision1(String s) {
    if (!(s.equals(null) || s.equals("")))
      division1 = s.toLowerCase();
    else
      division1 = "";
  }

  public void setDivision2(String s) {
    if (!(s.equals(null) || s.equals("")))
      division2 = s.toLowerCase();
    else
      division2 = "";
  }

  public void setDivision3(String s) {
    if (!(s.equals(null) || s.equals("")))
      division3 = s.toLowerCase();
    else
      division3 = "";
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ArrayList<String> getAlternativeNames() {
    return alternativeNames;
  }

  public ArrayList<String> getOriginNames() {
    return originNames;
  }

  public String getGeograficalType() {
    return geograficalType;
  }

  public long getPopulation() {
    return population;
  }

  public int getLatitude() {
    return latitude;// latitude (for places)
  }

  public int getLongitude() { // longitude (for places)
    return longitude;
  }

  public String getParentCountry() {
    return parentCountry;// parent country
  }

  public String getDivision1() {
    return division1;// parent administrative division of first level
  }

  public String getDivision2() {
    return division2; // parent administrative division of second level
  }

  public String getDivision3() {
    return division3;
  }

}
