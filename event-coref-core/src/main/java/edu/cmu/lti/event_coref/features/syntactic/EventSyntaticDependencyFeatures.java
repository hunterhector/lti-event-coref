package edu.cmu.lti.event_coref.features.syntactic;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.EventRelation;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EventSyntaticDependencyFeatures extends PairwiseFeatureGenerator {
  Table<EventMention, EventMention, String> eventRelationMap;

  // String[] interestingRelationTypesArr = { "appos" };
  //
  // Set<String> interestingRelationTypes = new HashSet<String>(
  // Arrays.asList(interestingRelationTypesArr));

  public EventSyntaticDependencyFeatures(JCas aJCas) {
    // Prepare event relations
    eventRelationMap = HashBasedTable.create();
    Collection<EventRelation> eventRelations = JCasUtil.select(aJCas, EventRelation.class);
    for (EventRelation eventRelation : eventRelations) {
      EventMention headEvent = eventRelation.getHead();
      EventMention childEvent = eventRelation.getChild();
      eventRelationMap.put(headEvent, childEvent, eventRelation.getRelationType());
    }
  }

  @Override
  public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
          EventMention event2) {
    List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();

    // Add direct dependency information
    if (eventRelationMap.contains(event1, event2)) {
      String relationType = eventRelationMap.get(event1, event2);
      // if (interestingRelationTypes.contains(relationType)) {
      PairwiseEventFeature relationFeature = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
              "eventRelation_" + relationType, true, true);
      features.add(relationFeature);

      // System.out.println("appos triggered " + event1.getCoveredText() + " "
      // + event2.getCoveredText());
      // }
    } else if (eventRelationMap.contains(event2, event1)) {
      String relationType = eventRelationMap.get(event2, event1);
      // if (interestingRelationTypes.contains(relationType)) {
      PairwiseEventFeature relationFeature = FeatureUtils.createPairwiseEventBinaryFeature(aJCas,
              "eventRelation_" + relationType, true, true);
      features.add(relationFeature);
      // }
    }

    return features;
  }
}
