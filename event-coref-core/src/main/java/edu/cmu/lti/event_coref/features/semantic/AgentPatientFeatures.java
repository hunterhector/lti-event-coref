package edu.cmu.lti.event_coref.features.semantic;

import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.model.EventMentionRow;
import edu.cmu.lti.event_coref.model.EventMentionTable;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import edu.cmu.lti.event_coref.utils.QuantityUtils;
import edu.cmu.lti.event_coref.utils.SimilarityCalculator;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Zhengzhong Liu, Hector
 */
public class AgentPatientFeatures extends PairwiseFeatureGenerator {
    Map<EventMention, EventMentionRow> domainTable;

    SimilarityCalculator calc;

    Set<String> lowConfidentAnnotatorNames;

//  private Map<Word, Collection<com.ibm.racr.EntityMention>> wordCoveredBySire;

    String[] stopwordsRCV = {"a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has",
            "have", "he", "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "will",
            "with", "which"};

    Set<String> minimalStopWord = new HashSet<String>(Arrays.asList(stopwordsRCV));

    private static final Logger logger = LoggerFactory.getLogger(AgentPatientFeatures.class);

    public AgentPatientFeatures(JCas aJCas, EventMentionTable sTable, SimilarityCalculator calc,
                                Set<String> lowConfidentAnnotatorNames) {
        domainTable = sTable.getDomainOnlyTableView();
        this.calc = calc;
        this.lowConfidentAnnotatorNames = lowConfidentAnnotatorNames;

//    wordCoveredBySire = JCasUtil.indexCovering(aJCas, Word.class, com.ibm.racr.EntityMention.class);

    }

