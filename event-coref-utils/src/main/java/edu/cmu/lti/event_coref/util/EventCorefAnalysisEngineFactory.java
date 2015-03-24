package edu.cmu.lti.event_coref.util;

import edu.cmu.lti.util.io.*;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.metadata.AnalysisEngineMetaData;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;

import java.util.Set;

public class EventCorefAnalysisEngineFactory {

    /**
     * Creates a simple XMI reader assuming the directory naming convention
     *
     * @param parentInputDirName
     * @param baseInputDirName
     * @param stepNumber
     * @param failOnUnkown
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static CollectionReaderDescription createXmiReader(String parentInputDirName,
                                                              String baseInputDirName, Integer stepNumber, Boolean failOnUnkown)
            throws ResourceInitializationException {
        // Instantiate a collection reader to get XMI as input.
        CollectionReaderDescription reader = CollectionReaderFactory.createDescription(
                StepBasedDirXmiCollectionReader.class,
                StepBasedDirXmiCollectionReader.PARAM_PARENT_INPUT_DIR_PATH, parentInputDirName,
                StepBasedDirXmiCollectionReader.PARAM_BASE_INPUT_DIR_NAME, baseInputDirName,
                StepBasedDirXmiCollectionReader.PARAM_INPUT_STEP_NUMBER, stepNumber,
                StepBasedDirXmiCollectionReader.PARAM_FAIL_UNKNOWN, failOnUnkown);

        return reader;
    }

    /**
     * Creates a simple XMI reader with the specified type system, assuming the directory naming
     * convention.
     *
     * @param typeSystemDescription
     * @param parentInputDirName
     * @param baseInputDirName
     * @param stepNumber
     * @param failOnUnkown
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static CollectionReaderDescription createXmiReader(
            TypeSystemDescription typeSystemDescription, String parentInputDirName,
            String baseInputDirName, Integer stepNumber, Boolean failOnUnkown)
            throws ResourceInitializationException {
        // Instantiate a collection reader to get XMI as input.
        CollectionReaderDescription reader = CollectionReaderFactory.createDescription(
                XmiCollectionReader.class, typeSystemDescription,
                XmiCollectionReader.PARAM_PARENT_INPUT_DIR, parentInputDirName,
                XmiCollectionReader.PARAM_BASE_INPUT_DIR_NAME, baseInputDirName,
                XmiCollectionReader.PARAM_STEP_NUMBER, stepNumber,
                XmiCollectionReader.PARAM_FAILUNKNOWN, failOnUnkown);
        return reader;
    }

    /**
     * Create a gzipped XMI reader assuming the directory naming convention.
     *
     * @param parentInputDirName
     * @param baseInputDirName
     * @param stepNumber
     * @param failOnUnkown
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static CollectionReaderDescription createGzippedXmiReader(String parentInputDirName,
                                                                     String baseInputDirName, Integer stepNumber, Boolean failOnUnkown)
            throws ResourceInitializationException {
        // Instantiate a collection reader to get XMI as input.
        CollectionReaderDescription reader = CollectionReaderFactory.createDescription(
                StepBasedDirGzippedXmiCollectionReader.class,
                StepBasedDirGzippedXmiCollectionReader.PARAM_PARENT_INPUT_DIR_PATH, parentInputDirName,
                StepBasedDirGzippedXmiCollectionReader.PARAM_BASE_INPUT_DIR_NAME, baseInputDirName,
                StepBasedDirGzippedXmiCollectionReader.PARAM_INPUT_STEP_NUMBER, stepNumber,
                StepBasedDirGzippedXmiCollectionReader.PARAM_FAIL_UNKNOWN, failOnUnkown);

        return reader;
    }

    /**
     * Create a gzipped XMI reader, with specified type system. Assuming the directory naming
     * convention.
     *
     * @param typeSystemDescription
     * @param parentInputDirName
     * @param baseInputDirName
     * @param stepNumber
     * @param failOnUnkown
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static CollectionReaderDescription createGzippedXmiReader(
            TypeSystemDescription typeSystemDescription, String parentInputDirName,
            String baseInputDirName, Integer stepNumber, Boolean failOnUnkown)
            throws ResourceInitializationException {
        // Instantiate a collection reader to get XMI as input.
        CollectionReaderDescription reader = CollectionReaderFactory.createDescription(
                StepBasedDirGzippedXmiCollectionReader.class, typeSystemDescription,
                StepBasedDirGzippedXmiCollectionReader.PARAM_PARENT_INPUT_DIR_PATH, parentInputDirName,
                StepBasedDirGzippedXmiCollectionReader.PARAM_BASE_INPUT_DIR_NAME, baseInputDirName,
                StepBasedDirGzippedXmiCollectionReader.PARAM_INPUT_STEP_NUMBER, stepNumber,
                StepBasedDirGzippedXmiCollectionReader.PARAM_FAIL_UNKNOWN, failOnUnkown);

        return reader;
    }

    /**
     * Creates a Gzipped XMI reader assuming the directory naming convention
     *
     * @param parentInputDirName
     * @param baseInputDirName
     * @param stepNumber
     * @param failOnUnkown
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static CollectionReaderDescription createGzipXmiReader(String parentInputDirName,
                                                                  String baseInputDirName, Integer stepNumber, Boolean failOnUnkown)
            throws ResourceInitializationException {
        // Instantiate a collection reader to get XMI as input.
        CollectionReaderDescription reader = CollectionReaderFactory.createDescription(
                StepBasedDirGzippedXmiCollectionReader.class,
                StepBasedDirGzippedXmiCollectionReader.PARAM_PARENT_INPUT_DIR_PATH, parentInputDirName,
                StepBasedDirGzippedXmiCollectionReader.PARAM_BASE_INPUT_DIR_NAME, baseInputDirName,
                StepBasedDirGzippedXmiCollectionReader.PARAM_INPUT_STEP_NUMBER, stepNumber,
                StepBasedDirGzippedXmiCollectionReader.PARAM_FAIL_UNKNOWN, failOnUnkown);

        return reader;
    }

    /**
     * Creates a Gzipped XMI reader assuming the directory naming convention
     *
     * @param parentInputDirName
     * @param baseInputDirName
     * @param stepNumber
     * @param failOnUnkown
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static CollectionReaderDescription createGzipXmiReader(
            TypeSystemDescription typeSystemDescription, String parentInputDirName,
            String baseInputDirName, Integer stepNumber, Boolean failOnUnkown)
            throws ResourceInitializationException {
        // Instantiate a collection reader to get XMI as input.
        CollectionReaderDescription reader = CollectionReaderFactory.createDescription(
                StepBasedDirGzippedXmiCollectionReader.class, typeSystemDescription,
                StepBasedDirGzippedXmiCollectionReader.PARAM_PARENT_INPUT_DIR_PATH, parentInputDirName,
                StepBasedDirGzippedXmiCollectionReader.PARAM_BASE_INPUT_DIR_NAME, baseInputDirName,
                StepBasedDirGzippedXmiCollectionReader.PARAM_INPUT_STEP_NUMBER, stepNumber,
                StepBasedDirGzippedXmiCollectionReader.PARAM_FAIL_UNKNOWN, failOnUnkown);

        return reader;
    }

    /**
     * Creates a simple plain text reader for the text under the specified directory.
     *
     * @param inputViewName
     * @param parentInputDirName
     * @param encoding
     * @param textSuffix
     * @return
     * @throws ResourceInitializationException
     */
    public static CollectionReaderDescription createPlainTextReader(String inputViewName,
                                                                    String parentInputDirName, String encoding, String[] textSuffix)
            throws ResourceInitializationException {
        // Instantiate a collection reader to get plain text as input.
        CollectionReaderDescription reader = CollectionReaderFactory.createDescription(
                PlainTextCollectionReader.class, PlainTextCollectionReader.PARAM_INPUT_VIEW_NAME,
                inputViewName, PlainTextCollectionReader.PARAM_INPUTDIR, parentInputDirName,
                PlainTextCollectionReader.PARAM_ENCODING, encoding,
                PlainTextCollectionReader.PARAM_TEXT_SUFFIX, textSuffix);
        return reader;
    }

