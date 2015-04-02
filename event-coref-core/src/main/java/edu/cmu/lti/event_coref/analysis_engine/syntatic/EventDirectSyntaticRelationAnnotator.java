package edu.cmu.lti.event_coref.analysis_engine.syntatic;

import com.google.common.collect.ArrayListMultimap;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.EventRelation;
import edu.cmu.lti.event_coref.type.FanseDependencyRelation;
import edu.cmu.lti.event_coref.type.FanseToken;
import edu.cmu.lti.event_coref.utils.FanseDependencyUtils;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EventDirectSyntaticRelationAnnotator extends JCasAnnotator_ImplBase {
    private static final String ANNOTATOR_COMPONENT_ID = "System-Event-Relations";
    private static final Logger logger = LoggerFactory.getLogger(EventDirectSyntaticRelationAnnotator.class);

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        logger.info(String.format("Processing article: %s with [%s]",
                UimaConvenience.getShortDocumentName(aJCas), this.getClass().getSimpleName()));
        Collection<EventMention> allEvents = JCasUtil.select(aJCas, EventMention.class);

        Map<FanseToken, EventMention> token2Events = new HashMap<FanseToken, EventMention>();
        for (EventMention eevm : allEvents) {
            for (FanseToken word : JCasUtil.selectCovered(FanseToken.class, eevm)) {
                token2Events.put(word, eevm);
            }
        }

        Collection<FanseDependencyRelation> sfDependencies = JCasUtil.select(aJCas,
                FanseDependencyRelation.class);

        // 1. Simply annotate all these relations
        ArrayListMultimap<EventMention, EventRelation> eventHeadRelations = ArrayListMultimap.create();
        ArrayListMultimap<EventMention, EventRelation> eventChildRelations = ArrayListMultimap.create();

        // changed to Fanse
        // for (StanfordDependencyRelation dependency : relationIndicatingDependencies) {
        for (FanseDependencyRelation dependency : sfDependencies) {
            if (dependency.getDependency().equals(FanseDependencyUtils.FANSE_ROOT_NODE)) {
                continue;
            }

            FanseToken head = dependency.getHead();
            FanseToken child = dependency.getChild();

            // Word headWord = JCasUtil.selectCovered(Word.class, head).get(0);
            // Word childWord = JCasUtil.selectCovered(Word.class, child).get(0);

            if (token2Events.containsKey(head) && token2Events.containsKey(child)) {
                EventMention headEvent = token2Events.get(head);
                EventMention childEvent = token2Events.get(child);
                if (!headEvent.equals(childEvent)) {
                    EventRelation eventRelation = createRelation(aJCas, headEvent, childEvent,
                            dependency.getDependency());
                    eventHeadRelations.put(childEvent, eventRelation);
                    eventChildRelations.put(headEvent, eventRelation);
                }
            }
        }

        for (EventMention aEvent : allEvents) {
            if (eventHeadRelations.containsKey(aEvent))
                aEvent.setHeadEventRelations(FSCollectionFactory.createFSList(aJCas,
                        eventHeadRelations.get(aEvent)));
            if (eventChildRelations.containsKey(aEvent))
                aEvent.setChildEventRelations(FSCollectionFactory.createFSList(aJCas,
                        eventChildRelations.get(aEvent)));
        }
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        super.collectionProcessComplete();
    }

    private EventRelation createRelation(JCas aJCas, EventMention headEvent, EventMention childEvent,
                                         String relationType) {
        if (headEvent.equals(childEvent))
            throw new IllegalArgumentException("Two events in a relation cannot be the same");
        EventRelation eventRelation = new EventRelation(aJCas);
        eventRelation.setHead(headEvent);
        eventRelation.setChild(childEvent);
        eventRelation.setRelationType(relationType);
        eventRelation.setComponentId(ANNOTATOR_COMPONENT_ID);
        eventRelation.addToIndexes(aJCas);

        return eventRelation;
    }
}
