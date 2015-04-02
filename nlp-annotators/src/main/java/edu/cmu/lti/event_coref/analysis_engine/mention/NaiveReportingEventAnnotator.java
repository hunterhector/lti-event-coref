/**
 *
 */
package edu.cmu.lti.event_coref.analysis_engine.mention;

import com.google.common.collect.Table;
import edu.cmu.lti.event_coref.type.EventMention;
import edu.cmu.lti.utils.ling.FrameDataReader;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Require Semafor annotated, annotate all Frames named "Communication" as reporting
 *
 * @author Zhengzhong Liu, Hector
 */
public class NaiveReportingEventAnnotator extends JCasAnnotator_ImplBase {
    public static final String PARAM_ANNOTATION_VIEW_NAMES = "viewToAnnotate";

    public static final String PARAM_FRAME_RELATION_PATH = "frameRelationPath";

    @ConfigurationParameter(name = PARAM_ANNOTATION_VIEW_NAMES, mandatory = true)
    List<String> annotationViewNames;

    @ConfigurationParameter(name = PARAM_FRAME_RELATION_PATH)
    String frameRelationPath;

    public static final String targetEventType = "event";

    public static final String reportEventType = "event";

    private static final String reportingFrameName = "Communication";

    private static final Logger logger = LoggerFactory.getLogger(NaiveReportingEventAnnotator.class);

    private Set<String> allSubFrames = new HashSet<>();

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(org
     * .apache.uima.jcas.JCas)
     */
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        UimaConvenience.printProcessLog(aJCas, logger);
        annotateEventType(aJCas);

        for (String annotationViewName : annotationViewNames) {
            JCas viewToAnnotate = null;
            try {
                logger.info("Writing basic language units also for "
                        + annotationViewName);
                viewToAnnotate = aJCas.getView(annotationViewName);
            } catch (CASException e) {
                e.printStackTrace();
            }

            annotateEventType(viewToAnnotate);
        }
    }

    private Set<String> getReportingFrameName() {
        Map<String, Table<String, String, Map<String, String>>> relations = FrameDataReader.getFrameRelations(frameRelationPath);

        Table<String, String, Map<String, String>> inheritances = relations.get("Inheritance");

        Table<String, String, Map<String, String>> subframes = relations.get("Subframe");

        Table<String, String, Map<String, String>> perspectives = relations.get("Perspective_on");


        allSubFrames.addAll(getSubframe(reportingFrameName, inheritances));
        allSubFrames.addAll(getSubframe(reportingFrameName, subframes));
        allSubFrames.addAll(getSubframe(reportingFrameName, perspectives));

        return allSubFrames;
    }

    private Set<String> getSubframe(String superFrameName, Table<String, String, Map<String, String>> frameRelations) {
        Set<String> subframes = new HashSet<>();
        for (Table.Cell<String, String, Map<String, String>> cell : frameRelations.cellSet()) {
            if (cell.getColumnKey().equals(superFrameName)) {
                subframes.add(cell.getRowKey());
            }
        }
        return subframes;
    }

    private void annotateEventType(JCas aJCas) {
        for (EventMention evm : JCasUtil.select(aJCas, EventMention.class)) {
            if (evm.getEventType() == null) {
                String frameName = evm.getFrameName();
                if (frameName != null) {
                    if (allSubFrames.contains(frameName)) {
                        evm.setEventType(reportEventType);
                    } else {
                        evm.setEventType(targetEventType);
                    }
                }
            }
        }
    }
}