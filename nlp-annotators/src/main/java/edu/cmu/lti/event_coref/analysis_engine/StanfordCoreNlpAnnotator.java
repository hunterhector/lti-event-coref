package edu.cmu.lti.event_coref.analysis_engine;

import com.google.common.collect.ArrayListMultimap;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.utils.general.ErrorUtils;
import edu.cmu.lti.utils.general.FileUtils;
import edu.cmu.lti.utils.general.TimeUtils;
import edu.cmu.lti.utils.model.Span;
import edu.cmu.lti.utils.uima.UimaConvenience;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class uses the Stanford Corenlp Pipeline to annotate POS, NER, Parsing
 * and entity coreference
 * <p/>
 * The current issue is that it does not split on DATETIME and TITLE correctly.
 *
 * @author Zhengzhong Liu, Hector
 * @author Jun Araki
 */
public class StanfordCoreNlpAnnotator extends JCasAnnotator_ImplBase {
    public final static String PARAM_USE_SUTIME = "UseSuTime";

    public final static String PARAM_SU_TIME_CONF = "SuTimeConfPath";

    @ConfigurationParameter(name = PARAM_USE_SUTIME)
    private Boolean useSUTime;

    @ConfigurationParameter(name = PARAM_SU_TIME_CONF, mandatory = false)
    private String suPath;

    private final static String ANNOTATOR_COMPONENT_ID = "System-stanford-corenlp";

    private StanfordCoreNLP pipeline;

    private final static String PARSE_TREE_ROOT_NODE_LABEL = "ROOT";

