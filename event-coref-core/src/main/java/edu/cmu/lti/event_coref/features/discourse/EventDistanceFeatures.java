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

public class EventDistanceFeatures extends PairwiseFeatureGenerator {
  EventMentionRow[] allRows;

  Map<EventMention, EventMentionRow> domainTable;

  EventMentionTable sTable;

  public EventDistanceFeatures(EventMentionTable sTable) {
    allRows = sTable.getRowView();
    this.domainTable = sTable.getDomainOnlyTableView();
    this.sTable = sTable;
  }

  @Override
  public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
          EventMention event2) {
    List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();

    EventMentionRow row1 = domainTable.get(event1);
    EventMentionRow row2 = domainTable.get(event2);

    double domainEventInBetweenCount = countEventMentionsInBetween(allRows, row1.getId(),
            row2.getId());
    PairwiseEventFeature eventInBetweenByTotalEvent = FeatureUtils
            .createPairwiseEventNumericFeature(aJCas, "eventInBetweenByTotalEvent",
                    domainEventInBetweenCount / sTable.getNumOfNonEpistemicdDomainEvents(), false);

    features.add(eventInBetweenByTotalEvent);

    return features;
  }

  private int countEventMentionsInBetween(EventMentionRow[] allRows, int first, int second) {
    if (second > allRows.length) {
      return 0;
    }

    int count = 0;
    for (int i = first + 1; i < second; i++) {
      if (allRows[i].getEventMention().getEventType().equals("event")) {
        count++;
      }
    }
    return count;
  }
}
