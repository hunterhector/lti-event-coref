package edu.cmu.lti.event_coref.analysis_engine.semantic;

import ac.biu.nlp.normalization.BiuNormalizer;
import com.google.common.collect.Iterators;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.utils.model.Span;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Annotation requirement : 1. Stanford Corenlp (use the number entity annotation) 2. Annotators
 * about agent patient (to associate number with)
 *
 * @author Zhengzhong Liu, Hector
 */
public class NumberAnnotator extends JCasAnnotator_ImplBase {
    public static final String NUMBER_NORMALIZER_STRING_RULE = "normalize_rule_file";

    private final String ANNOTATOR_COMPONENT_ID = "System-number-annotation";

    @ConfigurationParameter(name = NUMBER_NORMALIZER_STRING_RULE)
    private String normalizeRulePath;

    private BiuNormalizer normalizer;

    private final String numberEntityType = "NUMBER";

    private final String stanfordNumModifierDependency = "num";

    private final String stanfordDetModifilerDependency = "det";

    private Map<String, String> commonNumberMappings = new HashMap<String, String>();

    // control duplication, need to be refresh for each Cas
    private Map<Span, NumberAnnotation> numberMap;

    private static final Logger logger = LoggerFactory.getLogger(NumberAnnotator.class);

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        // initialize normalizer
        File normalizeRuleFile = new File(normalizeRulePath);
        try {
            normalizer = new BiuNormalizer(normalizeRuleFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        commonNumberMappings.put("one", "1");
        commonNumberMappings.put("two", "2");
        commonNumberMappings.put("three", "3");
        commonNumberMappings.put("four", "4");
        commonNumberMappings.put("two", "2");
        commonNumberMappings.put("two", "2");
        commonNumberMappings.put("dozen", "12");
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        String articleTitle = UimaConvenience.getShortDocumentName(aJCas);
        logger.info(String.format("Processing article: %s with [%s]", articleTitle, this.getClass()
                .getSimpleName()));

        numberMap = new HashMap<Span, NumberAnnotation>();

        Collection<StanfordEntityMention> allEntityMentions = JCasUtil.select(aJCas,
                StanfordEntityMention.class);

        // 1 Create number annotations based on stanford dependencies
        for (StanfordEntityMention em : allEntityMentions) {
            String entityType = em.getEntityType();
            if (entityType != null && entityType.equals(numberEntityType)) {
                String normalized = getNormalizedNumberString(em.getCoveredText());
                createNumberAnnotation(aJCas, em.getBegin(), em.getEnd(), normalized);
            }
        }

        Map<StanfordDependencyNode, Collection<NumberAnnotation>> node2NumberMap = JCasUtil
                .indexCovering(aJCas, StanfordDependencyNode.class, NumberAnnotation.class);

        // 2 Associate entities with the number annotations, also consider the entity itself could be a
        // number
        for (EntityBasedComponent entity : JCasUtil.select(aJCas, EntityBasedComponent.class)) {
            Word headWord = entity.getHeadWord();

            if (headWord == null) {
                continue;// currently locations are don't have head word
            }

            // 2.1 if the annotation is single word, chances are its head is the same with quantity word,
            // try to find number format the annotation itself
            if (JCasUtil.selectCovered(Word.class, entity).size() == 1) {
                String entityOriginalStr = entity.getCoveredText();
                String normalizedEntityStr = getNormalizedNumberString(entityOriginalStr);
                if (!normalizedEntityStr.equalsIgnoreCase(normalizationPreprocessing(entityOriginalStr))) {
                    NumberAnnotation number = createNumberAnnotation(aJCas, entity.getBegin(),
                            entity.getEnd(), normalizedEntityStr);
                    entity.setQuantity(number);
                }
            }
            // 2.2 find number from the dependencies
            NumberAnnotation number = getNaiveQuantityFromModifier(aJCas, node2NumberMap, headWord);
            entity.setQuantity(number);

        }

        // 3 associate number annotations with event mentions
        for (EventMention evm : JCasUtil.select(aJCas, EventMention.class)) {
            Word headWord = evm.getHeadWord();
            if (headWord != null) {// some events don't even appear as text
                NumberAnnotation number = getNaiveQuantityFromModifier(aJCas, node2NumberMap, headWord);
                evm.setQuantity(number);
            }
        }

    }

    private NumberAnnotation getNaiveQuantityFromModifier(JCas aJCas,
                                                          Map<StanfordDependencyNode, Collection<NumberAnnotation>> node2NumberMap, Word headWord) {
        List<StanfordDependencyNode> coveredNodes = JCasUtil.selectCovered(
                StanfordDependencyNode.class, headWord);
        if (coveredNodes.isEmpty()) {
            return null; // something like punctuation don't have dependency node
        }

        StanfordDependencyNode headNode = coveredNodes.get(0);
        FSList childRelationsFS = headNode.getChildRelations();
        NumberAnnotation number = null;

        if (childRelationsFS != null) {
            Collection<StanfordDependencyRelation> childDependencies = FSCollectionFactory.create(
                    childRelationsFS, StanfordDependencyRelation.class);
            for (StanfordDependencyRelation childDependency : childDependencies) {
                String relationType = childDependency.getRelationType();
                if (relationType.equals(stanfordNumModifierDependency)) {
                    StanfordDependencyNode childNode = childDependency.getChild();
                    if (node2NumberMap.containsKey(childNode)) {
                        number = Iterators.get(node2NumberMap.get(childNode).iterator(), 0);
                        break;
                    }
                }

                if (relationType.equals(stanfordDetModifilerDependency)) {
                    StanfordDependencyNode childNode = childDependency.getChild();
                    String determiner = childNode.getCoveredText().toLowerCase();
                    if (determiner.equals("a") || determiner.equals("an")) {
                        number = createNumberAnnotation(aJCas, childNode.getBegin(), childNode.getEnd(), "1");
                        break;
                    }
                }
            }
        }
        return number;
    }

    private String normalizationPreprocessing(String beforePreprocessStr) {
        return beforePreprocessStr.toLowerCase().replace("-", " ");
    }

    private String getNormalizedNumberString(String originalStr) {
        // this is very hacky
        String numberStr = normalizationPreprocessing(originalStr);

        String normalized = null;
        try {
            normalized = normalizer.normalize(numberStr);
            if (commonNumberMappings.containsKey(normalized)) {
                normalized = commonNumberMappings.get(normalized);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return normalized;
    }

    private NumberAnnotation createNumberAnnotation(JCas aJCas, int begin, int end, String normalized) {
        Span span = new Span(begin, end);
        if (numberMap.containsKey(span)) {
            return numberMap.get(span);
        } else {
            NumberAnnotation numAnn = new NumberAnnotation(aJCas);
            numAnn.setBegin(begin);
            numAnn.setEnd(end);
            numAnn.addToIndexes(aJCas);
            numAnn.setComponentId(ANNOTATOR_COMPONENT_ID);
            numAnn.setNormalizedString(normalized);

            numberMap.put(span, numAnn);

            return numAnn;
        }
    }

}
