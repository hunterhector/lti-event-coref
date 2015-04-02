package edu.cmu.lti.event_coref.utils;

import com.google.common.collect.ArrayListMultimap;
import edu.cmu.lti.utils.general.FileUtils;

import java.util.*;
import java.util.Map.Entry;

public class WorldGazetteer {
  // private HashMap<String,ArrayList<String>> aliasMap; // map from alias (including original) to
  // id.
  ArrayListMultimap<String, String> aliasMap;

  private HashMap<String, WorldGazetteerRecord> idMap; // map id to the record

  public WorldGazetteer(String filePath) {
    // initialize
    parseWorldGazetteerFile(filePath);
  }

  // TODO: need to be able to find similar names, naive records only consider direct match
  public List<WorldGazetteerRecord> getNaiveRecord(String name) {
    // LogUtils.log(String.format("Looking for record for name: %s ", name));
    name = name.toLowerCase();
    List<String> possibleIds = aliasMap.get(name);
    if (possibleIds.isEmpty()) {
      for (String partialName : name.split(" ")) {
        List<String> partialNameMatches = aliasMap.get(partialName);
        possibleIds.addAll(partialNameMatches);
      }
    }

    List<WorldGazetteerRecord> possibleRecords = new ArrayList<WorldGazetteerRecord>();
    for (String id : possibleIds) {
      WorldGazetteerRecord record = idMap.get(id);
      possibleRecords.add(record);
      // LogUtils.log(String.format("Found record: %s", record));
    }
    return possibleRecords;
  }

  // test the class
  public static void main(String[] args) {
    String pathName = "../edu.cmu.lti.event_coref.system/resources/the_world_gazetteer/dataen.txt";
    WorldGazetteer g = new WorldGazetteer(pathName);
    HashMap<String, WorldGazetteerRecord> gm = g.idMap;
    for (Entry<String, WorldGazetteerRecord> entry : gm.entrySet()) {
      String name = entry.getKey();
      WorldGazetteerRecord r = entry.getValue();
    }
  }

  private void parseWorldGazetteerFile(String gazetteerPath) {
    idMap = new HashMap<String, WorldGazetteerRecord>();
    aliasMap = ArrayListMultimap.create();

    String content = FileUtils.readFileWithEncoding(gazetteerPath, "UTF-8");
    String[] records = content.split("(?=((^^|\\n)(-\\d+|\\d+)))");

    for (String record : records) {
      String[] fields = record.split("\t", -1);
      if (fields.length == 12) {
        WorldGazetteerRecord wgr = new WorldGazetteerRecord(fields);
        String id = wgr.getId();
        idMap.put(id, wgr);
        Set<String> uniqueAliases = new HashSet<String>(wgr.getAlternativeNames());
        uniqueAliases.addAll(wgr.getOriginNames());
        uniqueAliases.add(wgr.getName());

        for (String alias : uniqueAliases) {
          aliasMap.put(alias.toLowerCase(), id);
        }
      }
    }
    
//  old debug message
//    List<String> aliasId = aliasMap.get("gaza");
//    for (String id:aliasId){
//      System.out.println(idMap.get(id));
//    }
  }
}
