package edu.cmu.lti.event_coref.utils.io;

import com.google.common.base.Joiner;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class that handle directory name based on the date and step number for convenience
 *
 * @author Zhengzhong Liu, Hector
 */
public abstract class AbstractStepBasedFolderWriter extends JCasAnnotator_ImplBase {
    private static final Logger logger = LoggerFactory.getLogger(AbstractStepBasedFolderWriter.class);

    public static final String PARAM_PARENT_OUTPUT_PATH = "ParentFeatureOutputPath";

    public static final String PARAM_BASE_OUTPUT_DIR_NAME = "BaseOutputDirectoryName";

    public static final String PARAM_OUTPUT_STEP_NUMBER = "OutputStepNumber";

    public static final String PARAM_OUTPUT_FILE_SUFFIX = "OutputFileSuffix";

    @ConfigurationParameter(name = PARAM_PARENT_OUTPUT_PATH)
    private String parentOutputPath;

    @ConfigurationParameter(name = PARAM_BASE_OUTPUT_DIR_NAME)
    private String baseOutputDirName;

    @ConfigurationParameter(name = PARAM_OUTPUT_STEP_NUMBER)
    private Integer outputStepNumber;

    @ConfigurationParameter(name = PARAM_OUTPUT_FILE_SUFFIX)
    protected String outputFileSuffix;

    protected File outputDir;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        List<String> dirNameSegments = new ArrayList<String>();
        dirNameSegments.add(String.format("%02d", outputStepNumber));
        dirNameSegments.add(String.format(outputFileSuffix));
        dirNameSegments.add(baseOutputDirName);

        Joiner underscoreJoiner = Joiner.on("_");

        outputDir = new File(parentOutputPath + "/" + underscoreJoiner.join(dirNameSegments));
        if (!outputDir.exists()) {
            outputDir.mkdirs();
            logger.debug("Creating path: " + outputDir.getAbsolutePath());
        }

        logger.debug("Output path is: " + outputDir.getAbsolutePath());

        subInitialize();
    }

    /**
     * Sub class can do initialization things here
     */
    public abstract void subInitialize();
}
