package edu.cmu.lti.event_coref.features.discourse;

import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.model.EventMentionRow;
import edu.cmu.lti.event_coref.model.EventMentionTable;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TitleFeatures extends PairwiseFeatureGenerator {

  Map<EventMention, EventMentionRow> domainTable;

  public TitleFeatures(EventMentionTable sTable) {
    domainTable = sTable.getDomainOnlyTableView();
  }

  @Override
  public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
          EventMention event2) {
    List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();

    EventMentionRow row1 = domainTable.get(event1);
    EventMentionRow row2 = domainTable.get(event2);

    // System.out.println(event1.getCoveredText() + " " + event1.getBegin() + " " +
    // event1.getEnd());
    // System.out.println(event2.getCoveredText() + " " + event2.getBegin() + " " +
    // event2.getEnd());

    // first one is title
    PairwiseEventFeature firstOneIsTitle = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
            "firstOneIsTitle", row1.isTitle(), false);
    features.add(firstOneIsTitle);

    // second one is the first non-title
    PairwiseEventFeature latterIsFirstSentence = FeatureUtils.createPairwiseEventBinaryFeature(
            aJCas, "latterIsFirstSentence", row2.getSentenceId() == 2, false);

    features.add(latterIsFirstSentence);

    return features;
  }
}
