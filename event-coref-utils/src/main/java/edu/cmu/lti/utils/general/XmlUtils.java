package edu.cmu.lti.utils.general;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * A utility class for handling XML.
 * 
 * @author Jun Araki
 */
public class XmlUtils {

  private static final String TAG_BEGIN = "<";

  private static final String TAG_END = ">";

  /**
   * Returns the data structure org.w3c.dom.Document that we obtain by parsing the specified XML
   * text.
   * 
   * @param XmlText
   * @return
   */
  public static Document getDomDocument(String xmlText) {
    Document doc = null;

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = factory.newDocumentBuilder();
      InputSource inStream = new InputSource();
      inStream.setCharacterStream(new StringReader(xmlText));
      doc = db.parse(inStream);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return doc;
  }

  /**
   * Checks whether the specified XML text has one and only one tag of the specified tag name.
   * 
   * @param xmlText
   * @param tagName
   * @return True if the specified XML text has one and only one tag of the specified tag name;
   *         false otherwise.
   */
  public static Boolean hasSingleTag(String xmlText, String tagName) {
    Document doc = getDomDocument(xmlText);
    if (doc == null) {
      return false;
    }
    NodeList nodes = doc.getElementsByTagName(tagName);

    return hasUniqueNode(nodes);
  }

  /**
   * Returns the element specified with the given tag name in the specified XML text. Assumes that
   * there is only one tag of the speicified name in the XML text.
   * 
   * @param xmlText
   * @param tagName
   * @return
   */
  public static Element getSingleElement(String xmlText, String tagName) {
    if (!hasSingleTag(xmlText, tagName)) {
      return null;
    }

    Document doc = getDomDocument(xmlText);
    NodeList nodes = doc.getElementsByTagName(tagName);
    return (Element) nodes.item(0);
  }

  /**
   * Returns the string value specified with the given tag name in the specified XML text. Assumes
   * that there is only one tag of the speicified name in the XML text.
   * 
   * @param xmlText
   * @param tagName
   * @return the string value specified with the given tag name in the specified XML text
   */
  public static String getSingleTagValue(String xmlText, String tagName) {
    Element e = getSingleElement(xmlText, tagName);
    if (e == null) {
      return null;
    }

    return e.getFirstChild().getNodeValue();
  }

  /**
   * Returns the string value specified with the given tag name in the specified element. Assumes
   * that there is only one tag of the speicified name in the element.
   * 
   * @param element
   * @param tagName
   * @return the string value specified with the given tag name in the specified element
   */
  public static String getSingleTagValue(Element element, String tagName) {
    NodeList nodes = element.getElementsByTagName(tagName);
    if (!hasUniqueNode(nodes)) {
      return null;
    }

    Element e = (Element) nodes.item(0);
    if (e == null) {
      return null;
    }

    return e.getFirstChild().getNodeValue();
  }

  /**
   * Returns the index within the specified xmlText of the occurrence of the specified tag. If there
   * is not one and only one tag of the specified name, then -1 is returned.
   * 
   * @param xmlText
   * @param tagName
   * @return the index within the specified xmlText of the occurrence of the specified tag
   */
  public static int indexOfSingleTag(String xmlText, String tagName) {
    if (!hasSingleTag(xmlText, tagName)) {
      return -1;
    }

    String tag = TAG_BEGIN + tagName + TAG_END;
    return xmlText.indexOf(tag);
  }

  /**
   * Returns the index within the specified xmlText of the occurrence of the value of the specified
   * tag. If there is not one and only one tag of the specified name, then -1 is returned.
   * 
   * @param xmlText
   * @param tagName
   * @return the index within the specified xmlText of the occurrence of the value of the specified
   *         tag
   */
  public static int indexOfSingleTagValue(String xmlText, String tagName) {
    int index = indexOfSingleTag(xmlText, tagName);
    if (index < 0) {
      return -1;
    }

    String tag = TAG_BEGIN + tagName + TAG_END;
    return (index + tag.length());
  }

  /**
   * Returns the index within the specified xmlText of the occurrence of the specified value in the
   * specified tag. If there is not one and only one tag of the specified name, then -1 is returned.
   * 
   * @param xmlText
   * @param tagName
   * @param str
   * @return Returns the index within the specified xmlText of the occurrence of the specified value
   *         in the specified tag
   */
  public static int indexOfSingleTagValue(String xmlText, String tagName, String str) {
    int index = indexOfSingleTagValue(xmlText, tagName);
    if (index < 0) {
      return -1;
    }

    String value = getSingleTagValue(xmlText, tagName);
    if (value == null) {
      return -1;
    }

    int indexWithinValue = value.indexOf(str);
    if (indexWithinValue < 0) {
      return -1;
    }

    return (index + indexWithinValue);
  }

  /**
   * Returns true if the specified attribute has no string.
   * 
   * @param nodes
   * @return
   * @throws AnalysisEngineProcessException
   */
  public static boolean hasNoAttribute(String attr) {
    if (StringUtils.isNullOrEmptyString(attr)) {
      return true;
    }

    return false;
  }

  /**
   * Returns true if the specified node list has no nodes.
   * 
   * @param nodes
   * @return
   * @throws AnalysisEngineProcessException
   */
  public static boolean hasNoNodes(NodeList nodes) {
    if (nodes == null || nodes.getLength() == 0) {
      return true;
    }

    return false;
  }

  /**
   * Returns true if the specified node list has one and only one node.
   * 
   * @param articleNodes
   * @throws AnalysisEngineProcessException
   */
  public static boolean hasUniqueNode(NodeList nodes) {
    if (nodes == null) {
      return false;
    }

    if (nodes.getLength() == 1) {
      return true;
    }

    return false;
  }

}
