/**
 *
 */
package edu.cmu.lti.event_coref.analysis_engine.semantic;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.APLUtils;
import edu.cmu.lti.utils.ling.FrameDataReader;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Hector
 *         <p/>
 *         This class creates arguments for events mentions based on semafor. As the type system
 *         defines that argument relation is by connecting Event(Mention) to Entity(Mention).
 *         Sometimes new Entities might be created during the process. Actually another unification
 *         step should be done after this to make sure entities are connected together.
 */
public class FrameBasedEventArgumentExtractor extends JCasAnnotator_ImplBase {

    public static final String PARAM_REDUCE_TO_VERB_NET = "reduceToWordNet";

    public static final String PARAM_VN2FN_MAP_PATH = "vb2fnMappingPath";

    public static final String PARAM_FN_RELATION_PATH = "fnRelationPath";

    public static final String PARAM_VN2PB_MAP_PATH = "vb2pbMappingPath";

    @ConfigurationParameter(name = PARAM_REDUCE_TO_VERB_NET)
    boolean reduceToVerbNet = false;

    @ConfigurationParameter(name = PARAM_VN2FN_MAP_PATH, mandatory = true)
    String vn2FnMappingPath;

    @ConfigurationParameter(name = PARAM_VN2PB_MAP_PATH, mandatory = true)
    String vn2pbMappingPath;

    @ConfigurationParameter(name = PARAM_FN_RELATION_PATH, mandatory = true)
    String fnRelatonPath;

    private final String ANNOTATOR_COMPONENT_ID = "System_" + this.getClass().getSimpleName();

    private Map<Pair<String, String>, Pair<String, String>> fn2VnMapping;

    private Map<String, Table<String, String, Map<String, String>>> frameRelationMappings;

    private Map<Pair<String, String>, Pair<String, String>> vn2pbMapping;

    private static final Logger logger = LoggerFactory.getLogger(FrameBasedEventArgumentExtractor.class);

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
        fn2VnMapping = FrameDataReader.getFN2VNRoleMap(vn2FnMappingPath, false);
        frameRelationMappings = FrameDataReader.getFrameRelations(fnRelatonPath);
        vn2pbMapping = FrameDataReader.getVN2PBRoleMap(vn2pbMappingPath, false);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        UimaConvenience.printProcessLog(aJCas, logger);
        ArrayListMultimap<Word, SemaforLabel> word2Labels = labelMatch(aJCas);

        ArrayListMultimap<SemaforLabel, SemaforLabel> target2Elements = ArrayListMultimap.create();

        // assuming single frame name for single label
        Map<SemaforLabel, String> target2FrameName = new HashMap<SemaforLabel, String>();


        for (SemaforAnnotationSet set : JCasUtil.select(aJCas, SemaforAnnotationSet.class)) {
            SemaforLabel targetLabel = null;
            List<SemaforLabel> elementLabels = new ArrayList<SemaforLabel>();
            for (SemaforLayer layer : FSCollectionFactory.create(set.getLayers(), SemaforLayer.class)) {
                String layerName = layer.getName();
                if (layerName.equals("Target")) {// Target that invoke the frame
                    if (layer.getLabels().size() > 0) {
                        targetLabel = layer.getLabels(0);
                    }
                } else if (layerName.equals("FE")) {// Frame element
                    FSArray elements = layer.getLabels();
                    if (elements != null) {
                        for (SemaforLabel element : FSCollectionFactory.create(elements, SemaforLabel.class)) {
                            elementLabels.add(element);
                        }
                    }
                }
            }

            if (targetLabel != null) {
                target2Elements.putAll(targetLabel, elementLabels);
                String frameName = set.getFrameName();
                if (target2FrameName.containsKey(targetLabel)) {
                    logger.debug("One target evoked multiple frames! Overwrite old one");
                }
                target2FrameName.put(targetLabel, frameName);
            }
        }

        HashMultimap<Word, EntityMention> word2EntityMentions = APLUtils.getWord2Entities(aJCas);

