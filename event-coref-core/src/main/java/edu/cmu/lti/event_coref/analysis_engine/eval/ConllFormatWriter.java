/**
 *
 */
package edu.cmu.lti.event_coref.analysis_engine.eval;

import edu.cmu.lti.event_coref.model.EventCorefConstants;
import edu.cmu.lti.event_coref.utils.eval.ConllFormatConverter;
import edu.cmu.lti.event_coref.utils.io.AbstractStepBasedFolderWriter;
import edu.cmu.lti.utils.general.FileUtils;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.descriptor.ConfigurationParameter;

import java.io.BufferedWriter;
import java.io.IOException;

//import edu.cmu.lti.event_coref.model.EvaluationConstants;

/**
 * @author Zhengzhong Liu, Hector
 */
public class ConllFormatWriter extends AbstractStepBasedFolderWriter {
    private static final Logger logger = LoggerFactory.getLogger(ConllFormatWriter.class);

    BufferedWriter systemEvmWriter;

    BufferedWriter goldEvmWriter;

    BufferedWriter systemEntityWriter;

    BufferedWriter goldEntityWriter;

    public static final String PARAM_GOLD_STANDARD_VIEWNAME = "GoldStandardViewName";

    public static final String PARAM_WRITE_SENTENCE_ID = "writerSentenceId";

    public static final String PARAM_GOLD_COMPONENT_ID_PREFIX = "goldComponentIdPrefix";

    public static final String PARAM_SYSTEM_COMPONENT_ID_PREFIX = "systemComponentIdPrefix";

    public static final String PARAM_INCLUDE_SINGLETON = "includeSingleton";

    @ConfigurationParameter(mandatory = true, name = PARAM_GOLD_COMPONENT_ID_PREFIX)
    private String goldComponentIdPrefix;

    @ConfigurationParameter(mandatory = true, name = PARAM_SYSTEM_COMPONENT_ID_PREFIX)
    private String systemComponentIdPrefix;

    @ConfigurationParameter(mandatory = true, name = PARAM_GOLD_STANDARD_VIEWNAME)
    private String goldViewName;

    @ConfigurationParameter(name = PARAM_WRITE_SENTENCE_ID)
    private Boolean writeSentenceId;

    @ConfigurationParameter(mandatory = true, name = PARAM_INCLUDE_SINGLETON)
    private Boolean includeSingleton;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(org.apache.uima.jcas.JCas)
     */
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        String articleName = UimaConvenience.getShortDocumentName(aJCas);
        UimaConvenience.printProcessLog(aJCas, logger);

        JCas goldView = null;

        try {
            goldView = aJCas.getView(goldViewName);
        } catch (CASException e) {

        } catch (CASRuntimeException e) {
            System.err.println("Gold standard is not annotated");
        }

        if (writeSentenceId == null)
            writeSentenceId = false;

        try {
            // String systemOutputPath = outputDir.getCanonicalPath() + "/" + title + "_response.txt";
            // String goldOutputPath = outputDir.getCanonicalPath() + "/" + title + "_key.txt";

            logger.info("Writing system outoupt for event mentions");
            ConllFormatConverter.writeEventMentionClusters(aJCas, articleName, systemEvmWriter,
                    EventCorefConstants.FULL_COREFERENCE_TYPE, writeSentenceId, systemComponentIdPrefix,
                    includeSingleton);

            if (goldView != null) {
                logger.info("Writing gold standard output for event mentions");
                ConllFormatConverter.writeEventMentionClusters(goldView, articleName, goldEvmWriter,
                        EventCorefConstants.FULL_COREFERENCE_TYPE, writeSentenceId, goldComponentIdPrefix,
                        includeSingleton);
            }

            logger.info("Writing system output for entity mentions");
            System.err.println("Temporarily transfer to lower for component id");
            ConllFormatConverter.writeEntityMentionClusters(aJCas, articleName, systemEntityWriter,
                    writeSentenceId, systemComponentIdPrefix.toLowerCase(), includeSingleton);

            if (goldView != null) {
                logger.info("Writing gold standard output for entity mentions");
                ConllFormatConverter.writeEntityMentionClusters(aJCas, articleName, goldEntityWriter,
                        writeSentenceId, goldComponentIdPrefix, includeSingleton);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void subInitialize() {
        try {
            systemEvmWriter = FileUtils.openFileWrite(outputDir.getCanonicalPath() + "/"
                    + "event_response.txt");
            goldEvmWriter = FileUtils.openFileWrite(outputDir.getCanonicalPath() + "/" + "event_key.txt");

            systemEntityWriter = FileUtils.openFileWrite(outputDir.getCanonicalPath() + "/"
                    + "entity_response.txt");
            goldEntityWriter = FileUtils.openFileWrite(outputDir.getCanonicalPath() + "/"
                    + "entity_key.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void collectionProcessComplete() {
        try {
            systemEvmWriter.close();
            goldEvmWriter.close();
            systemEntityWriter.close();
            goldEntityWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
