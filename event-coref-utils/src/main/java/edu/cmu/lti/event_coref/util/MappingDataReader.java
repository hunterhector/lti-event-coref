/**
 * 
 */
package edu.cmu.lti.event_coref.util;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.javatuples.Pair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zhengzhong Liu, Hector
 * 
 * 
 */
public class MappingDataReader {

  // The following methods wraps methods to read SemLink data from
  // http://verbs.colorado.edu/semlink/
  public static Map<Pair<String, String>, Pair<String, String>> getFN2VNMap(String vfMappingPath) {

    Map<Pair<String, String>, Pair<String, String>> fn2vn = new HashMap<Pair<String, String>, Pair<String, String>>();

    try {
      SAXBuilder builder = new SAXBuilder();
      builder.setDTDHandler(null);

      Document doc = builder.build(vfMappingPath);

      Element data = doc.getRootElement();

      List<Element> mappings = data.getChildren("vncls");

      for (Element mapping : mappings) {
        String frameName = mapping.getAttributeValue("fnframe");
        String vnClass = mapping.getAttributeValue("class");
        List<Element> roles = mapping.getChild("roles").getChildren("role");
        for (Element role : roles) {
          String fnRole = role.getAttributeValue("fnrole");
          String vnRole = role.getAttributeValue("vnrole");

          Pair<String, String> framePair = new Pair<String, String>(frameName, fnRole);
          Pair<String, String> vnPair = new Pair<String, String>(vnClass, vnRole);

          fn2vn.put(framePair, vnPair);
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (JDOMException e) {
      e.printStackTrace();
      System.exit(1);
    }

    System.out.println(String.format("%d mappings read from FrameNet to VerbNet", fn2vn.size()));

    return fn2vn;
  }

  public static Map<Pair<String, String>, Integer> getVN2PBMap(String vpMappingPath) {
    Map<Pair<String, String>, Integer> vn2pb = new HashMap<Pair<String, String>, Integer>();

    try {
      SAXBuilder builder = new SAXBuilder();
      builder.setDTDHandler(null);
      Document doc = builder.build(vpMappingPath);

      Element data = doc.getRootElement();

      List<Element> predicates = data.getChildren("predicate");

      for (Element predicate : predicates) {
        List<Element> mappings = predicate.getChildren("argmap");
        for (Element mapping : mappings) {
          String vnClass = mapping.getAttributeValue("vn-class");
          // String pbRoleset = mapping.getAttributeValue("pb-roleset");

          for (Element role : mapping.getChildren("roles")) {
            int pbArg = Integer.parseInt(role.getAttributeValue("pb-arg"));
            String vnRole = role.getAttributeValue("vn-theta");
            vn2pb.put(new Pair<String, String>(vnClass, vnRole), pbArg);
          }

        }
      }

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (JDOMException e) {
      e.printStackTrace();
      System.exit(1);
    }

    return vn2pb;
  }

  // The following methods wraps methods to read data from FrameNet relations
  public static Map<String, Table<String, String, Map<String, String>>> getFrameRelations(
          String fnRelatonPath) {
    Map<String, Table<String, String, Map<String, String>>> frameRelationMappings = new HashMap<String, Table<String, String, Map<String, String>>>();
    int typeCounter = 0;
    int frameRelationCounter = 0;
    int feRelationCounter = 0;

    try {
      SAXBuilder builder = new SAXBuilder();
      builder.setDTDHandler(null);

      Document doc = builder.build(fnRelatonPath);

      Element data = doc.getRootElement();

      Namespace ns = data.getNamespace();

      List<Element> relationTypeGroup = data.getChildren("frameRelationType", ns);

      for (Element relationsByType : relationTypeGroup) {
        typeCounter++;

        String relationType = relationsByType.getAttributeValue("name");

        Table<String, String, Map<String, String>> subFrame2SuperFrameMapping = HashBasedTable
                .create();

        List<Element> frameRelations = relationsByType.getChildren("frameRelation", ns);
        for (Element frameRelation : frameRelations) {
          frameRelationCounter++;

          List<Element> feRelations = frameRelation.getChildren("FERelation", ns);

          String subFrameName = frameRelation.getAttributeValue("subFrameName");
          String superFrameName = frameRelation.getAttributeValue("superFrameName");

          Map<String, String> feMapping = new HashMap<String, String>();

          for (Element feRelation : feRelations) {
            feRelationCounter++;

            String subFeName = feRelation.getAttributeValue("subFEName");
            String superFeName = feRelation.getAttributeValue("superFEName");
            feMapping.put(subFeName, superFeName);
          }

          subFrame2SuperFrameMapping.put(subFrameName, superFrameName, feMapping);
        }

        frameRelationMappings.put(relationType, subFrame2SuperFrameMapping);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (JDOMException e) {
      e.printStackTrace();
      System.exit(1);
    }

    System.out.println(String.format(
            "%d types, %s frame relations, %s frame element relations read", typeCounter,
            frameRelationCounter, feRelationCounter));

    return frameRelationMappings;
  }
}
