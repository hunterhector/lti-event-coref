package edu.cmu.lti.event_coref.io;

import edu.cmu.lti.utils.general.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * This plain text writer generates some text-based output at the end of its process (i.e., via the
 * collectionProcessComplete() method). You can use this writer to output a certain kind of summary
 * or statistics over all input.
 *
 * @author Jun Araki
 */
public abstract class AbstractPlainTextAggregator extends JCasAnnotator_ImplBase {
    private static final Logger logger = LoggerFactory.getLogger(AbstractPlainTextAggregator.class);

    public static final String PARAM_OUTPUT_FILE_PATH = "OutputFilePath";

    @ConfigurationParameter(name = PARAM_OUTPUT_FILE_PATH, mandatory = true)
    private String outputFilePath;

    private File outputFile;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        outputFile = new File(outputFilePath);
        File parentDir = outputFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        subInitialize(context);
    }

    @Override
    public void collectionProcessComplete() {
        logger.debug("Writing to : " + outputFilePath);
        String text = getAggregatedTextToPrint();
        FileUtils.writeFile(outputFile, text);
    }

    public abstract String getAggregatedTextToPrint();

    /**
     * Subclass can implement this to get more things to done
     *
     * @param context
     */
    public abstract void subInitialize(UimaContext context);

}