/**
 *
 */
package edu.cmu.lti.event_coref.utils.eval;

import com.google.common.collect.Iterables;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.event_coref.utils.EventMentionUtils;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.util.FSCollectionFactory;
import org.uimafit.util.JCasUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author Zhengzhong Liu, Hector
 *         <p/>
 *         Convert cluster into Conll format for simple cluster case, require annotation on both
 *         golden standard and system. Currently for Event Mentions that have start and end span
 *         less than 0 (if any) will be removed from the Golden Standard output.
 */
public class ConllFormatConverter {
    private static final Logger logger = LoggerFactory.getLogger(ConllFormatConverter.class);

    public static final String NEW_LINE = System.getProperty("line.separator");

    /**
     * Read in the JCas, assume cluster already annotated, output both golden standard and system
     * output into seperate files. This write one document at a time
     *
     * @param aJCas
     * @param articleName
     * @param systemWriter
     * @param clusterType
     * @param includeSentenceId
     * @param componentIdPrefix
     * @param includeSingleton
     * @throws IOException
     */
    public static void writeEventMentionClusters(JCas aJCas, String articleName,
                                                 BufferedWriter systemWriter, String clusterType, boolean includeSentenceId,
                                                 String componentIdPrefix, boolean includeSingleton) throws IOException {
        List<EventCoreferenceCluster> eventMentionClusters = new ArrayList<EventCoreferenceCluster>();

        for (EventCoreferenceCluster cluster : JCasUtil.select(aJCas, EventCoreferenceCluster.class)) {
            if (componentIdPrefix == null || cluster.getComponentId().startsWith(componentIdPrefix)) {
                boolean isTargetCluster = false;
                if (clusterType != null) {
                    String currentClusterType = cluster.getClusterType();
                    if (currentClusterType == null) {
                        logger.error("The cluster is not associated with a type but you requested one, I think you need to check out the set ups");
                        isTargetCluster = true;
                    } else if (currentClusterType.equals(clusterType)) {
                        isTargetCluster = true;
                    }
                } else {
                    // if user didn't specify target cluster type, all clusters all put in
                    isTargetCluster = true;
                }
                if (isTargetCluster) {
                    eventMentionClusters.add(cluster);
                }
            }
        }

        if (eventMentionClusters.size() == 0) {
            logger.info("Did not find any clusters in view");
        } else {
            logger.info(eventMentionClusters.size() + " clusters");
        }

        Set<EventMention> nonEllipticalDomainEventMentionList = new HashSet<EventMention>(
                EventMentionUtils.getNonImplicitDomainEvents(aJCas));

        Map<Word, Triplet<Integer, Boolean, Boolean>> token2SystemEventMentionClusterId = getWord2EventClusterMapping(
                eventMentionClusters, nonEllipticalDomainEventMentionList, includeSingleton);

        writeResults(systemWriter, aJCas, token2SystemEventMentionClusterId, articleName,
                includeSentenceId);
    }

    /**
     * Read in the JCas, assume cluster already annotated, output both golden standard and system
     * output into seperate files. This write one document at a time
     *
     * @param aJCas
     * @param articleName
     * @param systemWriter
     * @param includeSentenceId
     * @param componentIdPrefix
     * @param includeSingleton
     * @throws IOException
     */
    public static void writeEntityMentionClusters(JCas aJCas, String articleName,
                                                  BufferedWriter systemWriter, boolean includeSentenceId, String componentIdPrefix,
                                                  boolean includeSingleton) throws IOException {
        logger.info("Writing entity mentions for " + articleName);

        List<EntityCoreferenceCluster> entityMentionClusters = new ArrayList<EntityCoreferenceCluster>();

        for (EntityCoreferenceCluster cluster : JCasUtil.select(aJCas, EntityCoreferenceCluster.class)) {
            if (componentIdPrefix == null || cluster.getComponentId().startsWith(componentIdPrefix))
                entityMentionClusters.add(cluster);
        }

        logger.info("Entity clusters : " + entityMentionClusters.size());

        Map<Word, Triplet<Integer, Boolean, Boolean>> token2SystemEntityMentionClusterId = getWord2EntityClusterMapping(
                entityMentionClusters,
                new HashSet<EntityMention>(UimaConvenience.getAnnotationList(aJCas, EntityMention.class)), includeSingleton);

        writeResults(systemWriter, aJCas, token2SystemEntityMentionClusterId, articleName,
                includeSentenceId);
    }