    private static final Logger logger = LoggerFactory.getLogger(StanfordCoreNlpAnnotator.class);

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException {
        super.initialize(aContext);

        Properties props = new Properties();
        props.setProperty("annotators",
                "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        props.setProperty("dcoref.postprocessing", "true");
        if (useSUTime) {
            props.setProperty("ner.useSUTime", "true");
            if (suPath == null) {
                throw new IllegalArgumentException("SU time configuration path is not provided");
            }

            if (!new File(suPath).isDirectory()) {
                throw new IllegalArgumentException(String.format("Cannot find SU conf path [%s]", suPath));
            }
        } else {
            props.setProperty("ner.useSUTime", "false");
        }

        pipeline = new StanfordCoreNLP(props);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        UimaConvenience.printProcessLog(aJCas, logger);

        String documentText = aJCas.getDocumentText();

        // add period for title in a hacky way assume first sentence is title
        List<Sentence> sentences = new ArrayList<Sentence>(JCasUtil.select(
                aJCas, Sentence.class));
        if (!sentences.isEmpty()) {
            Sentence firstSent = sentences.get(0);
            int titleEnd = firstSent.getEnd();
            documentText = replaceAt(documentText, titleEnd, '.');
        }

        Annotation document = new Annotation(documentText);
        pipeline.annotate(document);

        Map<Span, StanfordEntityMention> spanMentionMap = new HashMap<Span, StanfordEntityMention>();

        List<CoreMap> sentAnnos = document.get(SentencesAnnotation.class);

        // The following put token annotation to CAS
        int tokenId = 1;
        String preNe = "";
        int neBegin = 0;
        int neEnd = 0;
        for (CoreLabel token : document.get(TokensAnnotation.class)) {
            int beginIndex = token.get(CharacterOffsetBeginAnnotation.class);
            int endIndex = token.get(CharacterOffsetEndAnnotation.class);

            StanfordCorenlpToken sToken = new StanfordCorenlpToken(aJCas,
                    beginIndex, endIndex);
            sToken.setTokenId(tokenId);
            sToken.setPos(token.get(PartOfSpeechAnnotation.class));
            sToken.setLemma(token.get(LemmaAnnotation.class));
            sToken.addToIndexes(aJCas);
            tokenId++;

            // Add NER annotation
            String ne = token.get(NamedEntityTagAnnotation.class);
            if (ne != null) {
                if (!ne.equals(preNe) || preNe.equals("")) {
                    if (preNe.equals("")) {
                        // if the previous is start of sentence(no label).
                        neBegin = beginIndex;
                        preNe = ne;
                    } else {
                        if (!preNe.equals("O")) {// "O" represent no label (other)
                            StanfordEntityMention sne = new StanfordEntityMention(
                                    aJCas);
                            sne.setBegin(neBegin);
                            sne.setEnd(neEnd);
                            sne.setEntityType(preNe);
                            spanMentionMap.put(new Span(neBegin, neEnd), sne);
                        }
                        // set the next span of NE
                        neBegin = beginIndex;
                        preNe = ne;
                    }
                }
                neEnd = endIndex;
            }
        }

        int sentenceId = 1;
        for (CoreMap sentAnno : sentAnnos) {
            // The following add Stanford sentence to CAS
            int begin = sentAnno.get(CharacterOffsetBeginAnnotation.class);
            int end = sentAnno.get(CharacterOffsetEndAnnotation.class);
            StanfordCorenlpSentence sSent = new StanfordCorenlpSentence(aJCas,
                    begin, end);
            sSent.setSentenceId(sentenceId);
            sSent.addToIndexes(aJCas);
            sentenceId++;

            // The following deals with tree annotation
            Tree tree = sentAnno.get(TreeAnnotation.class);
            addPennTreeAnnotation(tree, aJCas, null);

            // the following add the collapsed cc processed dependencies of each sentence into CAS annotation
            SemanticGraph depends = sentAnno
                    .get(CollapsedCCProcessedDependenciesAnnotation.class);
            // SemanticGraph basicDepends =
            // sentAnno.get(BasicDependenciesAnnotation.class);

            List<StanfordCorenlpToken> tokens = JCasUtil.selectCovered(aJCas,
                    StanfordCorenlpToken.class, sSent);
            Map<IndexedWord, StanfordDependencyNode> stanford2UimaMap = new HashMap<IndexedWord, StanfordDependencyNode>();
            for (IndexedWord stanfordNode : depends.vertexSet()) {
                int indexBegin = stanfordNode.get(BeginIndexAnnotation.class);
                int indexEnd = stanfordNode.get(EndIndexAnnotation.class);
                StanfordCorenlpToken sToken = tokens.get(indexBegin);
                int tokenBegin = sToken.getBegin();
                int tokenEnd = tokens.get(indexEnd - 1).getEnd();

                if (indexBegin + 1 != indexEnd) {
                    System.err
                            .print("Dependency node is not exactly one token here!");
                }
                StanfordDependencyNode node;

                // here we make new annotation called DependencyNode, it is also
                // possible to reuse
                // StanfordToken.
                if (depends.getRoots().contains(stanfordNode)) {
                    node = new StanfordDependencyRootNode(aJCas, tokenBegin,
                            tokenEnd);
                } else {
                    node = new StanfordDependencyNode(aJCas, tokenBegin,
                            tokenEnd);
                }

                node.setToken(sToken);

                stanford2UimaMap.put(stanfordNode, node);
            }

            ArrayListMultimap<StanfordDependencyNode, StanfordDependencyRelation> headRelationMap = ArrayListMultimap
                    .create();
            ArrayListMultimap<StanfordDependencyNode, StanfordDependencyRelation> childRelationMap = ArrayListMultimap
                    .create();

            for (SemanticGraphEdge stanfordEdge : depends.edgeIterable()) {
                String edgeType = stanfordEdge.getRelation().toString();
                // weight is usually infinity
                double edgeWeight = stanfordEdge.getWeight();
                StanfordDependencyNode head = stanford2UimaMap.get(stanfordEdge
                        .getGovernor());
                StanfordDependencyNode child = stanford2UimaMap
                        .get(stanfordEdge.getDependent());

                StanfordDependencyRelation sr = new StanfordDependencyRelation(
                        aJCas);
                sr.setHead(head);
                sr.setChild(child);
                sr.setWeight(edgeWeight);
                sr.setRelationType(edgeType);
                sr.addToIndexes(aJCas);

                headRelationMap.put(child, sr);
                childRelationMap.put(head, sr);
            }

            // associate the edges to the nodes
            for (StanfordDependencyNode sNode : stanford2UimaMap.values()) {
                if (headRelationMap.containsKey(sNode)) {
                    sNode.setHeadRelations(FSCollectionFactory.createFSList(
                            aJCas, headRelationMap.get(sNode)));
                }
                if (childRelationMap.containsKey(sNode)) {
                    sNode.setChildRelations(FSCollectionFactory.createFSList(
                            aJCas, childRelationMap.get(sNode)));
                }
                sNode.addToIndexes(aJCas);
            }
        }

        // the following set the coreference chain to CAS annotation
        Map<Integer, CorefChain> graph = document
                .get(CorefChainAnnotation.class);

        List<List<StanfordCorenlpToken>> sentTokens = new ArrayList<List<StanfordCorenlpToken>>();

        for (StanfordCorenlpSentence sSent : JCasUtil.select(aJCas,
                StanfordCorenlpSentence.class)) {
            sentTokens.add(JCasUtil.selectCovered(aJCas,
                    StanfordCorenlpToken.class, sSent));
        }

        for (Entry<Integer, CorefChain> entry : graph.entrySet()) {
            CorefChain refChain = entry.getValue();
            StanfordEntityCoreferenceCluster cc = new StanfordEntityCoreferenceCluster(
                    aJCas);
            List<StanfordEntityMention> semInCluster = new ArrayList<StanfordEntityMention>();

            TObjectIntMap<String> typeCount = new TObjectIntHashMap<>();

            for (CorefMention mention : refChain.getMentionsInTextualOrder()) {
                List<StanfordCorenlpToken> sTokens = sentTokens
                        .get(mention.sentNum - 1);
                int begin = sTokens.get(mention.startIndex - 1).getBegin();
                int end = sTokens.get(mention.endIndex - 2).getEnd();

                StanfordEntityMention em;
                Span thisSpan = new Span(begin, end);
                if (spanMentionMap.containsKey(thisSpan)) {
                    em = spanMentionMap.get(thisSpan);
                    if (em.getEntityType() != null) {
                        typeCount.adjustOrPutValue(em.getEntityType(), 1, 1);
                    }
                } else {
                    em = new StanfordEntityMention(aJCas, begin, end);
                    spanMentionMap.put(thisSpan, em);
                }
                em.setEntityCoreferenceCluster(cc);
                semInCluster.add(em);
            }

            int maxTypeCount = 0;
            String maxType = null;
            for (TObjectIntIterator<String> iter = typeCount.iterator(); iter.hasNext(); ) {
                iter.advance();
                if (iter.value() > maxTypeCount) {
                    maxType = iter.key();
                    maxTypeCount = iter.value();
                }
            }

            //transfer mention type in cluster
            for (StanfordEntityMention mention : semInCluster) {
                if (mention.getEntityType() == null && maxType != null) {
                    mention.setEntityType(maxType);
                    logger.debug(String.format("Mention [%s] assigned with cluster type [%s]",
                            mention.getCoveredText(), maxType));
                }
            }

            // convert the list to CAS entity mention FSList type
            FSList mentionFSList = FSCollectionFactory.createFSList(aJCas,
                    semInCluster);

            // Put that in the cluster type
            cc.setEntityMentions(mentionFSList);
            cc.addToIndexes(aJCas);
        }


        //add to indices at once to avoid jcas confusion
        for (Entry<Span, StanfordEntityMention> span2Mention : spanMentionMap.entrySet()) {
            span2Mention.getValue().addToIndexes();
        }

        if (!useSUTime) {
            return;
        }

        // The following add Time annotation
        // TimeAnnotator should come after the tokenizer, sentence splitter,
        // and pos tagger
        Properties propsTime = new Properties();

        propsTime.put("sutime.rules", suPath + "/defs.sutime.txt" + "," + suPath + "/english.sutime.txt" + "," + suPath + "/english.holidays.sutime.txt");
        propsTime.put("sutime.markTimeRanges", true);
        propsTime.put("sutime.includeNested", true);
        propsTime.put("sutime.teRelHeurLevel", "MORE");

        TimeAnnotator timeAnnotator = new TimeAnnotator("sutime", propsTime);

        // temporarily edit by adapting ArticleDateAnnotator the following to
        // add datatime
        SourceDocumentInformation srcDocInfo = JCasUtil.selectSingle(aJCas,
                SourceDocumentInformation.class);
        String srcDocURI = srcDocInfo.getUri();
        String fileShortName = FileUtils.getShortName(srcDocURI); // Got
        // 'AAA_BBB_YYYYMMDD'

        Pattern p = Pattern.compile(".+_.+_(\\d+)");
        Matcher m = p.matcher(fileShortName);

        String articleDate = null;
        if (m.find()) {
            articleDate = m.group(1);
        } else {
            articleDate = TimeUtils.getCurrentYYYYMMDD();
        }

        ErrorUtils.terminateIfFalse(TimeUtils.isValidYYYYMMDD(articleDate),
                "Invalid date: " + articleDate);

        document.set(DocDateAnnotation.class, articleDate);
        timeAnnotator.annotate(document);

        List<CoreMap> timexes = document.get(TimeAnnotations.TimexAnnotations.class);
        for (CoreMap timex : timexes) {
            int begin = timex.get(CharacterOffsetBeginAnnotation.class);
            int end = timex.get(CharacterOffsetEndAnnotation.class);
            Timex timexObj = timex.get(TimeAnnotations.TimexAnnotation.class);

            StanfordCoreNlpTimex annTimex = new StanfordCoreNlpTimex(aJCas);
            annTimex.setBegin(begin);
            annTimex.setEnd(end);
            annTimex.setTid(timexObj.tid());
            annTimex.setTimexType(timexObj.timexType());
            annTimex.setValue(timexObj.value());
            annTimex.setComponentId(ANNOTATOR_COMPONENT_ID);
            annTimex.addToIndexes();
        }
    }

    private StanfordTreeAnnotation addPennTreeAnnotation(Tree currentNode,
                                                         JCas aJCas, StanfordTreeAnnotation parent) {
        StanfordTreeAnnotation treeAnno = new StanfordTreeAnnotation(aJCas);

        if (!currentNode.isLeaf()) {
            int thisBegin = 0;
            int thisEnd = 0;
            int count = 0;
            int numChild = currentNode.children().length;
            String currentNodeLabel = currentNode.value();

            List<StanfordTreeAnnotation> childrenList = new ArrayList<StanfordTreeAnnotation>();
            for (Tree child : currentNode.children()) {
                StanfordTreeAnnotation childTree = addPennTreeAnnotation(child,
                        aJCas, treeAnno);
                childrenList.add(childTree);
                if (count == 0) {
                    thisBegin = childTree.getBegin();
                }
                if (count == numChild - 1) {
                    thisEnd = childTree.getEnd();
                }
                count++;
            }

            if (PARSE_TREE_ROOT_NODE_LABEL.equals(currentNodeLabel)) {
                treeAnno.setIsRoot(true);
            } else {
                treeAnno.setIsRoot(false);
            }

            treeAnno.setBegin(thisBegin);
            treeAnno.setEnd(thisEnd);
            treeAnno.setPennTreeLabel(currentNodeLabel);
            treeAnno.setParent(parent);
            treeAnno.setChildren(FSCollectionFactory.createFSList(aJCas,
                    childrenList));
            treeAnno.setIsLeaf(false);
            treeAnno.setComponentId(ANNOTATOR_COMPONENT_ID);
            treeAnno.addToIndexes(aJCas);
            return treeAnno;
        } else {
            ArrayList<Word> words = currentNode.yieldWords();
            StanfordTreeAnnotation leafTree = new StanfordTreeAnnotation(aJCas);
            leafTree.setBegin(words.get(0).beginPosition());
            leafTree.setEnd(words.get(words.size() - 1).endPosition());
            leafTree.setPennTreeLabel(currentNode.value());
            leafTree.setIsLeaf(true);
            leafTree.setParent(parent);
            leafTree.setComponentId(ANNOTATOR_COMPONENT_ID);
            leafTree.addToIndexes(aJCas);

            return leafTree;
        }
    }

    private String replaceAt(String str, int index, char c) {
        if (index >= str.length()) {
            return str;
        } else {
            return str.substring(0, index) + c + str.substring(index + 1);
        }
    }

}