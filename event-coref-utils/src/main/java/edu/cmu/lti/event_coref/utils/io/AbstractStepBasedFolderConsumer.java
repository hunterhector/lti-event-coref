package edu.cmu.lti.event_coref.utils.io;

import com.google.common.base.Joiner;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStepBasedFolderConsumer extends
        JCasAnnotator_ImplBase {
    public static final String PARAM_PARENT_RESOURCE_INPUT_PATH = "ParentResourceInputPath";

    public static final String PARAM_BASE_RESOURCE_INPUT_DIR_NAME = "BaseResourceInputDirectoryName";

    public static final String PARAM_RESOURCE_INPUT_STEP_NUMBER = "ResourceOutputStepNumber";

    public static final String PARAM_RESOURCE_INPUT_FILE_SUFFIX = "ResourceInputFileSuffix";

    @ConfigurationParameter(name = PARAM_PARENT_RESOURCE_INPUT_PATH)
    protected String parentResourceInputPath;

    @ConfigurationParameter(name = PARAM_BASE_RESOURCE_INPUT_DIR_NAME)
    protected String baseResourceInputDirName;

    @ConfigurationParameter(name = PARAM_RESOURCE_INPUT_STEP_NUMBER)
    private Integer resourceInputStepNumber;

    @ConfigurationParameter(name = PARAM_RESOURCE_INPUT_FILE_SUFFIX)
    protected String resourceFileSuffix;

    protected File resourceDir;

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException {
        super.initialize(aContext);

        List<String> dirNameSegments = new ArrayList<String>();
        dirNameSegments.add(String.format("%02d", resourceInputStepNumber));
        dirNameSegments.add(String.format(resourceFileSuffix));
        dirNameSegments.add(baseResourceInputDirName);

        Joiner underscoreJoiner = Joiner.on("_");

        String inputDirectoryPath = parentResourceInputPath + "/" + underscoreJoiner.join(dirNameSegments);

        resourceDir = new File(parentResourceInputPath + "/" + underscoreJoiner.join(dirNameSegments));
        if (!resourceDir.exists()) {
            throw new IllegalArgumentException(String.format("Cannot find the directory [%s] specified, please check parameters", inputDirectoryPath));
        }

        try {
            subInitialize();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Sub class can do initialization things here
     *
     * @throws Exception
     */
    public abstract void subInitialize() throws Exception;
}