    private static void writeResults(BufferedWriter writer, JCas aJCas,
                                     Map<Word, Triplet<Integer, Boolean, Boolean>> token2ClusterId, String identifier,
                                     boolean includeSentenceId) throws IOException {
        writer.write(String.format("#begin document (%s); part 000\n", identifier));

        Map<Word, Collection<Sentence>> sentencesCoveringWord = JCasUtil.indexCovering(aJCas,
                Word.class, Sentence.class);

        Collection<Sentence> sentences = JCasUtil.select(aJCas, Sentence.class);

        Word previousWord = null;

        Collection<Word> allWords = JCasUtil.select(aJCas, Word.class);

        logger.info(allWords.size() + " number of words");

        for (Word word : allWords) {
            StringBuilder corefCol = new StringBuilder();
            int wordId = Integer.parseInt(word.getWordId()) - 1;

            Collection<Sentence> sents = sentencesCoveringWord.get(word);
            Sentence sent;

            if (sents != null && !sents.isEmpty()) { // some words not covered by sent
                sent = Iterables.get(sents, 0);
            } else {
                if (previousWord != null) {
                    Collection<Sentence> previousSents = sentencesCoveringWord.get(previousWord);
                    if (previousSents != null && !previousSents.isEmpty())
                        // use the previous words sentence to replace this one
                        sent = Iterables.get(previousSents, 0);
                    else {
                        sent = Iterables.get(sentences, 0);
                    }
                } else {
                    // ok, use the first sentence then
                    sent = Iterables.get(sentences, 0);
                }
            }

            previousWord = word;

            int sentId = Integer.parseInt(sent.getSentenceId());

            String text = word.getCoveredText();

            if (text.contains(NEW_LINE)) {
                System.err
                        .println("Some word annotations have new lines in it, be careful, here the system replace the new lines with spaces");
                text = text.replace(NEW_LINE, " ");
                logger.info(text);
            }

            if (token2ClusterId.containsKey(word)) {
                Triplet<Integer, Boolean, Boolean> wordInfo = token2ClusterId.get(word);
                boolean isFirst = wordInfo.getValue1();
                boolean isLast = wordInfo.getValue2();
                int clusterId = wordInfo.getValue0();

                if (isFirst) {
                    corefCol.append("(");
                    corefCol.append(clusterId);
                }

                if (isLast) {
                    if (!isFirst)
                        corefCol.append(clusterId);
                    corefCol.append(")");
                }

                if (!isFirst && !isLast) {
                    corefCol.append("-");
                }
            } else {
                corefCol.append("-");
            }

            String wordline = includeSentenceId ? String.format("%s\t0\t%d\t%s\t%s\t%d\n", identifier,
                    wordId, text, corefCol, sentId) : String.format("%s\t0\t%d\t%s\t%s\n", identifier,
                    wordId, text, corefCol);

            writer.write(wordline);
        }
        writer.write("#end document\n");
    }

