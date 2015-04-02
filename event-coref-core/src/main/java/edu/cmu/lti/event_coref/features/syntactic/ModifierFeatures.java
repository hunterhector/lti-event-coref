package edu.cmu.lti.event_coref.features.syntactic;

import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.type.StanfordDependencyNode;
import edu.cmu.lti.event_coref.type.StanfordDependencyRelation;
import edu.cmu.lti.event_coref.utils.SimilarityCalculator;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.util.JCasUtil;

import java.util.ArrayList;
import java.util.List;

public class ModifierFeatures extends PairwiseFeatureGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ModifierFeatures.class);

    SimilarityCalculator calc;

    public ModifierFeatures(SimilarityCalculator calc) {
        this.calc = calc;
    }


    @Override
    public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
                                                     EventMention event2) {

        List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();

        // Add definiteness information
        List<StanfordDependencyNode> event1Nodes = JCasUtil.selectCovered(StanfordDependencyNode.class,
                event1);
        List<StanfordDependencyNode> event2Nodes = JCasUtil.selectCovered(StanfordDependencyNode.class,
                event2);

        Boolean e1Definite = false;
        Boolean e2Definite = false;
        for (StanfordDependencyNode e1Node : event1Nodes) {
            FSList e1NodeChildDepFSList = e1Node.getChildRelations();
            if (e1NodeChildDepFSList != null) {
                for (StanfordDependencyRelation e1NodeChildDep : FSCollectionFactory.create(
                        e1NodeChildDepFSList, StanfordDependencyRelation.class)) {
                    if (e1NodeChildDep.getRelationType().equals("det"))
                        e1Definite = true;
                }
            }
        }

        for (StanfordDependencyNode e2Node : event2Nodes) {
            FSList e2NodeChildDepFSList = e2Node.getChildRelations();
            if (e2NodeChildDepFSList != null) {
                for (StanfordDependencyRelation e2NodeChildDep : FSCollectionFactory.create(
                        e2NodeChildDepFSList, StanfordDependencyRelation.class)) {
                    if (e2NodeChildDep.getRelationType().equals("det"))
                        e2Definite = true;
                }
            }
        }

        PairwiseEventFeature latterIsDefinite = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                "latterEventIsDefinite", e2Definite, false);

        features.add(latterIsDefinite);

        // Add modifying information
        Boolean e1LooseMod = false;
        Boolean e2LooseMod = false;
        String e1LooseModStr = null;
        String e2LooseModStr = null;

        String e1LastChildNodeStr = null;
        String e1ChildNodeStr = null;

        Boolean e1StrictMod = false;
        Boolean e2StrictMod = false;
        String e1StrictModStr = null;
        String e2StrictModStr = null;

        String e2lastChildNodeStr = null;
        String e2ChildNodeStr = null;

        Boolean e1Neg = false;
        Boolean e2Neg = false;

        for (StanfordDependencyNode e1Node : event1Nodes) {
            FSList e1NodeChildDepFSList = e1Node.getChildRelations();
            if (e1NodeChildDepFSList != null) {
                for (StanfordDependencyRelation e1NodeChildDep : FSCollectionFactory.create(
                        e1NodeChildDepFSList, StanfordDependencyRelation.class)) {
                    String e1Relation = e1NodeChildDep.getRelationType();

                    if (e1Relation.endsWith("mod")) {
                        e1LooseMod = true;
                        e1LooseModStr = e1NodeChildDep.getChild().getCoveredText();
                    }

                    e1LastChildNodeStr = e1NodeChildDep.getChild().getCoveredText();

                    if (e1ChildNodeStr != null) {
                        e1ChildNodeStr = e1LastChildNodeStr;
                    } else {
                        e1ChildNodeStr += " " + e1LastChildNodeStr;
                    }

                    if (e1Relation.equals("amod") || e1Relation.equals("nn")) {
                        e1StrictMod = true;
                        e1StrictModStr = e1NodeChildDep.getChild().getCoveredText();
                    }

                    if (e1Relation.equals("neg")) {
                        e1Neg = true;
                    }
                }
            }
        }

        for (StanfordDependencyNode e2Node : event2Nodes) {
            FSList e2NodeChildDepFSList = e2Node.getChildRelations();
            if (e2NodeChildDepFSList != null) {
                for (StanfordDependencyRelation e2NodeChildDep : FSCollectionFactory.create(
                        e2NodeChildDepFSList, StanfordDependencyRelation.class)) {
                    String e2Relation = e2NodeChildDep.getRelationType();

                    if (e2Relation.endsWith("mod")) {
                        e2LooseMod = true;
                        e2LooseModStr = e2NodeChildDep.getChild().getCoveredText();
                    }

                    e2lastChildNodeStr = e2NodeChildDep.getChild().getCoveredText();

                    if (e1ChildNodeStr != null) {
                        e2ChildNodeStr = e2lastChildNodeStr;
                    } else {
                        e2ChildNodeStr += " " + e2lastChildNodeStr;
                    }

                    if (e2Relation.equals("amod") || e2Relation.equals("nn")) {
                        e2StrictMod = true;
                        e2StrictModStr = e2NodeChildDep.getChild().getCoveredText();
                    }
                    if (e2Relation.equals("neg")) {
                        e2Neg = true;
                    }
                }
            }
        }

        if (e1Neg && e2Neg) {
            PairwiseEventFeature bothNegation = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                    "bothNegation", true, true);
            features.add(bothNegation);
            logger.debug("Both negation" + event1.getCoveredText() + " " + event2.getCoveredText());
        }

        features.add(FeatureUtils.createPairwiseEventBinaryFeature(aJCas, "latterIsLooseModified",
                e2LooseMod, false));

        features.add(FeatureUtils.createPairwiseEventBinaryFeature(aJCas, "latterIsStrictlyModified",
                e2StrictMod, false));

        if (e1LastChildNodeStr != null && e2lastChildNodeStr != null) {
            features.add(FeatureUtils.createPairwiseEventNumericFeature(aJCas, "lastChildNodeDice",
                    calc.relaxedDiceTest(e1LastChildNodeStr, e2lastChildNodeStr), true));
        }

        if (e1ChildNodeStr != null && e2ChildNodeStr != null) {
            features.add(FeatureUtils.createPairwiseEventNumericFeature(aJCas, "childNodeDice",
                    calc.relaxedDiceTest(e1ChildNodeStr, e2ChildNodeStr), true));
        }

        return features;
    }
}
