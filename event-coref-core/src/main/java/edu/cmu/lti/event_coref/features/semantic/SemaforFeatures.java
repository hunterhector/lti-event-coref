/**
 *
 */
package edu.cmu.lti.event_coref.features.semantic;

import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * The prerequisites to this is FrameBasedEventArgumentExtractor in IntegeratedSemanticRoleAnnotator project
 *
 * @author Zhengzhong Liu, Hector
 */
public class SemaforFeatures extends PairwiseFeatureGenerator {

    // private final String SEMAFOR_TARGET = "Target";
    //
    // private Map<SemaforLabel, String> label2FrameName = new HashMap<SemaforLabel, String>();
    //
    // private Map<EventMention, SemaforLabel> event2Label = new HashMap<EventMention,
    // SemaforLabel>();

    /**
     * Initialize this feature generator, by adding semantic information
     */
    public SemaforFeatures() {
//    // first create a mapping from semafor label to the frame name it trigger
//    Collection<SemaforAnnotationSet> semaforAnnotationSets = JCasUtil.select(aJCas,
//            SemaforAnnotationSet.class);
//    for (SemaforAnnotationSet sas : semaforAnnotationSets) {
//      FSArray layers = sas.getLayers();
//      if (layers != null) {
//
//        for (SemaforLayer layer : FSCollectionFactory.create(layers, SemaforLayer.class)) {
//          if (layer.getName().equals(SEMAFOR_TARGET)) {
//            String frameName = sas.getFrameName();
//            FSArray labels = layer.getLabels();
//            for (SemaforLabel label : FSCollectionFactory.create(labels, SemaforLabel.class)) {
//              if (label.getName().equals(SEMAFOR_TARGET)) {
//                label2FrameName.put(label, frameName);
//              }
//            }
//          }
//        }
//      }
//    }
//
//    // then create a mapping from the event to semafor labels
//    for (EventMention evm : JCasUtil.select(aJCas, EventMention.class)) {
//      for (SemaforLabel coveredLabel : JCasUtil.selectCovered(SemaforLabel.class, evm)) {
//        event2Label.put(evm, coveredLabel);
//      }
//    }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * edu.cmu.lti.event_coref.ml.feature.PairwiseFeatureGenerator#createFeatures(org.apache.uima.
     * jcas.JCas, edu.cmu.lti.event_coref.type.EventMention,
     * edu.cmu.lti.event_coref.type.EventMention)
     */
    @Override
    public List<PairwiseEventFeature> createFeatures(JCas aJCas, EventMention event1,
                                                     EventMention event2) {
        List<PairwiseEventFeature> features = new ArrayList<PairwiseEventFeature>();
        String featureName = "TriggerSameFrame";

//    SemaforLabel label1 = event2Label.get(event1);
//    String frameName1 = null;
//    String frameName2 = null;
//    if (label1 != null) {
//      frameName1 = label2FrameName.get(label1);
//    }
//
//    SemaforLabel label2 = event2Label.get(event2);
//    if (label2 != null) {
//      frameName2 = label2FrameName.get(label2);
//    }

        String frameName1 = event1.getFrameName();
        String frameName2 = event2.getFrameName();

        if (frameName1 != null && frameName2 != null) {
            if (frameName1.equals(frameName2)) {
                features.add(FeatureUtils.createPairwiseEventBinaryFeature(aJCas, featureName, true, false));
            } else {
                features.add(FeatureUtils.createPairwiseEventBinaryFeature(aJCas, featureName, false, false));
            }
        }
        return features;
    }
}
