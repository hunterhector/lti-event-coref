package edu.cmu.lti.event_coref.io;

import edu.cmu.lti.utils.general.StringUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * A simple collection preprocessor that reads CASes in XMI format from a directory in the filesystem.
 */
public class StepBasedDirGzippedXmiCollectionReader extends AbstractStepBasedDirReader {
    public static final String PARAM_INPUT_VIEW_NAME = "ViewName";

    private static final String DEFAULT_FILE_SUFFIX = ".xmi.gz";

    private String inputViewName;

    private List<File> xmiFiles;

    private int currentDocIndex;

    private static final Logger logger = LoggerFactory.getLogger(StepBasedDirGzippedXmiCollectionReader.class);

    /**
     * @see org.apache.uima.collection.CollectionReader_ImplBase#initialize()
     */
    public void initialize() throws ResourceInitializationException {
        super.initialize();

        inputViewName = (String) getConfigParameterValue(PARAM_INPUT_VIEW_NAME);
        if (StringUtils.isNullOrEmptyString(inputFileSuffix)) {
            inputFileSuffix = DEFAULT_FILE_SUFFIX;
        }

        // Get a list of XMI files in the specified directory
        xmiFiles = new ArrayList<File>();
        File[] files = inputDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (!files[i].isDirectory() && files[i].getName().endsWith(inputFileSuffix)) {
                xmiFiles.add(files[i]);
            }
        }

        if (xmiFiles.size() == 0) {
            logger.warn("The directory " + inputDir.getAbsolutePath()
                    + " does not have any compressed files ending with " + inputFileSuffix);
        }

        currentDocIndex = 0;
    }

    @Override
    public void subInitialize() {
    }

    /**
     * @see org.apache.uima.collection.CollectionReader#hasNext()
     */
    public boolean hasNext() {
        return currentDocIndex < xmiFiles.size();
    }

    /**
     * @see org.apache.uima.collection.CollectionReader#getNext(org.apache.uima.cas.CAS)
     */
    public void getNext(CAS aCAS) throws IOException, CollectionException {
        try {
            if (!StringUtils.isNullOrEmptyString(inputViewName)) {
                aCAS = aCAS.getView(inputViewName);
            }
        } catch (Exception e) {
            throw new CollectionException(e);
        }

        File currentFile = xmiFiles.get(currentDocIndex);
        currentDocIndex++;

        GZIPInputStream gzipIn = new GZIPInputStream(new FileInputStream(currentFile));
        try {
            XmiCasDeserializer.deserialize(gzipIn, aCAS, !failOnUnknownType);
            gzipIn.close();
        } catch (SAXException e) {
            throw new CollectionException(e);
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
        return new Progress[]{new ProgressImpl(currentDocIndex, xmiFiles.size(), Progress.ENTITIES)};
    }

}