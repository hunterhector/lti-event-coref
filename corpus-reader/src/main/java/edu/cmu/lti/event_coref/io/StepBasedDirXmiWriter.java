package edu.cmu.lti.event_coref.io;

import edu.cmu.lti.utils.general.ListUtils;
import edu.cmu.lti.utils.general.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLSerializer;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * This analysis engine outputs CAS in the XMI format.
 * 
 * @author Jun Araki
 */
public class StepBasedDirXmiWriter extends AbstractStepBasedDirWriter {

  public static final String PARAM_SRC_DOC_INFO_VIEW_NAME = "SourceDocumentInfoViewName";

  public static final String PARAM_OUTPUT_FILE_NUMBERS = "OutputFileNumbers";

  @ConfigurationParameter(name = PARAM_SRC_DOC_INFO_VIEW_NAME, mandatory = false)
  /** The view where you extract source document information */
  private String srcDocInfoViewName;

  @ConfigurationParameter(name = PARAM_OUTPUT_FILE_NUMBERS, mandatory = false)
  /**
   * This is a list of documents that you want to generate XMI output. If it is
   * null or empty, the writer works against all input.  UIMA does not allow us
   * to pass this into Integer list, but it would be ideal.
   */
  private List<String> outputDocumentNumberList;

  private int docCounter;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    docCounter = 0;
  }

  @Override
  public void subInitialize(UimaContext context) {
  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    docCounter++;

    JCas srcDocInfoView = null;
    try {
      if (!StringUtils.isNullOrEmptyString(srcDocInfoViewName)) {
        srcDocInfoView = aJCas.getView(srcDocInfoViewName);
      } else {
        srcDocInfoView = aJCas;
      }
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }

    if (!ListUtils.isNullOrEmptyList(outputDocumentNumberList)) {
      if (!outputDocumentNumberList.contains(StringUtils.convertIntegerToString(docCounter))) {
        return;
      }
    }

    // Retrieve the filename of the input file from the CAS.
    FSIterator<?> it = srcDocInfoView.getAnnotationIndex(SourceDocumentInformation.type).iterator();
    File outputFile = null;
    if (it.hasNext()) {
      SourceDocumentInformation fileLoc = (SourceDocumentInformation) it.next();
      File inFile;
      try {
        inFile = new File(new URL(fileLoc.getUri()).getPath());
        StringBuilder buf = new StringBuilder();
        buf.append(inFile.getName());
        if (fileLoc.getOffsetInSource() > 0) {
          buf.append("_" + fileLoc.getOffsetInSource());
        }
        if (StringUtils.isNullOrEmptyString(outputFileSuffix)) {
          buf.append(".xmi");
        } else {
          buf.append(outputFileSuffix);
        }

        String outputFileName = buf.toString();
        outputFile = new File(outputDir, outputFileName);
      } catch (MalformedURLException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }
    if (outputFile == null) {
      outputFile = new File(outputDir, "doc" + docCounter + ".xmi");
    }
    // serialize XCAS and write to output file
    try {
      writeXmi(aJCas.getCas(), outputFile);
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    } catch (SAXException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  /**
   * Serialize a CAS to a file in XMI format
   * 
   * @param aCas
   *          CAS to serialize
   * @param xmiFile
   *          output file
   * @throws org.xml.sax.SAXException
   * @throws Exception
   * 
   * @throws org.apache.uima.resource.ResourceProcessException
   */
  private void writeXmi(CAS aCas, File xmiFile) throws IOException, SAXException {
    FileOutputStream out = null;

    try {
      // write XMI
      out = new FileOutputStream(xmiFile);
      XmiCasSerializer ser = new XmiCasSerializer(aCas.getTypeSystem());
      XMLSerializer xmlSer = new XMLSerializer(out, false);
      ser.serialize(aCas, xmlSer.getContentHandler());
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

}