        logger.debug("Start extracting arguments for event mentions");

        Collection<EventMention> eventMentions = JCasUtil.select(aJCas, EventMention.class);

        for (EventMention evm : eventMentions) {
            // extract all the tagets covered by the event mention
            Set<SemaforLabel> targets = new HashSet<SemaforLabel>();
            for (Word word : JCasUtil.selectCovered(Word.class, evm)) {
                for (SemaforLabel label : word2Labels.get(word)) {
                    if (label.getName().equals("Target")) {
                        targets.add(label);
                    }
                }
            }

            Set<SemaforLabel> headTargets = new HashSet<SemaforLabel>();

            SemaforLabel targetForEventMention = null;

            // choose among the targets which to use
            if (targets.size() > 1) {
                for (SemaforLabel label : word2Labels.get(evm.getHeadWord())) {
                    if (label.getName().equals("Target")) {
                        headTargets.add(label);
                    }
                }
                if (headTargets.size() > 0) {
                    // use the first target in head word targets
                    targetForEventMention = Iterables.get(headTargets, 0);
                    if (headTargets.size() > 1) {
                        logger.debug("Event head word of event mention relates to multiple targets, using the first one");
                    }
                } else {
                    // use the first target in targets in head word targets is empty
                    targetForEventMention = Iterables.get(targets, 0);
                    logger.debug("The event head word does not relates to any target, using other words in the event mention");
                }
            } else if (targets.size() > 0) {
                targetForEventMention = Iterables.get(targets, 0);
            }

            // create argument links from the target
            if (targetForEventMention != null) {
                // System.out.println("Target found "
                // + targetForEventMention.getCoveredText().replace("\n", " "));

                List<EventMentionArgumentLink> argumentLinks = new ArrayList<EventMentionArgumentLink>();
                String frameName = target2FrameName.get(targetForEventMention);
                for (SemaforLabel frameElement : target2Elements.get(targetForEventMention)) {
                    String frameElementName = frameElement.getName();

                    List<Triplet<String, String, String>> superFeNames = getFrameNetSuperRoleName(frameName,
                            frameElementName);

                    Pair<String, String> verbNetRole = getVerbNetRole(frameName, frameElementName,
                            superFeNames);
                    String verbNetClass = verbNetRole.getValue0();
                    String verbNetRoleName = verbNetRole.getValue1();

                    Pair<String, String> pbRole = getPbRoleNumber(verbNetClass, verbNetRoleName);

                    String pbRoleName = null;
                    if (pbRole != null) {
                        pbRoleName = pbRole.getValue1();
                    }

                    List<EntityMention> entityMentions = APLUtils.getContainedEntityMentionOrCreateNew(aJCas,
                            frameElement, word2EntityMentions, ANNOTATOR_COMPONENT_ID);

                    EventMentionArgumentLink argumentLink = new EventMentionArgumentLink(aJCas);

                    argumentLink.setArgument(APLUtils.createEntityBasedComponent(aJCas,
                            frameElement.getBegin(), frameElement.getEnd(), ANNOTATOR_COMPONENT_ID));
                    argumentLink.setEventMention(evm);
                    argumentLink.setVerbNetRoleName(verbNetRoleName);
                    argumentLink.setFrameElementName(frameElementName);
                    argumentLink.setPropbankRoleName(pbRoleName);

                    List<String> superFeNameStrs = new ArrayList<String>();
                    for (Triplet<String, String, String> superFeName : superFeNames) {
                        superFeNameStrs.add(String.format("%s#%s#%s", superFeName.getValue0(),
                                superFeName.getValue1(), superFeName.getValue2()));
                    }
                    argumentLink.setSuperFrameElementRoleNames(FSCollectionFactory.createStringList(aJCas,
                            superFeNameStrs));
                    argumentLink.addToIndexes();
                    argumentLink.setComponentId(ANNOTATOR_COMPONENT_ID);
                    argumentLinks.add(argumentLink);
                }

                // NOTE that some times the target is found, but argument set is empty
                // System.out.println(String.format("Create %d arguments",argumentLinks.size()));
                evm.setArguments(FSCollectionFactory.createFSList(aJCas, argumentLinks));
                evm.setFrameName(frameName);
            }
        }