    public static CollectionReaderDescription createPlainTextReader(String inputViewName,
                                                                    String[] srcDocInfoViewNames, String parentInputDirName, String encoding,
                                                                    String[] textSuffix) throws ResourceInitializationException {
        // Instantiate a collection reader to get plain text as input.
        CollectionReaderDescription reader = CollectionReaderFactory.createDescription(
                PlainTextCollectionReader.class, PlainTextCollectionReader.PARAM_INPUT_VIEW_NAME,
                inputViewName, PlainTextCollectionReader.PARAM_SRC_DOC_INFO_VIEW_NAMES,
                srcDocInfoViewNames, PlainTextCollectionReader.PARAM_INPUTDIR, parentInputDirName,
                PlainTextCollectionReader.PARAM_ENCODING, encoding,
                PlainTextCollectionReader.PARAM_TEXT_SUFFIX, textSuffix);
        return reader;
    }

    /**
     * Creates a simple analysis engine with a specified type system Current it cannot provide wrapper
     * to parameters
     *
     * @param engineClass
     * @param typeSystemDescription
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static <T extends JCasAnnotator_ImplBase> AnalysisEngineDescription createAnalysisEngine(
            Class<T> engineClass, TypeSystemDescription typeSystemDescription)
            throws ResourceInitializationException {
        // Instantiate the analysis engine.
        AnalysisEngineDescription engine = AnalysisEngineFactory.createPrimitiveDescription(
                engineClass, typeSystemDescription);
        return engine;
    }

    public static <T extends JCasAnnotator_ImplBase> AnalysisEngineDescription createAnalysisEngine(
            Class<T> engineClass, TypeSystemDescription typeSystemDescription,
            Object... configurationData) throws ResourceInitializationException {
        // Instantiate the analysis engine.
        return AnalysisEngineFactory.createPrimitiveDescription(
                engineClass, typeSystemDescription, configurationData);
    }

    public static void setTypeSystem(AnalysisEngineDescription coreferenceEngine,
                                     TypeSystemDescription typeSystem) {
        AnalysisEngineMetaData metatData = coreferenceEngine.getAnalysisEngineMetaData();
        metatData.setTypeSystem(typeSystem);
        coreferenceEngine.setMetaData(metatData);
    }

    /**
     * Creates an XMI writer assuming the directory naming convention
     *
     * @param parentOutputDirPath
     * @param baseOutputDirName
     * @param stepNumber
     * @param outputFileSuffix
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static AnalysisEngineDescription createXmiWriter(String parentOutputDirPath,
                                                            String baseOutputDirName, Integer stepNumber, String outputFileSuffix)
            throws ResourceInitializationException {
        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
                StepBasedDirXmiWriter.class, StepBasedDirXmiWriter.PARAM_PARENT_OUTPUT_DIR_PATH,
                parentOutputDirPath, StepBasedDirXmiWriter.PARAM_BASE_OUTPUT_DIR_NAME,
                baseOutputDirName, StepBasedDirXmiWriter.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
                StepBasedDirXmiWriter.PARAM_OUTPUT_STEP_NUMBER, stepNumber);
        return writer;
    }

    /**
     * Creates an XMI writer assuming the directory naming convention
     *
     * @param parentOutputDirPath
     * @param baseOutputDirName
     * @param stepNumber
     * @param outputFileSuffix
     * @param srcDocInfoViewName  the view that contains the source document info
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static AnalysisEngineDescription createXmiWriter(String parentOutputDirPath,
                                                            String baseOutputDirName, Integer stepNumber, String outputFileSuffix,
                                                            String srcDocInfoViewName) throws ResourceInitializationException {
        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
                StepBasedDirXmiWriter.class, StepBasedDirXmiWriter.PARAM_PARENT_OUTPUT_DIR_PATH,
                parentOutputDirPath, StepBasedDirXmiWriter.PARAM_BASE_OUTPUT_DIR_NAME,
                baseOutputDirName, StepBasedDirXmiWriter.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
                StepBasedDirXmiWriter.PARAM_OUTPUT_STEP_NUMBER, stepNumber,
                StepBasedDirXmiWriter.PARAM_SRC_DOC_INFO_VIEW_NAME, srcDocInfoViewName);
        return writer;
    }

    /**
     * Creates an XMI writer assuming the directory naming convention but compress into gzip
     *
     * @param parentOutputDirPath
     * @param baseOutputDirName
     * @param stepNumber
     * @param outputFileSuffix
     * @param srcDocInfoViewName
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static AnalysisEngineDescription createGzipWriter(String parentOutputDirPath,
                                                             String baseOutputDirName, Integer stepNumber, String outputFileSuffix,
                                                             String srcDocInfoViewName) throws ResourceInitializationException {
        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
                StepBasedDirGzippedXmiWriter.class,
                StepBasedDirGzippedXmiWriter.PARAM_PARENT_OUTPUT_DIR_PATH, parentOutputDirPath,
                StepBasedDirGzippedXmiWriter.PARAM_BASE_OUTPUT_DIR_NAME, baseOutputDirName,
                StepBasedDirGzippedXmiWriter.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
                StepBasedDirGzippedXmiWriter.PARAM_OUTPUT_STEP_NUMBER, stepNumber,
                StepBasedDirGzippedXmiWriter.PARAM_SRC_DOC_INFO_VIEW_NAME, srcDocInfoViewName);
        return writer;
    }

    /**
     * Creates an XMI writer assuming the directory naming convention but compress into gzip
     *
     * @param parentOutputDirPath
     * @param baseOutputDirName
     * @param stepNumber
     * @param outputFileSuffix
     * @param srcDocInfoViewName
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static AnalysisEngineDescription createSelectiveGzipWriter(String parentOutputDirPath,
                                                                      String baseOutputDirName, Integer stepNumber, String outputFileSuffix,
                                                                      String srcDocInfoViewName, Set<Integer> outputDocumentNumbers)
            throws ResourceInitializationException {
        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
                StepBasedDirGzippedXmiWriter.class,
                StepBasedDirGzippedXmiWriter.PARAM_PARENT_OUTPUT_DIR_PATH, parentOutputDirPath,
                StepBasedDirGzippedXmiWriter.PARAM_BASE_OUTPUT_DIR_NAME, baseOutputDirName,
                StepBasedDirGzippedXmiWriter.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
                StepBasedDirGzippedXmiWriter.PARAM_OUTPUT_STEP_NUMBER, stepNumber,
                StepBasedDirGzippedXmiWriter.PARAM_SRC_DOC_INFO_VIEW_NAME, srcDocInfoViewName,
                StepBasedDirXmiWriter.PARAM_OUTPUT_FILE_NUMBERS, outputDocumentNumbers);
        return writer;
    }

    /**
     * Creates an XMI writer assuming the directory naming convention and provides an array of indices
     * to select documents to output
     *
     * @param parentOutputDirPath
     * @param baseOutputDirName
     * @param stepNumber
     * @param outputFileSuffix
     * @param outputDocumentNumbers
     * @return
     * @throws ResourceInitializationException
     */
    public static AnalysisEngineDescription createSelectiveXmiWriter(String parentOutputDirPath,
                                                                     String baseOutputDirName, Integer stepNumber, String outputFileSuffix,
                                                                     Set<Integer> outputDocumentNumbers) throws ResourceInitializationException {
        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
                StepBasedDirXmiWriter.class, StepBasedDirXmiWriter.PARAM_PARENT_OUTPUT_DIR_PATH,
                parentOutputDirPath, StepBasedDirXmiWriter.PARAM_BASE_OUTPUT_DIR_NAME,
                baseOutputDirName, StepBasedDirXmiWriter.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
                StepBasedDirXmiWriter.PARAM_OUTPUT_STEP_NUMBER, stepNumber,
                StepBasedDirXmiWriter.PARAM_OUTPUT_FILE_NUMBERS, outputDocumentNumbers);
        return writer;
    }

    /**
     * Creates a gzipped XMI writer without specifying a particular output view.
     *
     * @param parentOutputDirPath
     * @param baseOutputDirName
     * @param stepNumber
     * @param outputFileSuffix
     * @return
     * @throws ResourceInitializationException
     */
    public static AnalysisEngineDescription createGzippedXmiWriter(String parentOutputDirPath,
                                                                   String baseOutputDirName, Integer stepNumber, String outputFileSuffix)
            throws ResourceInitializationException {
        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
                StepBasedDirGzippedXmiWriter.class,
                StepBasedDirGzippedXmiWriter.PARAM_PARENT_OUTPUT_DIR_PATH, parentOutputDirPath,
                StepBasedDirGzippedXmiWriter.PARAM_BASE_OUTPUT_DIR_NAME, baseOutputDirName,
                StepBasedDirGzippedXmiWriter.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
                StepBasedDirGzippedXmiWriter.PARAM_OUTPUT_STEP_NUMBER, stepNumber);
        return writer;
    }

    /**
     * Creates a gzipped XMI writer while specifying an output view.
     *
     * @param parentOutputDirPath
     * @param baseOutputDirName
     * @param stepNumber
     * @param outputFileSuffix
     * @param srcDocInfoViewName
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static AnalysisEngineDescription createGzippedXmiWriter(String parentOutputDirPath,
                                                                   String baseOutputDirName, Integer stepNumber, String outputFileSuffix,
                                                                   String srcDocInfoViewName) throws ResourceInitializationException {
        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
                StepBasedDirGzippedXmiWriter.class,
                StepBasedDirGzippedXmiWriter.PARAM_PARENT_OUTPUT_DIR_PATH, parentOutputDirPath,
                StepBasedDirGzippedXmiWriter.PARAM_BASE_OUTPUT_DIR_NAME, baseOutputDirName,
                StepBasedDirGzippedXmiWriter.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
                StepBasedDirGzippedXmiWriter.PARAM_OUTPUT_STEP_NUMBER, stepNumber,
                StepBasedDirGzippedXmiWriter.PARAM_SRC_DOC_INFO_VIEW_NAME, srcDocInfoViewName);
        return writer;
    }

    public static AnalysisEngineDescription createSelectiveGzippedXmiWriter(
            String parentOutputDirPath, String baseOutputDirName, Integer stepNumber,
            String outputFileSuffix, Set<Integer> outputDocumentNumbers)
            throws ResourceInitializationException {
        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
                StepBasedDirGzippedXmiWriter.class,
                StepBasedDirGzippedXmiWriter.PARAM_PARENT_OUTPUT_DIR_PATH, parentOutputDirPath,
                StepBasedDirGzippedXmiWriter.PARAM_BASE_OUTPUT_DIR_NAME, baseOutputDirName,
                StepBasedDirGzippedXmiWriter.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
                StepBasedDirGzippedXmiWriter.PARAM_OUTPUT_STEP_NUMBER, stepNumber,
                StepBasedDirGzippedXmiWriter.PARAM_OUTPUT_FILE_NUMBERS, outputDocumentNumbers);
        return writer;
    }

//    /**
//     * Creates a plain text writer.
//     *
//     * @param parentOutputDirPath
//     * @param baseOutputDirName
//     * @param stepNumber
//     * @param outputFileSuffix
//     * @param viewName
//     * @return
//     * @throws org.apache.uima.resource.ResourceInitializationException
//     */
//    public static AnalysisEngineDescription createPlainTextWriter(String parentOutputDirPath,
//                                                                  String baseOutputDirName, Integer stepNumber, String outputFileSuffix, String viewName)
//            throws ResourceInitializationException {
//        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
//                StepBasedDirPlainTextWriter.class,
//                StepBasedDirPlainTextWriter.PARAM_PARENT_OUTPUT_DIR_PATH, parentOutputDirPath,
//                StepBasedDirPlainTextWriter.PARAM_BASE_OUTPUT_DIR_NAME, baseOutputDirName,
//                StepBasedDirPlainTextWriter.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
//                StepBasedDirPlainTextWriter.PARAM_OUTPUT_STEP_NUMBER, stepNumber,
//                StepBasedDirPlainTextWriter.PARAM_SRC_DOC_INFO_VIEW_NAME, viewName);
//        return writer;
//    }

