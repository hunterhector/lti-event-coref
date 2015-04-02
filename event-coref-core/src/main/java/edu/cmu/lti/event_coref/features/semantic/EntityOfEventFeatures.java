/**
 *
 */
package edu.cmu.lti.event_coref.features.semantic;

import edu.cmu.lti.event_coref.features.PairwiseFeatureGenerator;
import edu.cmu.lti.event_coref.type.EntityCoreferenceCluster;
import edu.cmu.lti.event_coref.type.EntityMention;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.utils.ClusterUtils;
import edu.cmu.lti.event_coref.utils.ml.FeatureUtils;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.util.JCasUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zhengzhong Liu, Hector
 */
public class EntityOfEventFeatures extends PairwiseFeatureGenerator {
    private static final Logger logger = LoggerFactory.getLogger(EntityOfEventFeatures.class);

    Map<EventMention, EntityMention> event2EntityMapping = new HashMap<EventMention, EntityMention>();

    public EntityOfEventFeatures(JCas aJCas) {
        for (EntityMention em : JCasUtil.select(aJCas, EntityMention.class)) {
            List<EventMention> eventsCovered = JCasUtil.selectCovered(EventMention.class, em);
            if (!eventsCovered.isEmpty()) {
                event2EntityMapping.put(eventsCovered.get(0), em);
            }
        }

        for (EventMention evm : JCasUtil.select(aJCas, EventMention.class)) {
            List<EntityMention> entitiesCovered = JCasUtil.selectCovered(EntityMention.class, evm);
            if (!entitiesCovered.isEmpty()) {
                event2EntityMapping.put(evm, entitiesCovered.get(0));
            }
        }

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
        EntityMention entity1 = event2EntityMapping.get(event1);
        EntityMention entity2 = event2EntityMapping.get(event2);

        if (entity1 != null && entity2 != null) {
            EntityCoreferenceCluster cluster1 = ClusterUtils.getEntityFullClusterSystem(entity1);
            EntityCoreferenceCluster cluster2 = ClusterUtils.getEntityFullClusterSystem(entity2);

            if (cluster1 != null && cluster2 != null) {
                boolean clusterCorefer = false;
                if (cluster1.equals(cluster2)) {
                    clusterCorefer = true;
                    logger.debug("Entity corefer triggered " + event1.getCoveredText() + " " + event2.getCoveredText());
                }

                features.add(FeatureUtils.createPairwiseEventBinaryFeature(aJCas, "EntityOfEventCorefer",
                        clusterCorefer, false));
            }
        }

        return features;
    }
}