    @Override
    public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
                                                     EventMention event2) {
        List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();

        features.addAll(createAgentFeatures(aJCas, domainTable.get(event1), domainTable.get(event2),
                lowConfidentAnnotatorNames));

        features.addAll(createPatientFeatures(aJCas, domainTable.get(event1), domainTable.get(event2),
                lowConfidentAnnotatorNames));

        return features;
    }

    public List<PairwiseEventFeature> createAgentFeatures(JCas aJCas, EventMentionRow row1, EventMentionRow row2,
                                                          Set<String> lowConfidentAnnotatorNames) {
        List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();

        List<EntityBasedComponent> agents1 = row1.getAgents();
        List<EntityBasedComponent> agents2 = row2.getAgents();

        if (agents1.size() > 0 && agents2.size() > 0) {
            EntityBasedComponent agent1 = agents1.get(0);
            EntityBasedComponent agent2 = agents2.get(0);

            PairwiseEventFeature bothHasAgent = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                    "bothAnnotatedWithAgent", true, true);
            features.add(bothHasAgent);

            for (Entry<String, Double> scoreEntry : checkComponentSimilarity(agent1, agent2).entrySet()) {

                String scoreName = scoreEntry.getKey();
                Double score = scoreEntry.getValue();

                if (scoreName.equals("surfaceDice")) {
                    String featureName = "agentSurfaceDice";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("surfaceRelaxedDice")) {
                    String featureName = "agentSurfaceRelaxedDice";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("maxEntityStringSim")) {
                    String featureName = "agentMaxEntityStringSimilarity";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("maxEntityClusterTypeMatch")) {
                    String featureName = "agentMaxEntityClusterTypeMatch";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("wordnetSim")) {
                    String featureName = "agentWordnetSim";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("headWordNetSim")) {
                    String featureName = "agentHeadWordnetSim";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("wordOverlap")) {
                    String featureName = "agentOverlapSimilarity";

                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                // some binary features
                if (scoreName.equals("exactSurfaceMath")) {
                    String featureName = "agentExactSurfaceMatch";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                            featureName, score == 1.0, false);
                    features.add(aFeature);
                }
                if (scoreName.equals("entityCoref")) {
                    String featureName = "agentCoref";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                            featureName, score == 1.0, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("substring")) {
                    String featureName = "agentSubString";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                            featureName, score == 1.0, false);
                    features.add(aFeature);
                }

            }

            Double numberMatchResult = null;
            try {
                numberMatchResult = QuantityUtils.numberCompare(agent1.getQuantity(),
                        agent2.getQuantity());
            } catch (Exception e) {
                logger.info(e.getMessage());
            }

            if (numberMatchResult != null) {
                PairwiseEventFeature sameQuantityFeature = FeatureUtils.createPairwiseEventBinaryFeature(
                        aJCas, "QuantityFeature_agentSameQuantity", numberMatchResult == 0.0, false);

                PairwiseEventFeature firstSmallerFeature = FeatureUtils.createPairwiseEventBinaryFeature(
                        aJCas, "QuantityFeature_firstAgentSmaller", numberMatchResult < 0, false);

                PairwiseEventFeature secondSmallerFeature = FeatureUtils.createPairwiseEventBinaryFeature(
                        aJCas, "QuantityFeature_secondAgentSmaller", numberMatchResult > 0, false);

                features.add(sameQuantityFeature);
                features.add(firstSmallerFeature);
                features.add(secondSmallerFeature);
            }
        }
        return features;
    }

    public List<PairwiseEventFeature> createPatientFeatures(JCas aJCas, EventMentionRow row1,
                                                            EventMentionRow row2, Set<String> lowConfidentAnnotatorNames) {
        List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();

        List<EntityBasedComponent> patients1 = row1.getAgents();
        List<EntityBasedComponent> patients2 = row2.getAgents();

        if (patients1.size() == 1 && patients2.size() == 1) {
            EntityBasedComponent patient1 = patients1.get(0);
            EntityBasedComponent patient2 = patients2.get(0);

            PairwiseEventFeature bothHasPatient = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                    "bothAnnotatedWithPatient", true, true);
            features.add(bothHasPatient);

            for (Entry<String, Double> scoreEntry : checkComponentSimilarity(patient1, patient2)
                    .entrySet()) {
                String scoreName = scoreEntry.getKey();
                Double score = scoreEntry.getValue();

                if (scoreName.equals("surfaceDice")) {
                    String featureName = "patientSurfaceDice";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("surfaceRelaxedDice")) {
                    String featureName = "patientSurfaceRelaxedDice";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("maxEntityStringSim")) {
                    String featureName = "patientMaxEntityStringSimilarity";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("maxEntityClusterTypeMatch")) {
                    String featureName = "patientMaxEntityClusterTypeMatch";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("wordnetSim")) {
                    String featureName = "patientWordnetSim";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("headWordNetSim")) {
                    String featureName = "patientHeadWordnetSim";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("wordOverlap")) {
                    String featureName = "patientOverlapSimilarity";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventNumericFeature(aJCas,
                            featureName, score, false);
                    features.add(aFeature);
                }

                // some binary features
                if (scoreName.equals("exactSurfaceMath")) {
                    String featureName = "patientExactSurfaceMatch";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                            featureName, score == 1.0, false);
                    features.add(aFeature);
                }
                if (scoreName.equals("entityCoref")) {
                    String featureName = "patientCoref";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                            featureName, score == 1.0, false);
                    features.add(aFeature);
                }

                if (scoreName.equals("substring")) {
                    String featureName = "patientSubString";
                    PairwiseEventFeature aFeature = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                            featureName, score == 1.0, false);
                    features.add(aFeature);
                }

            }

            Double numberMatchResult = null;
            try {
                numberMatchResult = QuantityUtils.numberCompare(patient1.getQuantity(),
                        patient2.getQuantity());
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
            if (numberMatchResult != null) {
                PairwiseEventFeature sameQuantityFeature = FeatureUtils.createPairwiseEventBinaryFeature(
                        aJCas, "QuantityFeature_patientSameQuantity", numberMatchResult == 0.0, false);

                PairwiseEventFeature firstSmallerFeature = FeatureUtils.createPairwiseEventBinaryFeature(
                        aJCas, "QuantityFeature_firstPatientSmaller", numberMatchResult < 0, false);

                PairwiseEventFeature secondSmallerFeature = FeatureUtils.createPairwiseEventBinaryFeature(
                        aJCas, "QuantityFeature_secondPatientSmaller", numberMatchResult > 0, false);

                features.add(sameQuantityFeature);
                features.add(firstSmallerFeature);
                features.add(secondSmallerFeature);
            }
        }
        return features;
    }

    private Map<String, Double> checkComponentSimilarity(EntityBasedComponent anno1,
                                                         EntityBasedComponent anno2) {
        Map<String, Double> scoreMap = new HashMap<String, Double>();
        String str1 = anno1.getCoveredText();
        String str2 = anno2.getCoveredText();

        if (str1.equals(str2))
            scoreMap.put("exactSurfaceMath", 1.0);
        else
            scoreMap.put("exactSurfaceMath", 0.0);

        scoreMap.put("surfaceDice", calc.getDiceCoefficient(str1, str2));

        scoreMap.put("surfaceRelaxedDice", calc.relaxedDiceTest(str1, str2));

        Collection<EntityMention> anno1Entities = FSCollectionFactory.create(
                anno1.getContainingEntityMentions(), EntityMention.class);

        Collection<EntityMention> anno2Entities = FSCollectionFactory.create(
                anno2.getContainingEntityMentions(), EntityMention.class);

        double maxEntitySim = 0.0;
//        double maxSireEntityTypeMatch = 0.0;
        boolean isCoref = false;
        for (EntityMention em1 : anno1Entities) {
            for (EntityMention em2 : anno2Entities) {
                double entityScore = calc.getClosestInterClusterStringSimilarity(em1, em2);
//                double sireMentionTypeMatch = getMentionTypeMatch(em1, em2);
                if (entityScore > maxEntitySim) {
                    maxEntitySim = entityScore;
                }
//                if (sireMentionTypeMatch > maxSireEntityTypeMatch) {
//                    maxSireEntityTypeMatch = sireMentionTypeMatch;
//                }

                if (calc.checkEntityCorference(em1, em2)) {
                    isCoref = true;
                }
            }
        }

        scoreMap.put("maxEntityInterClusterStringSim", maxEntitySim);

//        scoreMap.put("maxEntityClusterTypeMatch", maxSireEntityTypeMatch);

        if (isCoref) {
            scoreMap.put("entityCoref", 1.0);
        } else {
            scoreMap.put("entityCoref", 0.0);
        }

        double wordNetSim = calc.getLemmaWordNetSimilarity(JCasUtil.selectCovered(Word.class, anno1),
                JCasUtil.selectCovered(Word.class, anno2));
        if (wordNetSim > 1.0)
            scoreMap.put("wordnetSim", 1.0);
        else
            scoreMap.put("wordnetSim", wordNetSim);

        Word headWord1 = anno1.getHeadWord();
        Word headWord2 = anno2.getHeadWord();

        if (headWord1 != null && headWord2 != null) { // location doesn't have head word
            double headWordNetSim = calc.getLemmaWordNetSimilarity(headWord1, headWord2);

            if (headWordNetSim > 1.0)
                scoreMap.put("headWordNetSim", 1.0);
            else
                scoreMap.put("headWordNetSim", headWordNetSim);

        } else {
            scoreMap.put("headWordNetSim", 0.0);
        }

        if (calc.subStringTest(str1, str2)) {
            scoreMap.put("substring", 1.0);
        } else {
            scoreMap.put("substring", 0.0);
        }

        Set<String> wordBag1 = new HashSet<String>();
        for (Word word : JCasUtil.selectCovered(Word.class, anno1)) {
            String lemma = word.getLemma().toLowerCase();
            if (!minimalStopWord.contains(lemma)) {
                wordBag1.add(lemma);
            }
        }

        Set<String> wordBag2 = new HashSet<String>();
        for (Word word : JCasUtil.selectCovered(Word.class, anno2)) {
            String lemma = word.getLemma().toLowerCase();
            if (!minimalStopWord.contains(lemma)) {
                wordBag2.add(lemma);
            }
        }

        int overlapCount = 0;
        for (String word : wordBag1) {
            if (wordBag2.contains(word)) {
                overlapCount++;
            }
        }

        double overlapSim = 0;
        if (Math.min(wordBag1.size(), wordBag2.size()) != 0) {
            // Avoids the undefined value (divided by 0);
            overlapSim = overlapCount * 1.0 / Math.min(wordBag1.size(), wordBag2.size());
        }

        scoreMap.put("wordOverlap", overlapSim);

        return scoreMap;
    }

