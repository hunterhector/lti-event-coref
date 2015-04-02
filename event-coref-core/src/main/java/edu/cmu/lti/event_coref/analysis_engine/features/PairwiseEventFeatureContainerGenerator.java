package edu.cmu.lti.event_coref.analysis_engine.features;

import edu.cmu.lti.event_coref.model.EventCorefConstants;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.EventCoreferenceClusterUtils;
import edu.cmu.lti.event_coref.utils.EventCoreferenceConstants.DetailedEventCoreferenceRelationType;
import edu.cmu.lti.event_coref.utils.EventMentionUtils;
import edu.cmu.lti.event_coref.utils.EventRelationUtils;
import edu.cmu.lti.utils.general.ErrorUtils;
import edu.cmu.lti.utils.general.MapUtils;
import edu.cmu.lti.utils.uima.BaseAnalysisEngine;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Assuming that all necessary annotations are done previously, this analysis
 * engine annotates pairwise event features.
 *
 * @author Jun Araki
 */
public class PairwiseEventFeatureContainerGenerator extends BaseAnalysisEngine {
    public static final String ANNOTATOR_COMPONENT_ID = "System"
            + PairwiseEventFeatureContainerGenerator.class.getSimpleName();

    public static final String PARAM_GOLD_STANDARD_VIEWNAME = "GoldStandardViewName";

    private static final Logger logger = LoggerFactory.getLogger(PairwiseEventFeatureContainerGenerator.class);

    @ConfigurationParameter(mandatory = false, name = PARAM_GOLD_STANDARD_VIEWNAME)
    private String goldViewName;

    private Map<String, Integer> relationCounter;

    private int totalNumOfEventPairs;

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException {
        super.initialize(aContext);

        relationCounter = new LinkedHashMap<String, Integer>();
        totalNumOfEventPairs = 0;
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        UimaConvenience.printProcessLog(aJCas, logger);

        JCas goldStandardView = null;

        if (goldViewName != null) {
            try {
                goldStandardView = aJCas.getView(goldViewName);
            } catch (CASException | CASRuntimeException e) {
                logger.warn("Gold standard is not annotated");
                goldStandardView = aJCas;
            }
        }

        // currentDocId++;
        Collection<EventCoreferenceCluster> eccList = UimaConvenience.getAnnotationList(goldStandardView, EventCoreferenceCluster.class);

        List<EventCoreferenceCluster> fullClusterList = new ArrayList<EventCoreferenceCluster>();
        List<EventCoreferenceCluster> memberClusterList = new ArrayList<EventCoreferenceCluster>();
        List<EventCoreferenceCluster> subeventClusterList = new ArrayList<EventCoreferenceCluster>();

        for (EventCoreferenceCluster ecc : eccList) {
            if (EventCoreferenceClusterUtils.isGoldStandardFullCoreferenceCluster(ecc)
                    || ecc.getClusterType().equals(EventCorefConstants.FULL_COREFERENCE_TYPE)) {
                fullClusterList.add(ecc);
            } else if (EventCoreferenceClusterUtils.isGoldStandardMemberCoreferenceCluster(ecc)) {
                memberClusterList.add(ecc);
            } else if (EventCoreferenceClusterUtils.isGoldStandardSubeventCoreferenceCluster(ecc)) {
                subeventClusterList.add(ecc);
            }
        }

        logger.debug(String.format("Number of full clusters : %d. Number of member clusters : %d. Number of subevent clusters : %d",
                fullClusterList.size(), memberClusterList.size(), subeventClusterList.size()));

        // Gets only non-elliptical domain events.
        List<EventMention> nonEllipticalDomainEventMentionList;

        nonEllipticalDomainEventMentionList = EventMentionUtils.getNonImplicitDomainEvents(goldStandardView);

        logger.debug("Event mentions : " + nonEllipticalDomainEventMentionList.size());

        // In the code below, event1 precedes event2 in terms of discourse.
        int eventMentionSize = nonEllipticalDomainEventMentionList.size();
        for (int i = 0; i < eventMentionSize; i++) {
            EventMention event1 = nonEllipticalDomainEventMentionList.get(i);
            for (int j = i + 1; j < eventMentionSize; j++) {
                totalNumOfEventPairs++;
                EventMention event2 = nonEllipticalDomainEventMentionList.get(j);

                DetailedEventCoreferenceRelationType relationType =
                        EventRelationUtils.getRelation(fullClusterList, memberClusterList, subeventClusterList, event1, event2, true);

                String relationTypeStr = relationType.toString();

                MapUtils.incrementCounter(relationCounter, relationTypeStr);

                EventMention systemMention1 = findCorrespondingEventMention(aJCas, event1);
                EventMention systemMention2 = findCorrespondingEventMention(aJCas, event2);

                if (systemMention1 != null && systemMention2 != null &&
                        systemMention1.getHeadWord() != null && systemMention2.getHeadWord() != null) {
                    PairwiseEventCoreferenceEvaluation pece = new PairwiseEventCoreferenceEvaluation(aJCas);
                    pece.setEventMentionI(findCorrespondingEventMention(aJCas, event1));
                    pece.setEventMentionJ(findCorrespondingEventMention(aJCas, event2));
                    pece.setEventCoreferenceRelationGoldStandard(relationTypeStr);
                    List<PairwiseEventFeature> pairwiseEventFeatures = new ArrayList<PairwiseEventFeature>();
                    FSList pairwiseEventFeatureList = FSCollectionFactory.createFSList(aJCas, pairwiseEventFeatures);
                    pece.setPairwiseEventFeatures(pairwiseEventFeatureList);
                    pece.setComponentId(ANNOTATOR_COMPONENT_ID);
                    pece.addToIndexes();
                }
            }
        }

//        logger.info(String.format("There are %d gold standard full clusters, %d system full clusters",
//                JCasUtil.select(goldStandardView, EventCoreferenceCluster.class).size(), JCasUtil.select(aJCas, EventCoreferenceCluster.class).size()));

        logger.info(String.format("%d relations created.", JCasUtil.select(aJCas, PairwiseEventCoreferenceEvaluation.class).size()));

        DocumentStatistics annDocumentStatistics = new DocumentStatistics(aJCas);
        annDocumentStatistics.setNumberOfEventPairs(totalNumOfEventPairs);
        annDocumentStatistics.addToIndexes();
    }

    /**
     * This is the oversimplified version of finding the system event mention
     * regarding the gold standard event mention
     *
     * @param aJCas
     * @param goldMention
     * @return
     */
    private EventMention findCorrespondingEventMention(JCas aJCas,
                                                       EventMention goldMention) {
        List<EventMention> systemMentions = JCasUtil.selectCovered(aJCas,
                EventMention.class, goldMention.getBegin(),
                goldMention.getEnd());

        if (systemMentions.size() > 0) {
            return systemMentions.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException {
        for (String relation : MapUtils.getSortedKeys(relationCounter)) {
            DetailedEventCoreferenceRelationType relationType = DetailedEventCoreferenceRelationType.findConstant(relation);
            if (relationType == null) {
                ErrorUtils.terminate("Invalid relation type: " + relation);
            }
        }

        for (DetailedEventCoreferenceRelationType relationType : DetailedEventCoreferenceRelationType.values()) {
            String relationTypeStr = relationType.toString();
            int count = 0;
            if (relationCounter.containsKey(relationTypeStr)) {
                count = relationCounter.get(relationTypeStr);
            }
            logger.info("# of " + relationTypeStr + " relations: " + count);
        }
        logger.info("The total number of event pairs: " + totalNumOfEventPairs);
    }

}