//    /**
//     * Creates a plain text aggregator.
//     *
//     * @param parentOutputDirPath
//     * @param baseOutputDirName
//     * @param stepNumber
//     * @param outputFileSuffix
//     * @param outputFileName
//     * @return
//     * @throws org.apache.uima.resource.ResourceInitializationException
//     */
//    public static AnalysisEngineDescription createPlainTextAggregator(String parentOutputDirPath,
//                                                                      String baseOutputDirName, Integer stepNumber, String outputFileSuffix,
//                                                                      String outputFileName) throws ResourceInitializationException {
//        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
//                StepBasedDirPlainTextAggregator.class,
//                StepBasedDirPlainTextAggregator.PARAM_PARENT_OUTPUT_DIR_PATH, parentOutputDirPath,
//                StepBasedDirPlainTextAggregator.PARAM_BASE_OUTPUT_DIR_NAME, baseOutputDirName,
//                StepBasedDirPlainTextAggregator.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
//                StepBasedDirPlainTextAggregator.PARAM_OUTPUT_STEP_NUMBER, stepNumber,
//                StepBasedDirPlainTextAggregator.PARAM_OUTPUT_FILE_NAME, outputFileName);
//        return writer;
//    }

    /**
     * Creates a plain text writer.
     *
     * @param parentOutputDirPath
     * @param baseOutputDirName
     * @param stepNumber
     * @param outputFileSuffix
     * @param viewName
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static AnalysisEngineDescription createDocumentTextWriter(String parentOutputDirPath,
                                                                     String baseOutputDirName, Integer stepNumber, String outputFileSuffix, String viewName)
            throws ResourceInitializationException {
        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
                DocumentTextWriter.class, DocumentTextWriter.PARAM_PARENT_OUTPUT_DIR,
                parentOutputDirPath, DocumentTextWriter.PARAM_BASE_OUTPUT_DIR_NAME, baseOutputDirName,
                DocumentTextWriter.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
                DocumentTextWriter.PARAM_STEP_NUMBER, stepNumber,
                DocumentTextWriter.PARAM_SOURCE_DOCUMENT_INFO_VIEW_NAME, viewName);
        return writer;
    }

    /**
     * Create a description for subclasses of AbstractCustomizedTextWriterAnalsysisEngine. However, if
     * you need to use additional parameters, this method cannot help you.
     *
     * @param writerClass
     * @param typeSystemDescription
     * @param parentOutputDirPath
     * @param baseOutputDirName
     * @param stepNumber
     * @param outputFileSuffix
     * @param sourceDocumentViewName
     * @param <T>
     * @return
     * @throws ResourceInitializationException
     */
    public static <T extends AbstractCustomizedTextWriterAnalsysisEngine> AnalysisEngineDescription createCustomizedTextWriter(
            Class<T> writerClass, TypeSystemDescription typeSystemDescription,
            String parentOutputDirPath, String baseOutputDirName, Integer stepNumber,
            String outputFileSuffix, String sourceDocumentViewName)
            throws ResourceInitializationException {
        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
                writerClass, typeSystemDescription,
                AbstractCustomizedTextWriterAnalsysisEngine.PARAM_PARENT_OUTPUT_DIR,
                parentOutputDirPath,
                AbstractCustomizedTextWriterAnalsysisEngine.PARAM_BASE_OUTPUT_DIR_NAME,
                baseOutputDirName,
                AbstractCustomizedTextWriterAnalsysisEngine.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
                AbstractCustomizedTextWriterAnalsysisEngine.PARAM_STEP_NUMBER, stepNumber,
                AbstractCustomizedTextWriterAnalsysisEngine.PARAM_SOURCE_DOCUMENT_INFO_VIEW_NAME,
                sourceDocumentViewName);
        return writer;
    }

    /**
     * Create a description for subclasses of AbstractCsvWriterAnalysisEngine. However, if you need to
     * use additional parameters, this method cannot help you.
     *
     * @param writerClass
     * @param typeSystemDescription
     * @param parentOutputDirPath
     * @param baseOutputDirName
     * @param stepNumber
     * @param outputFileSuffix
     * @param sourceDocumentInfoViewName
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static <T extends AbstractCsvWriterAnalysisEngine> AnalysisEngineDescription createCustomizedCsvWriter(
            Class<T> writerClass, TypeSystemDescription typeSystemDescription,
            String parentOutputDirPath, String baseOutputDirName, Integer stepNumber,
            String outputFileSuffix, String sourceDocumentInfoViewName)
            throws ResourceInitializationException {
        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
                writerClass, typeSystemDescription,
                AbstractCsvWriterAnalysisEngine.PARAM_PARENT_OUTPUT_DIR, parentOutputDirPath,
                AbstractCsvWriterAnalysisEngine.PARAM_BASE_OUTPUT_DIR_NAME, baseOutputDirName,
                AbstractCsvWriterAnalysisEngine.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
                AbstractCsvWriterAnalysisEngine.PARAM_STEP_NUMBER, stepNumber,
                AbstractCsvWriterAnalysisEngine.PARAM_SOURCE_DOCUMENT_INFO_VIEW_NAME,
                sourceDocumentInfoViewName);

        return writer;
    }

    /**
     * Creates a customized plain text aggregator.
     *
     * @param writerClass
     * @param typeSystemDescription
     * @param outputFilePath
     * @param <T>
     * @return
     * @throws ResourceInitializationException
     */
    public static <T extends AbstractPlainTextAggregator> AnalysisEngineDescription createCustomPlainTextAggregator(
            Class<T> writerClass, TypeSystemDescription typeSystemDescription, String outputFilePath)
            throws ResourceInitializationException {
        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
                writerClass, typeSystemDescription, AbstractPlainTextAggregator.PARAM_OUTPUT_FILE_PATH,
                outputFilePath);

        return writer;
    }

    /**
     * Creates a plain text aggregator.
     *
     * @param parentOutputDirPath
     * @param baseOutputDirName
     * @param stepNumber
     * @param outputFileSuffix
     * @param outputFileName
     * @return
     * @throws org.apache.uima.resource.ResourceInitializationException
     */
    public static <T extends AbstractStepBasedDirPlainTextAggregator> AnalysisEngineDescription createCustomPlainTextAggregator(Class<T> writerClass,
                                                                                                                                TypeSystemDescription typeSystemDescription, String parentOutputDirPath,
                                                                                                                                String baseOutputDirName, Integer stepNumber, String outputFileSuffix,
                                                                                                                                String outputFileName) throws ResourceInitializationException {
        AnalysisEngineDescription writer = AnalysisEngineFactory.createPrimitiveDescription(
                writerClass, typeSystemDescription,
                AbstractStepBasedDirPlainTextAggregator.PARAM_PARENT_OUTPUT_DIR_PATH, parentOutputDirPath,
                AbstractStepBasedDirPlainTextAggregator.PARAM_BASE_OUTPUT_DIR_NAME, baseOutputDirName,
                AbstractStepBasedDirPlainTextAggregator.PARAM_OUTPUT_FILE_SUFFIX, outputFileSuffix,
                AbstractStepBasedDirPlainTextAggregator.PARAM_OUTPUT_STEP_NUMBER, stepNumber,
                AbstractStepBasedDirPlainTextAggregator.PARAM_OUTPUT_FILE_NAME, outputFileName);
        return writer;
    }

}
