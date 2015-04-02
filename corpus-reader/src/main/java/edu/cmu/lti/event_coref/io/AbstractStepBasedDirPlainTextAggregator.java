package edu.cmu.lti.event_coref.io;

import edu.cmu.lti.utils.general.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;

/**
 * This plain text writer generates some text-based output at the end of its process (i.e., via the
 * collectionProcessComplete() method). You can use this writer to output a certain kind of summary
 * or statistics over all input.
 * 
 * @author Jun Araki
 */
public abstract class AbstractStepBasedDirPlainTextAggregator extends AbstractStepBasedDirWriter {

  public static final String PARAM_OUTPUT_FILE_NAME = "OutputFileName";

  @ConfigurationParameter(name = PARAM_OUTPUT_FILE_NAME, mandatory = true)
  private String outputFileName;

  protected File outputFile;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    outputFile = new File(outputDir, outputFileName);
  }

  @Override
  public void collectionProcessComplete() {
    String text = getAggregatedTextToPrint();
    FileUtils.writeFile(outputFile, text);
  }

  /**
   * This method is expected to create some aggregated text in a subclass, and pass it to the method
   * collectionProcessComplete().
   * 
   * @return
   */
  public abstract String getAggregatedTextToPrint();

}