//    private double getMentionTypeMatch(EntityMention em1, EntityMention em2) {
//        EntityCoreferenceCluster cluster1 = ClusterUtils.getEntityFullClusterSystem(em1);
//        EntityCoreferenceCluster cluster2 = ClusterUtils.getEntityFullClusterSystem(em2);
//
//        List<EntityMention> em1Alternatives = new LinkedList<EntityMention>();
//        List<EntityMention> em2Alternatives = new LinkedList<EntityMention>();
//
//        if (cluster1 != null) {
//            for (EntityMention emFromCluster1 : FSCollectionFactory.create(cluster1.getEntityMentions(),
//                    EntityMention.class)) {
//                em1Alternatives.add(emFromCluster1);
//            }
//        }
//
//        if (cluster2 != null) {
//            for (EntityMention emFromCluster2 : FSCollectionFactory.create(cluster2.getEntityMentions(),
//                    EntityMention.class)) {
//                em2Alternatives.add(emFromCluster2);
//            }
//        }
//
//        double maxSireConfidence = 0;
//
//        for (EntityMention em1CoreferredEntity : em1Alternatives) {
//            for (EntityMention em2CoreferredEntity : em2Alternatives) {
//                Word em1CoreferredEntityHead = FanseDependencyUtils
//                        .findHeadWordFromDependency(em1CoreferredEntity);
//                Word em2CoreferredEntityHead = FanseDependencyUtils
//                        .findHeadWordFromDependency(em2CoreferredEntity);
//
////                for (com.ibm.racr.EntityMention sireEntity1 : wordCoveredBySire
////                        .get(em1CoreferredEntityHead)) {
////                    for (com.ibm.racr.EntityMention sireEntity2 : wordCoveredBySire
////                            .get(em2CoreferredEntityHead)) {
////                        if (sireEntity1.getMentionType().equals(sireEntity2.getMentionType())) {
////                            double conf = sireEntity1.getMentionTypeConf() * sireEntity2.getMentionTypeConf();
////                            if (conf > maxSireConfidence) {
////                                maxSireConfidence = conf;
////                            }
////                        }
////                    }
////                }
//            }
//        }
//
//        return maxSireConfidence;
//    }
}
