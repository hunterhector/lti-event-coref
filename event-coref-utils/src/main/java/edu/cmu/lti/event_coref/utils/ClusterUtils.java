/**
 * 
 */
package edu.cmu.lti.event_coref.utils;

import edu.cmu.lti.event_coref.model.EventCorefConstants;
import edu.cmu.lti.event_coref.type.EntityCoreferenceCluster;
import edu.cmu.lti.event_coref.type.EntityMention;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Constructing clusters and reading clusters take many repetitive steps, thus a factory is created
 * 
 * @author Zhengzhong Liu, Hector
 * 
 */
public class ClusterUtils {
  public static EntityCoreferenceCluster getEntityFullClusterSystem(EntityMention mention) {
    FSList clusterFS = mention.getEntityCoreferenceClusters();
    return getEntityFullCluster(clusterFS);
  }

  private static EntityCoreferenceCluster getEntityFullCluster(FSList clusterFS) {
    if (clusterFS != null) {
      Collection<EntityCoreferenceCluster> entityMentions = FSCollectionFactory.create(clusterFS,
              EntityCoreferenceCluster.class);
      for (EntityCoreferenceCluster entityMentionCluster : entityMentions) {
        // if (entityMentionCluster.getClusterType().equals(
        // EventCorefConstants.ENTITY_FULL_COREFERENCE_TYPE)) {
        return entityMentionCluster;
        // }
      }
    }
    return null;
  }

  public static void setEntityFullClusterSystem(JCas aJCas, EntityMention mention,
          EntityCoreferenceCluster cluster) {
    cluster.setClusterType(EventCorefConstants.ENTITY_FULL_COREFERENCE_TYPE);

    FSList oldClusterFS = mention.getEntityCoreferenceClusters();
    List<EntityCoreferenceCluster> newClusters = new ArrayList<EntityCoreferenceCluster>();
    if (oldClusterFS != null) {
      for (EntityCoreferenceCluster oldCluster : FSCollectionFactory.create(oldClusterFS,
              EntityCoreferenceCluster.class)) {
        if (!oldCluster.getClusterType().equals(EventCorefConstants.ENTITY_FULL_COREFERENCE_TYPE)) {
          newClusters.add(oldCluster);
        }
      }
    }
    newClusters.add(cluster);

    mention.setEntityCoreferenceClusters(FSCollectionFactory.createFSList(aJCas, newClusters));
  }
}