        APLUtils.updateComponentEntityMentions(aJCas, ANNOTATOR_COMPONENT_ID);
    }

    private Pair<String, String> getVerbNetRole(String frameName, String frameElementName,
                                                List<Triplet<String, String, String>> superFeNames) {
        Pair<String, String> framePair = new Pair<String, String>(frameName, frameElementName);
        if (fn2VnMapping.containsKey(framePair)) {
            return fn2VnMapping.get(framePair);
        } else {
            for (Triplet<String, String, String> superFeName : superFeNames) {
                String superFrameName = superFeName.getValue1();
                String superFrameElementName = superFeName.getValue2();

                Pair<String, String> superFramePair = new Pair<String, String>(superFrameName,
                        superFrameElementName);

                if (fn2VnMapping.containsKey(superFramePair)) {
                    // System.out.println("Mapping to verbnet found in super frames");
                    return fn2VnMapping.get(new Pair<String, String>(superFrameName, superFrameElementName));
                }
            }

            return new Pair<String, String>(null, null);
        }
    }

    private Pair<String, String> getPbRoleNumber(String vnClass, String vnRoleName) {
        Pair<String, String> vnPair = new Pair<String, String>(vnClass, vnRoleName);
        if (vn2pbMapping.containsKey(vnPair)) {
            return vn2pbMapping.get(vnPair);
        } else {
            return null;
        }
    }

    private List<Triplet<String, String, String>> getFrameNetSuperRoleName(String frameName,
                                                                           String frameElementName) {
        List<Triplet<String, String, String>> superRoleNames = new ArrayList<Triplet<String, String, String>>();

        for (Entry<String, Table<String, String, Map<String, String>>> fn : frameRelationMappings
                .entrySet()) {
            String relationName = fn.getKey();
            Table<String, String, Map<String, String>> mappingTable = fn.getValue();
            for (Entry<String, Map<String, String>> superFrame : mappingTable.row(frameName).entrySet()) {
                String superFrameName = superFrame.getKey();
                Map<String, String> feMappings = superFrame.getValue();
                String superFeName = feMappings.get(frameElementName);

                if (superFeName != null) {
                    Triplet<String, String, String> triple = new Triplet<String, String, String>(
                            relationName, superFrameName, superFeName);

                    superRoleNames.add(triple);
                }
            }
        }

        return superRoleNames;
    }

    private ArrayListMultimap<Word, SemaforLabel> labelMatch(JCas aJCas) {
        Map<SemaforLabel, Collection<Word>> labelCoveredWords = JCasUtil.indexCovered(aJCas,
                SemaforLabel.class, Word.class);

        Map<SemaforLabel, Collection<Word>> labelCoveringWords = JCasUtil.indexCovering(aJCas,
                SemaforLabel.class, Word.class);

        ArrayListMultimap<Word, SemaforLabel> word2Label = ArrayListMultimap.create();

        for (Entry<SemaforLabel, Collection<Word>> entry : labelCoveredWords.entrySet()) {
            SemaforLabel label = entry.getKey();
            Collection<Word> words = entry.getValue();
            if (words.size() > 0) {
                for (Word word : words) {
                    word2Label.put(word, label);
                }
            } else {// in case the label didn't event cover one word

                Collection<Word> coveringWords = labelCoveringWords.get(label);

                if (coveringWords.size() == 0) {
                    logger.debug(String.format(
                            "The label %s [%d : %d] is neither covered by a word nor covering a word.",
                            label.getCoveredText(), label.getAddress(), label.getEnd()));
                }

                for (Word word : coveringWords) {
                    word2Label.put(word, label);
                }
            }
        }
        return word2Label;
    }
}
