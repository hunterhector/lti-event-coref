/**
 *
 */
package edu.cmu.lti.event_coref.io;

import au.com.bytecode.opencsv.CSVWriter;
import edu.cmu.lti.event_coref.utils.io.CsvFactory;
import edu.cmu.lti.utils.general.StringUtils;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhengzhong Liu, Hector
 */
public abstract class AbstractCsvWriterAnalysisEngine extends JCasAnnotator_ImplBase {

    public static final String PARAM_PARENT_OUTPUT_DIR = "ParentOutputDirectory";

    public static final String PARAM_BASE_OUTPUT_DIR_NAME = "BaseOutputDirectoryName";

    public static final String PARAM_STEP_NUMBER = "StepNumber";

    public static final String PARAM_OUTPUT_FILE_SUFFIX = "OutputFileSuffix";

    public static final String PARAM_SOURCE_DOCUMENT_INFO_VIEW_NAME = "sourceDocumentViewName";

    @ConfigurationParameter(name = PARAM_PARENT_OUTPUT_DIR, mandatory = true)
    private String parentOutputDir;

    @ConfigurationParameter(name = PARAM_BASE_OUTPUT_DIR_NAME, mandatory = true)
    private String baseOutputDirName;

    @ConfigurationParameter(name = PARAM_STEP_NUMBER, mandatory = true)
    private Integer stepNumber;

    @ConfigurationParameter(name = PARAM_OUTPUT_FILE_SUFFIX)
    private String outputFileSuffix;

    @ConfigurationParameter(name = PARAM_SOURCE_DOCUMENT_INFO_VIEW_NAME, mandatory = true, description = "The view where the SourceDocumentInfo type can be found")
    private String sourceDocumentViewName;

    private File outputDir;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        try {
            super.initialize(context);
        } catch (ResourceInitializationException e) {
            throw new ResourceInitializationException(e);
        }

        List<Object> partOfDirNames = new ArrayList<Object>();
        if (stepNumber != null) {
            String stepNumberStr = StringUtils.convertIntegerToString(stepNumber);
            partOfDirNames.add(StringUtils.padStringToLeft(stepNumberStr, '0', 2));
        }
        partOfDirNames.add(baseOutputDirName);

        outputDir = new File(parentOutputDir + "/"
                + StringUtils.concatenate(partOfDirNames, "_"));
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        subinitialize(context);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        JCas sourceDocumentView = UimaConvenience.getView(aJCas, sourceDocumentViewName);

        // Retrieve the filename of the input file from the CAS.
        File outFile = null;
        SourceDocumentInformation fileLoc = JCasUtil.selectSingle(sourceDocumentView,
                SourceDocumentInformation.class);

        File inFile;
        try {
            inFile = new File(new URL(fileLoc.getUri()).getPath());
            String outFileName = inFile.getName();
            if (fileLoc.getOffsetInSource() > 0) {
                outFileName += ("_" + fileLoc.getOffsetInSource());
            }
            if (outputFileSuffix != null && outputFileSuffix.length() > 0) {
                outFileName += outputFileSuffix;
            }

            String defaultOutputFileSuffix = ".csv";
            if (!outFileName.endsWith(defaultOutputFileSuffix)) {
                outFileName += defaultOutputFileSuffix;
            }
            outFile = new File(outputDir, outFileName);

            CSVWriter writer = CsvFactory.getCSVWriter(outFile);

            String[] header = getHeader();

            if (header != null) {
                writer.writeNext(header);
            }

            prepare(aJCas);
            while (hasNextRow()) {
                writer.writeNext(getNextCsvRow());
            }

            writer.flush();
            writer.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract String[] getHeader();

    protected abstract void prepare(JCas aJCas);

    protected abstract boolean hasNextRow();

    protected abstract String[] getNextCsvRow();

    /**
     * Subclass can implement this to get more things to done
     *
     * @param context
     */
    protected void subinitialize(UimaContext context) {

    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {

    }
}
