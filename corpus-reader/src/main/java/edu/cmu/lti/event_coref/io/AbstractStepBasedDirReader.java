package edu.cmu.lti.event_coref.io;

import com.google.common.base.Joiner;
import edu.cmu.lti.utils.general.StringUtils;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract preprocessor to consume input in a directory whose name is based on the specified date and
 * step number for convenience
 *
 * @author Jun Araki
 */
public abstract class AbstractStepBasedDirReader extends CollectionReader_ImplBase {

    public static final String PARAM_PARENT_INPUT_DIR_PATH = "ParentInputDirPath";

    public static final String PARAM_BASE_INPUT_DIR_NAME = "BaseInputDirectoryName";

    public static final String PARAM_INPUT_STEP_NUMBER = "InputStepNumber";

    public static final String PARAM_INPUT_FILE_SUFFIX = "InputFileSuffix";

    public static final String PARAM_FAIL_UNKNOWN = "FailOnUnknownType";

    private String parentInputDirPath;


    private String baseInputDirName;

    private int inputStepNumber;

    protected String inputFileSuffix;

    protected boolean failOnUnknownType;

    protected File inputDir;

    @Override
    public void initialize() throws ResourceInitializationException {
        super.initialize();

        parentInputDirPath = (String) getConfigParameterValue(PARAM_PARENT_INPUT_DIR_PATH);
        baseInputDirName = (String) getConfigParameterValue(PARAM_BASE_INPUT_DIR_NAME);
        inputStepNumber = (Integer) getConfigParameterValue(PARAM_INPUT_STEP_NUMBER);
        inputFileSuffix = (String) getConfigParameterValue(PARAM_INPUT_FILE_SUFFIX);
        failOnUnknownType = (Boolean) getConfigParameterValue(PARAM_FAIL_UNKNOWN);

        List<String> dirNameSegments = new ArrayList<String>();
        dirNameSegments.add(String.format("%02d", inputStepNumber));
        if (!StringUtils.isNullOrEmptyString(inputFileSuffix)) {
            dirNameSegments.add(String.format(inputFileSuffix));
        }
        dirNameSegments.add(baseInputDirName);

        String dirName = Joiner.on("_").join(dirNameSegments);

        inputDir = new File(parentInputDirPath, dirName);
        if (!inputDir.exists()) {
            throw new IllegalArgumentException(String.format(
                    "Cannot find the directory [%s] specified, please check parameters",
                    inputDir.getAbsolutePath()));
        }

        subInitialize();
    }

    /**
     * A subclass can do its own initialization in this method
     *
     * @throws Exception
     */
    public abstract void subInitialize();

}
