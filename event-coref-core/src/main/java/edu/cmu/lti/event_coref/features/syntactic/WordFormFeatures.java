package edu.cmu.lti.event_coref.features.syntactic;

import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.type.StanfordCorenlpToken;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import edu.cmu.lti.utils.general.EnglishUtils;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.util.JCasUtil;

import java.util.ArrayList;
import java.util.List;

public class WordFormFeatures extends PairwiseFeatureGenerator {
    private static final Logger logger = LoggerFactory.getLogger(WordFormFeatures.class);

    public WordFormFeatures() {
    }

    @Override
    public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
                                                     EventMention event2) {

        List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();

        // System.out.println(event1.getCoveredText() + " " + event1.getBegin() + " " +
        // event1.getEnd());
        // System.out.println(event2.getCoveredText()+ "  "+ event2.getBegin() + " " + event2.
        // getEnd());

        // Word event1HeadWord = event1.getHeadWord();
        // Word event2HeadWord = event2.getHeadWord();

        StanfordCorenlpToken event1HeadWord = JCasUtil.selectCovered(StanfordCorenlpToken.class,
                event1.getHeadWord()).get(0);
        StanfordCorenlpToken event2HeadWord = JCasUtil.selectCovered(StanfordCorenlpToken.class,
                event2.getHeadWord()).get(0);

        // Add plural features
        Boolean e1NounPlural = EnglishUtils.isPluralNoun(event1HeadWord.getPos());
        Boolean e1NounSingle = EnglishUtils.isSingularNoun(event1HeadWord.getPos());

        Boolean e2NounPlural = EnglishUtils.isPluralNoun(event2HeadWord.getPos());
        Boolean e2NounSingle = EnglishUtils.isSingularNoun(event2HeadWord.getPos());

        boolean areBothPlural = e1NounPlural && e2NounPlural;
        boolean areBothSingle = e1NounSingle && e2NounSingle;
        boolean areDifferentPlurality = (e1NounPlural && e2NounSingle)
                || (e1NounSingle && e2NounPlural);

        PairwiseEventFeature bothPlural;
        String bothPluralName = "bothPlural";

        PairwiseEventFeature differentPlurality;
        String differerntPluralityName = "differentPlurality";

        PairwiseEventFeature bothSingle;
        String bothSingleName = "bothSingle";

        if (areBothPlural) {
            bothPlural = FeatureUtils
                    .createPairwiseEventBinaryFeature(aJCas, bothPluralName, true, false);
            differentPlurality = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                    differerntPluralityName, false, false);
            bothSingle = FeatureUtils.createPairwiseEventBinaryFeature(aJCas, bothSingleName, false,
                    false);
            features.add(bothPlural);
            features.add(bothSingle);
            features.add(differentPlurality);
        } else if (areDifferentPlurality) {
            bothPlural = FeatureUtils.createPairwiseEventBinaryFeature(aJCas, bothPluralName, false,
                    false);
            differentPlurality = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                    differerntPluralityName, true, false);
            bothSingle = FeatureUtils.createPairwiseEventBinaryFeature(aJCas, bothSingleName, false,
                    false);
            features.add(bothPlural);
            features.add(bothSingle);
            features.add(differentPlurality);
        } else if (areBothSingle) {
            bothPlural = FeatureUtils.createPairwiseEventBinaryFeature(aJCas, bothPluralName, false,
                    false);
            differentPlurality = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                    differerntPluralityName, false, false);
            bothSingle = FeatureUtils
                    .createPairwiseEventBinaryFeature(aJCas, bothSingleName, true, false);
            features.add(bothPlural);
            features.add(bothSingle);
            features.add(differentPlurality);
        }

        // Add verb tense information
        Boolean e1Past = EnglishUtils.isPastVerb(event1HeadWord.getPos());
        Boolean e2Past = EnglishUtils.isPastVerb(event2HeadWord.getPos());
        Boolean e13rdPerson = false;
        Boolean e23rdPerson = false;
        Boolean e1Non3rdPersonPresent = false;
        Boolean e2Non3rdPersonPresent = false;

        if (event1HeadWord.getPos().equals("VBZ"))
            e13rdPerson = true;
        if (event1HeadWord.getPos().equals("VBP") || event1HeadWord.getPos().equals("VB"))
            e1Non3rdPersonPresent = true;

        if (event2HeadWord.getPos().equals("VBZ"))
            e23rdPerson = true;
        if (event2HeadWord.getPos().equals("VBP") || event2HeadWord.getPos().equals("VB"))
            e2Non3rdPersonPresent = true;

        PairwiseEventFeature one3rdPerson;
        if (e13rdPerson && e2Non3rdPersonPresent || e1Non3rdPersonPresent && e23rdPerson) {
            one3rdPerson = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                    "bothPresentDifferent3rdPerson", true, false);
        } else {
            one3rdPerson = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                    "bothPresentDifferent3rdPerson", false, false);
        }

        Boolean e1Present = e13rdPerson || e1Non3rdPersonPresent;
        Boolean e2Present = e23rdPerson || e2Non3rdPersonPresent;
        PairwiseEventFeature differentTense;
        if ((e1Past && e2Present) || (e1Present && e2Past)) {
            differentTense = FeatureUtils.createPairwiseEventBinaryFeature(aJCas, "differentTense", true,
                    false);
        } else {
            differentTense = FeatureUtils.createPairwiseEventBinaryFeature(aJCas, "differentTense",
                    false, false);
        }

        PairwiseEventFeature bothPresentTense;
        if (e1Non3rdPersonPresent && e2Non3rdPersonPresent || e13rdPerson && e23rdPerson) {
            bothPresentTense = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                    "bothPresentTenseSame3rdPerson", true, false);
        } else {
            bothPresentTense = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
                    "bothPresentTenseSame3rdPerson", false, false);
        }

        features.add(one3rdPerson);
        features.add(differentTense);
        features.add(bothPresentTense);
        return features;
    }
}