    private static Map<Word, Triplet<Integer, Boolean, Boolean>> getWord2EntityClusterMapping(
            List<EntityCoreferenceCluster> clusters, Set<EntityMention> entitiesToBeInclude,
            boolean includeSingleton) {
        Map<Word, Triplet<Integer, Boolean, Boolean>> token2ClusterId = new HashMap<Word, Triplet<Integer, Boolean, Boolean>>();
        int clusterCounter = 0;

        Set<EntityMention> nonSingletonEntities = new HashSet<EntityMention>();

        // for non-singleton events
        for (EntityCoreferenceCluster cluster : clusters) {
            FSList childFS = cluster.getEntityMentions();

            if (childFS != null) {
                for (EntityMention entityMention : FSCollectionFactory.create(childFS, EntityMention.class)) {
                    if (entitiesToBeInclude.contains(entityMention)) {
                        nonSingletonEntities.add(entityMention);
                        if (entityMention.getBegin() < 0) {
                            logger.error("Omitting entity mention start index less than 0");
                        } else {
                            List<Word> evmWords = JCasUtil.selectCovered(Word.class, entityMention);
                            for (int i = 0; i < evmWords.size(); i++) {
                                boolean isFirst = i == 0;
                                boolean isLast = i == evmWords.size() - 1;
                                token2ClusterId.put(evmWords.get(i), new Triplet<Integer, Boolean, Boolean>(
                                        clusterCounter, isFirst, isLast));
                            }
                        }
                    }
                }
                clusterCounter++;
            } else {
                logger.error("Empty cluster found");
            }
        }

        // for singleton events
        if (includeSingleton) {
            for (EntityMention evm : entitiesToBeInclude) {
                if (nonSingletonEntities.contains(evm)) {
                    continue;
                }

                List<Word> evmWords = JCasUtil.selectCovered(Word.class, evm);
                for (int i = 0; i < evmWords.size(); i++) {
                    boolean isFirst = i == 0;
                    boolean isLast = i == evmWords.size() - 1;
                    token2ClusterId.put(evmWords.get(i), new Triplet<Integer, Boolean, Boolean>(
                            clusterCounter, isFirst, isLast));
                }
                clusterCounter++;
            }
        }

        return token2ClusterId;
    }

    private static Map<Word, Triplet<Integer, Boolean, Boolean>> getWord2EventClusterMapping(
            List<EventCoreferenceCluster> clusters, Set<EventMention> eventsToBeInclude,
            boolean includeSingleton) {
        Map<Word, Triplet<Integer, Boolean, Boolean>> token2ClusterId = new HashMap<Word, Triplet<Integer, Boolean, Boolean>>();
        int clusterCounter = 0;

        Set<EventMention> nonSingletonEvents = new HashSet<EventMention>();

        // for non-singleton events
        for (EventCoreferenceCluster cluster : clusters) {
            FSList childFS = cluster.getChildEventMentions();
            if (childFS != null) {
                for (EventMention evm : FSCollectionFactory.create(childFS, EventMention.class)) {
                    if (eventsToBeInclude.contains(evm)) {
                        nonSingletonEvents.add(evm);
                        if (evm.getBegin() < 0) {
                            logger.error("Omitting event mention start index less than 0");
                        } else {
                            List<Word> evmWords = JCasUtil.selectCovered(Word.class, evm);
                            for (int i = 0; i < evmWords.size(); i++) {
                                boolean isFirst = i == 0;
                                boolean isLast = i == evmWords.size() - 1;
                                token2ClusterId.put(evmWords.get(i), new Triplet<Integer, Boolean, Boolean>(
                                        clusterCounter, isFirst, isLast));
                            }
                        }
                    }
                }
                clusterCounter++;
            } else {
                logger.error("Empty cluster found");
            }
        }

        if (includeSingleton) {
            // for singleton events
            for (EventMention evm : eventsToBeInclude) {
                if (nonSingletonEvents.contains(evm)) {
                    continue;
                }

                List<Word> evmWords = JCasUtil.selectCovered(Word.class, evm);
                for (int i = 0; i < evmWords.size(); i++) {
                    boolean isFirst = i == 0;
                    boolean isLast = i == evmWords.size() - 1;
                    token2ClusterId.put(evmWords.get(i), new Triplet<Integer, Boolean, Boolean>(
                            clusterCounter, isFirst, isLast));
                }
                clusterCounter++;
            }
        }

        return token2ClusterId;
    }
}
