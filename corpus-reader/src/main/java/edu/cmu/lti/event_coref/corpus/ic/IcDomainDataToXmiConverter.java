package edu.cmu.lti.event_coref.corpus.ic;

import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.util.general.FileUtils;
import edu.cmu.lti.util.general.StringUtils;
import edu.cmu.lti.util.general.TimeUtils;
import edu.cmu.lti.util.general.XmlUtils;
import edu.cmu.lti.util.uima.BaseAnalysisEngine;
import edu.cmu.lti.util.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.FSCollectionFactory;
import org.uimafit.util.JCasUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This annotator annotates IC domain gold standard. Gold standard annotation are added to a
 * separated view instead of the default view.
 *
 * @author Jun Araki
 * @author Zhengzhong Liu, Hector
 */
public class IcDomainDataToXmiConverter extends BaseAnalysisEngine {

    public static final String PARAM_GOLD_STANDARD_INPUT_DIR = "GoldStandardInputDirectory";

    public static final String PARAM_GOLD_STANDARD_FILE_EXTENSION = "GoldStandardFileExtension";

    public static final String PARAM_GOLD_STANDARD_VIEW_NAME = "GoldStandardViewName";

    @ConfigurationParameter(name = PARAM_GOLD_STANDARD_INPUT_DIR)
    private String goldStandardInputDir;

    @ConfigurationParameter(name = PARAM_GOLD_STANDARD_FILE_EXTENSION)
    private String goldStandardFileExtension;

    @ConfigurationParameter(name = PARAM_GOLD_STANDARD_VIEW_NAME)
    private String goldStandardViewName;

    private Map<String, Element> wordIdToWord;

    private DocumentBuilder db;

