package edu.cmu.lti.utils.uima;

import edu.cmu.lti.utils.model.Span;
import edu.cmu.lti.utils.type.ComponentAnnotation;
import edu.cmu.lti.utils.type.ComponentTOP;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import java.util.Collection;
import java.util.List;

public class UimaAnnotationUtils {
    public static void finishAnnotation(ComponentAnnotation anno, int begin, int end,
                                        String componentId, String id, JCas aJCas) {
        anno.setBegin(begin);
        anno.setEnd(end);
        anno.setComponentId(componentId);
        anno.setId(id);
        anno.addToIndexes(aJCas);
    }

    public static void finishAnnotation(ComponentAnnotation anno, String componentId, String id,
                                        JCas aJCas) {
        anno.setComponentId(componentId);
        anno.setId(id);
        anno.addToIndexes(aJCas);
    }

    public static void finishAnnotation(ComponentAnnotation anno, int begin, int end,
                                        String componentId, int id, JCas aJCas) {
        anno.setBegin(begin);
        anno.setEnd(end);
        anno.setComponentId(componentId);
        anno.setId(Integer.toString(id));
        anno.addToIndexes();
    }

    public static void finishAnnotation(ComponentAnnotation anno, String componentId, int id,
                                        JCas aJCas) {
        anno.setComponentId(componentId);
        anno.setId(Integer.toString(id));
        anno.addToIndexes(aJCas);
    }

    public static void finishTop(ComponentTOP anno, String componentId, String id, JCas aJCas) {
        anno.setComponentId(componentId);
        anno.setId(id);
        anno.addToIndexes(aJCas);
    }

    public static void finishTop(ComponentTOP anno, String componentId, int id, JCas aJCas) {
        anno.setComponentId(componentId);
        anno.setId(Integer.toString(id));
        anno.addToIndexes(aJCas);
    }

    public static <T extends Annotation> T selectCoveredSingle(Annotation anno, Class<T> clazz) {
        T singleAnno = null;
        List<T> coveredAnnos = JCasUtil.selectCovered(clazz, anno);
        if (coveredAnnos.size() > 1) {
            System.err.println(String.format(
                    "Annotation [%s] contains more than one subspan of type [%s]", anno.getCoveredText(),
                    clazz.getSimpleName()));
        }

        if (coveredAnnos.size() == 0) {
            System.err.println(String.format(
                    "Annotation [%s] contains does not have subspans of type [%s]",
                    anno.getCoveredText(), clazz.getSimpleName()));
        } else {
            singleAnno = coveredAnnos.get(0);
        }

        return singleAnno;
    }

    public static <T extends ComponentAnnotation> void assignAnnotationIds(Collection<T> annos) {
        int id = 0;
        for (ComponentAnnotation anno : annos) {
            anno.setId(Integer.toString(id++));
        }
    }

    public static <T extends ComponentTOP> void assignTopIds(Collection<T> annos) {
        int id = 0;
        for (ComponentTOP anno : annos) {
            anno.setId(Integer.toString(id++));
        }
    }

    public static Span toSpan(ComponentAnnotation anno) {
        return new Span(anno.getBegin(), anno.getEnd());
    }

    public static int entityIdToInteger(String eid) {
        return Integer.parseInt(eid);
    }
}
