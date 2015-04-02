/**
 *
 */
package edu.cmu.lti.event_coref.analysis_engine.mention;

import com.google.common.collect.HashMultimap;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.APLUtils;
import edu.cmu.lti.event_coref.utils.MappingDataReader;
import edu.cmu.lti.utils.general.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Annotated Event Mention using FrameNet annotations, could be obtained by
 * Semafor or FrameNet gold standard. This assumes Frame Annotations can
 * annotate events of certain type, however, the trigger word of a frame
 * (target) might not be the predicate, this annotator thus turn to use the
 * predicate as the target instead.
 *
 * @author Zhengzhong Liu, Hector
 */
public class ArgumentBasedEventMentionAnnotator extends JCasAnnotator_ImplBase {

    public final static String PARAM_EVENT_FRAME_NAME_PATH = "EventFrameNamePath";

    public final static String PARAM_VN2FN_MAP_PATH = "vb2fnMappingPath";

    public final static String PARAM_GOLD_STANDARD_VIEW_NAME = "goldStandardViewName";

    private final String ANNOTATOR_COMPONENT_ID = "System_"
            + this.getClass().getSimpleName();

    @ConfigurationParameter(name = PARAM_EVENT_FRAME_NAME_PATH, mandatory = true)
    private String eventFramNamePath;

    @ConfigurationParameter(name = PARAM_VN2FN_MAP_PATH, mandatory = true)
    private String vn2FnMappingPath;

    @ConfigurationParameter(name = PARAM_GOLD_STANDARD_VIEW_NAME, mandatory = false)
    private String goldStandardViewName;

    private Set<String> eventFrameNames;

    private Map<Pair<String, String>, Pair<String, String>> fn2VnMapping;

    private static final Logger logger = LoggerFactory.getLogger(ArgumentBasedEventMentionAnnotator.class);

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException {
        super.initialize(context);

        File eventFrameFile = new File(eventFramNamePath);

        if (!eventFrameFile.exists()) {
            logger.error("The given event frame name file does not exists");
            throw new ResourceInitializationException();
        }

        List<String> eventFrameNamelist = FileUtils.readLines(eventFrameFile);

        eventFrameNames = new HashSet<String>(eventFrameNamelist);

        fn2VnMapping = MappingDataReader.getFN2VNMap(vn2FnMappingPath);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        HashMultimap<Word, EntityMention> word2EntityMentions = APLUtils
                .getWord2Entities(aJCas);

        JCas goldView = null;

        if (goldStandardViewName != null) {
            try {
                goldView = aJCas.getView(goldStandardViewName);
            } catch (CASException e) {
                logger.error("Gold standard view specified does not exists");
                e.printStackTrace();
            }
        }

        Set<FanseToken> containedFanse = new HashSet<>();

        for (SemaforAnnotationSet annoSet : JCasUtil.select(aJCas,
                SemaforAnnotationSet.class)) {
            String frameName = annoSet.getFrameName();

            if (!eventFrameNames.contains(frameName)) {
                continue;
            }

            SemaforLabel targetLabel = null;

            List<SemaforLabel> frameElements = new ArrayList<SemaforLabel>();

            for (SemaforLayer layer : FSCollectionFactory.create(
                    annoSet.getLayers(), SemaforLayer.class)) {
                String layerName = layer.getName();
                if (layerName.equals("Target")) {// Target that invoke the frame
                    targetLabel = layer.getLabels(0);
                } else if (layerName.equals("FE")) {// Frame element
                    FSArray elements = layer.getLabels();
                    if (elements != null) {
                        for (SemaforLabel element : FSCollectionFactory.create(
                                elements, SemaforLabel.class)) {
                            frameElements.add(element);
                        }
                    }
                } else { // there are other types of annotations which are not
                    // of interested at this moment

                }
            }

            if (targetLabel != null) {
                EventMention evm = new EventMention(aJCas);
                evm.setBegin(targetLabel.getBegin());
                evm.setEnd(targetLabel.getEnd());
                evm.setEventType("event");
                evm.setFrameName(frameName);
                evm.addToIndexes();
                if (goldView != null) {
                    evm.addToIndexes(goldView);
                }

                List<EventMentionArgumentLink> argumentLinks = new ArrayList<EventMentionArgumentLink>();

                for (SemaforLabel frameElement : frameElements) {
                    EventMentionArgumentLink argumentLink = new EventMentionArgumentLink(
                            aJCas);

                    String frameElementName = frameElement.getName();

                    String verNetRoleName = getVerbNetRoleName(frameName,
                            frameElementName);

                    // use the first contained entity as the entity
                    argumentLink.setArgument(APLUtils
                            .createEntityBasedComponent(aJCas,
                                    frameElement.getBegin(),
                                    frameElement.getEnd(),
                                    ANNOTATOR_COMPONENT_ID));
                    argumentLink.setEventMention(evm);
                    // use the frame name as role name
                    argumentLink.setVerbNetRoleName(verNetRoleName);
                    argumentLink.setFrameElementName(frameElementName);
                    argumentLink.addToIndexes();
                    argumentLink.setComponentId(ANNOTATOR_COMPONENT_ID);
                    argumentLinks.add(argumentLink);
                }
                evm.setArguments(FSCollectionFactory.createFSList(aJCas,
                        argumentLinks));

                for (FanseToken token : JCasUtil.selectCovered(
                        FanseToken.class, evm)) {
                    containedFanse.add(token);
                }

            }
        }

        for (FanseToken token : JCasUtil.select(aJCas, FanseToken.class)) {
            if (!containedFanse.contains(token)) {
                if (token.getPos().startsWith("V")) {
                    FSList semanticFS = token.getChildSemanticRelations();

                    boolean isEvent = false;

                    if (semanticFS != null) {
                        for (FanseSemanticRelation r : FSCollectionFactory.create(
                                semanticFS, FanseSemanticRelation.class)) {
                            if (r.getSemanticAnnotation().startsWith("ARG")) {
                                isEvent = true;
                                break;
                            }
                        }
                    }

                    if (isEvent) {
                        EventMention evm = new EventMention(aJCas);
                        evm.setBegin(token.getBegin());
                        evm.setEnd(token.getEnd());
                        evm.setEventType("event");
                        evm.addToIndexes();
                    }
                }
            }
        }

    }

    private String getVerbNetRoleName(String frameName, String frameElementName) {
        Pair<String, String> framePair = new Pair<String, String>(frameName,
                frameElementName);
        if (fn2VnMapping.containsKey(framePair)) {
            return fn2VnMapping.get(
                    new Pair<String, String>(frameName, frameElementName))
                    .getValue1();
        } else {
            // System.err.println(String.format("Corresponding fram role not found: <%s> - <%s>",
            // frameName,
            // frameElementName));
            return null;
        }
    }

}