    private static final Logger logger = LoggerFactory.getLogger(IcDomainDataToXmiConverter.class);

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        try {
            super.initialize(context);
        } catch (ResourceInitializationException e) {
            throw new ResourceInitializationException(e);
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            db = factory.newDocumentBuilder();
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        UimaConvenience.printProcessLog(aJCas);

        JCas goldStandardView = null;
        try {
            goldStandardView = aJCas.getView(goldStandardViewName);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        wordIdToWord = new HashMap<String, Element>();

        SourceDocumentInformation srcDocInfo = JCasUtil.selectSingle(aJCas,
                SourceDocumentInformation.class);
        String fileName = FileUtils.getName(srcDocInfo.getUri());

        String goldStandardFile = goldStandardInputDir + File.separator + fileName + goldStandardFileExtension;
        String goldStandardXml = FileUtils.readFile(goldStandardFile);

        Document doc = null;
        try {
            InputSource inStream = new InputSource();
            inStream.setCharacterStream(new StringReader(goldStandardXml));
            doc = db.parse(inStream);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        NodeList articleNodes = doc.getElementsByTagName("article");
        checkUniqueNode(articleNodes);

        // There should be one and only one 'article' element in a CAS.
        Element article = (Element) articleNodes.item(0);
        annotateArticle(goldStandardView, article, srcDocInfo.getDocumentSize());

        NodeList sentenceNodes = article.getElementsByTagName("sentence");
        checkNodes(sentenceNodes);

        for (int j = 0; j < sentenceNodes.getLength(); j++) {
            Element sentence = (Element) sentenceNodes.item(j);

            NodeList wordNodes = sentence.getElementsByTagName("word");
            // checkNodes(wordNodes);

            if (wordNodes == null || wordNodes.getLength() == 0) {
                continue;
            }

            Element firstWord = getFirstNonEllipticalWord(wordNodes, false);
            Element lastWord = getLastNonEllipticalWord(wordNodes, false);
            annotateSentence(goldStandardView, sentence, firstWord, lastWord);

            for (int k = 0; k < wordNodes.getLength(); k++) {
                Element word = (Element) wordNodes.item(k);
                annotateWord(goldStandardView, word);
            }
        }

        // Process the 'events' element.
        NodeList eventsNodes = article.getElementsByTagName("events");
        checkUniqueNode(eventsNodes);

        // There should be one and only one 'events' element in a CAS.
        Element events = (Element) eventsNodes.item(0);

        NodeList eventNodes = events.getElementsByTagName("event");

        for (int i = 0; i < eventNodes.getLength(); i++) {
            Element eventMention = (Element) eventNodes.item(i);
            annotateEventMention(goldStandardView, eventMention);
        }

        // Process the 'eventgroups' element.
        NodeList eventClustersNodes = article.getElementsByTagName("eventclusters");
        checkUniqueNode(eventClustersNodes);

        // There should be one and only one 'events' element in a CAS.
        Element eventclusters = (Element) eventClustersNodes.item(0);

        NodeList eventClusterNodes = eventclusters.getElementsByTagName("eventcluster");

        for (int i = 0; i < eventClusterNodes.getLength(); i++) {
            Element eventcluster = (Element) eventClusterNodes.item(i);
            annotateEventCoreferenceCluster(goldStandardView, eventcluster);
        }

        // Process the 'relations' element.
        NodeList relationsNodes = article.getElementsByTagName("relations");
        checkUniqueNode(relationsNodes);

        // There should be one and only one 'events' element in a CAS.
        Element relations = (Element) relationsNodes.item(0);

        NodeList relationNodes = relations.getElementsByTagName("relation");

        for (int i = 0; i < relationNodes.getLength(); i++) {
            Element relation = (Element) relationNodes.item(i);
            annotateEventCoreferenceRelation(goldStandardView, relation);
        }

    }

    /**
     * Add the annotation of 'Article' to CAS.
     *
     * @param aJCas
     * @param article
     * @param articleSize
     * @throws AnalysisEngineProcessException
     */
    private void annotateArticle(JCas aJCas, Element article, Integer articleSize)
            throws AnalysisEngineProcessException {
        String articleId = article.getAttribute("aid");
        String articleName = article.getAttribute("aname");
        String language = article.getAttribute("lng");

        checkAttribute(articleId);
        checkAttribute(articleName);
        checkAttribute(language);

        Pattern p = Pattern.compile(".+_.+_(\\d+)");  // Pattern for 'AAA_BBB_YYYYMMDD'
        Matcher m = p.matcher(articleName);

        String articleDate = null;
        if (m.find()) {
            articleDate = m.group(1);
        } else {
            articleDate = TimeUtils.getCurrentYYYYMMDD();
        }

        Article ann = new Article(aJCas);
        ann.setArticleId(articleId);
        ann.setArticleName(articleName);
        ann.setArticleDate(articleDate);
        ;
        ann.setLanguage(language);
        ann.setBegin(0);
        ann.setEnd(articleSize);

        setGoldStandardComponentId(ann);
        ann.addToIndexes();
    }

    /**
     * Add the annotation of 'Sentence' to CAS.
     *
     * @param aJCas
     * @param sentence
     * @param firstWord
     * @param lastWord
     * @throws AnalysisEngineProcessException
     */
    private void annotateSentence(JCas aJCas, Element sentence, Element firstWord, Element lastWord)
            throws AnalysisEngineProcessException {
        String sentenceId = sentence.getAttribute("sid");
        String paragraphId = sentence.getAttribute("pid");

        checkAttribute(sentenceId);
        checkAttribute(paragraphId);

        Integer beginOfFirstWord = getWordBegin(firstWord);
        Integer endOfLastWord = getWordEnd(lastWord);

        // Check an existing paragraph annotation.
        Paragraph p = getParagraphAnnotation(aJCas, paragraphId);
        if (p == null) {
            // There is no existing annotation.
            Paragraph paragraphAnn = new Paragraph(aJCas);
            paragraphAnn.setParagraphId(paragraphId);
            paragraphAnn.setBegin(beginOfFirstWord);
            paragraphAnn.setEnd(endOfLastWord);

            setGoldStandardComponentId(paragraphAnn);
            paragraphAnn.addToIndexes();
        } else {
            p.setEnd(endOfLastWord);
            p.addToIndexes();
        }

        Sentence sentenceAnn = new Sentence(aJCas);
        sentenceAnn.setSentenceId(sentenceId);
        sentenceAnn.setParagraphId(paragraphId);

        if (beginOfFirstWord < 0 || endOfLastWord < 0) {
            throw new AnalysisEngineProcessException(
                    "Word elements do not have a valid 'start' attribute.", null);
        }
        sentenceAnn.setBegin(beginOfFirstWord);
        sentenceAnn.setEnd(endOfLastWord);

        setGoldStandardComponentId(sentenceAnn);
        sentenceAnn.addToIndexes();
    }

    /**
     * Add the annotation of 'Word' to CAS.
     *
     * @param aJCas
     * @param word
     * @throws AnalysisEngineProcessException
     */
    private void annotateWord(JCas aJCas, Element word) throws AnalysisEngineProcessException {
        String wordId = word.getAttribute("wid");
        String lemma = word.getAttribute("lem");
        String surfaceForm = word.getAttribute("wd");
        String start = word.getAttribute("start");

        checkAttribute(wordId);

        wordIdToWord.put(wordId, word);

        Word ann = new Word(aJCas);
        ann.setWordId(wordId);

        if (isElliptical(word)) {
            String elliptical = word.getAttribute("elliptical");
            ann.setElliptical(elliptical);
        } else {
            checkAttribute(lemma);
            checkAttribute(surfaceForm);
            checkAttribute(start);

            ann.setLemma(lemma);

            Integer begin = getWordBegin(word);
            Integer end = getWordEnd(word);
            if (begin < 0 || end < 0) {
                throw new AnalysisEngineProcessException(
                        "Word elements do not have a valid 'start' attribute.", null);
            }
            ann.setBegin(begin);
            ann.setEnd(end);
        }

        setGoldStandardComponentId(ann);
        ann.addToIndexes();
    }

    /**
     * Add the annotation of 'Event Mention' to CAS.
     *
     * @param aJCas
     * @param eventMention
     * @throws AnalysisEngineProcessException
     */
    private void annotateEventMention(JCas aJCas, Element eventMention)
            throws AnalysisEngineProcessException {
        String eventMentionId = eventMention.getAttribute("eid");
        String eventType = eventMention.getAttribute("eventType");
        String epistemicStatus = eventMention.getAttribute("epistemicStatus");

        checkAttribute(eventMentionId);
        checkAttribute(eventType);
        checkAttribute(epistemicStatus);

        EventMention ann = new EventMention(aJCas);
        ann.setGoldStandardEventMentionId(eventMentionId);
        ann.setEventType(eventType);
        ann.setEpistemicStatus(epistemicStatus);

        NodeList wordNodes = eventMention.getElementsByTagName("word");
        checkNodes(wordNodes);

        List<Word> words = new ArrayList<Word>();
        for (int i = 0; i < wordNodes.getLength(); i++) {
            Element wordElement = (Element) wordNodes.item(i);
            String wordId = wordElement.getAttribute("wid");
            if (wordId == null || wordId.length() == 0) {
                continue;
            }

            // Add the word to this event mention.
            List<Word> wordList = UimaConvenience.getAnnotationList(aJCas, Word.class);
            for (Word word : wordList) {
                if (wordId.equals(word.getWordId())) {
                    words.add(word);
                    break;
                }
            }
        }

    /*
     * if (words.size() > 0) { FSList wordList = FSCollectionFactory.createFSList(aJCas, words);
     * ann.setWords(wordList); }
     */

        Element firstWord = getFirstNonEllipticalWord(wordNodes, true);
        Element lastWord = getLastNonEllipticalWord(wordNodes, true);

        Integer begin = getWordBegin(firstWord);
        Integer end = getWordEnd(lastWord);
        ann.setBegin(begin);
        ann.setEnd(end);

        setGoldStandardComponentId(ann);
        ann.addToIndexes();
    }

    /**
     * Add the annotation of 'Event Coreference Cluster' to CAS. This method assumes that the process
     * of annotating 'Event Mention' is already done.
     *
     * @param aJCas
     * @param eventCluster
     * @throws AnalysisEngineProcessException
     */
    private void annotateEventCoreferenceCluster(JCas aJCas, Element eventCluster)
            throws AnalysisEngineProcessException {
        String eventCoreferenceClusterId = eventCluster.getAttribute("ecid");
        String eventCoreferenceClusterType = eventCluster.getAttribute("eventClusterType");

        checkAttribute(eventCoreferenceClusterId);

        EventCoreferenceCluster ann = new EventCoreferenceCluster(aJCas);
        ann.setClusterId(eventCoreferenceClusterId);
        ann.setClusterType(eventCoreferenceClusterType);

        NodeList eventNodes = eventCluster.getElementsByTagName("event");
        checkNodes(eventNodes);

        List<EventMention> eventMentionList = UimaConvenience.getAnnotationList(aJCas,
                EventMention.class);

        List<EventMention> eventMentions = new ArrayList<EventMention>();
        for (int i = 0; i < eventNodes.getLength(); i++) {
            Element event = (Element) eventNodes.item(i);
            String eventMentionId = event.getAttribute("eid");
            if (StringUtils.isNullOrEmptyString(eventMentionId)) {
                continue;
            }

            String nodeType = event.getAttribute("nodeType");

            // Add a parent or a child to that event coreference cluster.
            for (EventMention eventMention : eventMentionList) {
                if (eventMentionId.equals(eventMention.getGoldStandardEventMentionId())) {
                    if ("parent".equals(nodeType)) {
                        ann.setParentEventMention(eventMention);
                    } else {
                        eventMentions.add(eventMention);
                    }

                    break;
                }
            }
        }

        setGoldStandardComponentId(ann);

        // If there are two or more event mentions, they form a event coreference cluster.
        // if (eventMentions.size() > 1) {

        // For the time being, we want to consider all potential clusters, including a cluster with only
        // one child.
        if (eventMentions.size() > 0) {
            ann.setChildEventMentions(FSCollectionFactory.createFSList(aJCas, eventMentions));

            // Post-processing: add the gold standard ann of event coreference cluster to each
            // event mention
            for (EventMention eventMention : eventMentions) {
                FSList eccList = eventMention.getEventCoreferenceClusters();

                Collection<EventCoreferenceCluster> eccsNew = new ArrayList<EventCoreferenceCluster>();
                if (eccList != null) {
                    Collection<EventCoreferenceCluster> eccs = FSCollectionFactory.create(eccList,
                            EventCoreferenceCluster.class);
                    for (EventCoreferenceCluster ecc : eccs) {
                        eccsNew.add(ecc);
                    }
                }
                eccsNew.add(ann);
                eccList = FSCollectionFactory.createFSList(aJCas, eccsNew);

                eventMention.setEventCoreferenceClusters(eccList);
                eventMention.addToIndexes();
            }

        }

        ann.addToIndexes();
    }

    /**
     * Add the annotation of 'Relation' to CAS. This method assumes that the process of annotating
     * 'Event Mention' is already done.
     *
     * @param aJCas
     * @param relation
     * @throws AnalysisEngineProcessException
     */
    private void annotateEventCoreferenceRelation(JCas aJCas, Element relation)
            throws AnalysisEngineProcessException {
        String relationId = relation.getAttribute("rid");
        String relationType = relation.getAttribute("relationType");
        String fromEventMentionId = relation.getAttribute("fromEid");
        String toEventMentionId = relation.getAttribute("toEid");

        checkAttribute(relationId);
        checkAttribute(relationType);
        checkAttribute(fromEventMentionId);
        checkAttribute(toEventMentionId);

        EventCoreferenceRelation ann = new EventCoreferenceRelation(aJCas);
        ann.setRelationId(relationId);
        ann.setRelationType(relationType);

        if (fromEventMentionId == null || fromEventMentionId.length() == 0 || toEventMentionId == null
                || toEventMentionId.length() == 0) {
            throw new AnalysisEngineProcessException(
                    "The specified relation element does not have a valid event id", null);
        }

        List<EventMention> eventMentionList = UimaConvenience.getAnnotationList(aJCas,
                EventMention.class);
        Boolean fromFlag, toFlag;
        fromFlag = toFlag = false;

        for (EventMention eventMention : eventMentionList) {
            String eventMentionId = eventMention.getGoldStandardEventMentionId();

            if (fromEventMentionId.equals(eventMentionId)) {
                ann.setFromEventMention(eventMention);
                fromFlag = true;
            }
            if (toEventMentionId.equals(eventMentionId)) {
                ann.setToEventMention(eventMention);
                toFlag = true;
            }

            if (fromFlag && toFlag) {
                break;
            }
        }

        ann.setConfidence(1.0);
        setGoldStandardComponentId(ann);

        ann.addToIndexes();
    }

    /**
     * Returns a paragraph annotation with the specified paragraph ID.
     *
     * @param aJCas
     * @param paragraphId
     * @return a paragraph annotation with the specified paragraph ID
     */
    private Paragraph getParagraphAnnotation(JCas aJCas, String paragraphId) {
        if (StringUtils.isNullOrEmptyString(paragraphId)) {
            return null;
        }

        List<Paragraph> paragraphList = UimaConvenience.getAnnotationList(aJCas, Paragraph.class);
        for (Paragraph paragraph : paragraphList) {
            if (paragraphId.equals(paragraph.getParagraphId())) {
                return paragraph;
            }
        }

        return null;
    }

    @SuppressWarnings("unused")
    private void logElement(Element e) {
        StringBuilder buf = new StringBuilder();
        buf.append("node: ");
        buf.append(e.getNodeName());
        buf.append(", attributes: ");
        for (int i = 0; i < e.getAttributes().getLength(); i++) {
            if (i != 0) {
                buf.append(", ");
            }
            buf.append(e.getAttributes().item(i));
        }

        logger.info(buf.toString());
    }

    private void checkNodes(NodeList nodes) throws AnalysisEngineProcessException {
        if (XmlUtils.hasNoNodes(nodes)) {
            throw new AnalysisEngineProcessException("There are no nodes.", null);
        }
    }

    private void checkUniqueNode(NodeList nodes) throws AnalysisEngineProcessException {
        if (!XmlUtils.hasUniqueNode(nodes)) {
            throw new AnalysisEngineProcessException("There are two or more nodes.", null);
        }
    }

    private void checkAttribute(String attr) throws AnalysisEngineProcessException {
        if (XmlUtils.hasNoAttribute(attr)) {
            throw new AnalysisEngineProcessException("This attribute does not have any values: " + attr,
                    null);
        }
    }

    /**
     * Returns the begin position of the specified word. Returns -1 if there is an error.
     *
     * @param word
     * @return the begin position of the specified word
     */
    private Integer getWordBegin(Element word) {
        String start = word.getAttribute("start");

        Integer begin = StringUtils.convertStringToInteger(start);
        if (begin == null) {
            return -1;
        }

        return begin;
    }

    /**
     * Returns the end position of the specified word. Returns -1 if there is an error.
     *
     * @param word
     * @return the end position of the specified word
     */
    private Integer getWordEnd(Element word) {
        String surfaceForm = word.getAttribute("wd");
        Integer begin = getWordBegin(word);

        if (begin == -1) {
            return -1;
        }

        return (begin + surfaceForm.length());
    }

    /**
     * Returns true if the specified word is elliptical; otherwise returns false.
     *
     * @param word
     * @return
     */
    private Boolean isElliptical(Element word) {
        if (StringUtils.isNullOrEmptyString(word.getAttribute("elliptical"))) {
            return false;
        }
        return true;
    }

    private Element getFirstNonEllipticalWord(NodeList wordNodes, Boolean useHashMap) {
        Element firstWord = null;
        for (int k = 0; k < wordNodes.getLength(); k++) {
            firstWord = (Element) wordNodes.item(k);
            if (useHashMap) {
                String wordId = firstWord.getAttribute("wid");
                firstWord = wordIdToWord.get(wordId);
            }

            if (!isElliptical(firstWord)) {
                break;
            }
        }

        return firstWord;
    }

    private Element getLastNonEllipticalWord(NodeList wordNodes, Boolean useHashMap) {
        Element lastWord = null;
        for (int k = wordNodes.getLength() - 1; k >= 0; k--) {
            lastWord = (Element) wordNodes.item(k);
            if (useHashMap) {
                String wordId = lastWord.getAttribute("wid");
                lastWord = wordIdToWord.get(wordId);
            }

            if (!isElliptical(lastWord)) {
                break;
            }
        }

        return lastWord;
    }

}
