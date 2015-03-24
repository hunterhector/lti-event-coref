package edu.cmu.lti.util.io;

import edu.cmu.lti.util.general.StringUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple collection reader that reads CASes in XMI format from a directory in the filesystem.
 */
public class XmiCollectionReader extends CollectionReader_ImplBase {
    /**
     * Name of configuration parameter that must be set to the path of a directory containing the XMI
     * files.
     */
    public static final String PARAM_VIEW_NAME = "ViewName";

    public static final String PARAM_PARENT_INPUT_DIR = "ParentInputDirectory";

    public static final String PARAM_BASE_INPUT_DIR_NAME = "BaseInputDirectoryName";

    public static final String PARAM_STEP_NUMBER = "StepNumber";

    /**
     * Name of the configuration parameter that must be set to indicate if the execution fails if an
     * encountered type is unknown
     */
    public static final String PARAM_FAILUNKNOWN = "FailOnUnknownType";

    private String mViewName;

    private String mParentInputDir;

    private String mBaseDirectoryName;

    private Integer mStepNumber;

    private Boolean mFailOnUnknownType;

    private ArrayList<File> mFiles;

    private int mCurrentIndex;

    /**
     * @see org.apache.uima.collection.CollectionReader_ImplBase#initialize()
     */
    public void initialize() throws ResourceInitializationException {
        mViewName = (String) getConfigParameterValue(PARAM_VIEW_NAME);
        mParentInputDir = (String) getConfigParameterValue(PARAM_PARENT_INPUT_DIR);
        mBaseDirectoryName = (String) getConfigParameterValue(PARAM_BASE_INPUT_DIR_NAME);

        mStepNumber = (Integer) getConfigParameterValue(PARAM_STEP_NUMBER);

        mFailOnUnknownType = (Boolean) getConfigParameterValue(PARAM_FAILUNKNOWN);
        if (null == mFailOnUnknownType) {
            mFailOnUnknownType = true; // default to true if not specified
        }

        List<Object> partOfDirNames = new ArrayList<Object>();

        if (mStepNumber != null) {
            String stepNumberStr = StringUtils.convertIntegerToString(mStepNumber);
            partOfDirNames.add(StringUtils.padStringToLeft(stepNumberStr, '0', 2));
        }
        partOfDirNames.add(mBaseDirectoryName);

        String inputDirectory = mParentInputDir + "/"
                + StringUtils.concatenate(partOfDirNames, "_");
        File directory = new File(inputDirectory);
        mCurrentIndex = 0;

        // if input directory does not exist or is not a directory, throw exception
        if (!directory.exists() || !directory.isDirectory()) {
            throw new ResourceInitializationException(ResourceConfigurationException.DIRECTORY_NOT_FOUND,
                    new Object[]{PARAM_PARENT_INPUT_DIR, this.getMetaData().getName(),
                            directory.getPath()});
        }

        // get list of .xmi files in the specified directory
        mFiles = new ArrayList<File>();
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (!files[i].isDirectory() && files[i].getName().endsWith(".xmi")) {
                mFiles.add(files[i]);
            }
        }
    }

    /**
     * @see org.apache.uima.collection.CollectionReader#hasNext()
     */
    public boolean hasNext() {
        return mCurrentIndex < mFiles.size();
    }

    /**
     * @see org.apache.uima.collection.CollectionReader#getNext(org.apache.uima.cas.CAS)
     */
    public void getNext(CAS aCAS) throws IOException, CollectionException {
        try {
            if (!StringUtils.isNullOrEmptyString(mViewName)) {
                aCAS.createView(mViewName);
                aCAS = aCAS.getView(mViewName);
            }
        } catch (Exception e) {
            throw new CollectionException(e);
        }

        File currentFile = (File) mFiles.get(mCurrentIndex++);
        FileInputStream inputStream = new FileInputStream(currentFile);
        try {
            XmiCasDeserializer.deserialize(inputStream, aCAS, !mFailOnUnknownType);
        } catch (SAXException e) {
            throw new CollectionException(e);
        } finally {
            inputStream.close();
        }
    }

    /**
     * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#close()
     */
    public void close() throws IOException {
    }

    /**
     * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#getProgress()
     */
    public Progress[] getProgress() {
        return new Progress[]{new ProgressImpl(mCurrentIndex, mFiles.size(), Progress.ENTITIES)};
    }

}