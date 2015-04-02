package edu.cmu.lti.event_coref.utils.ml;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class WekaUtils {

  public static Instances readArffData(String fileName, WekaFeatureFactory sampleFactory)
          throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader(fileName));
    Instances data = new Instances(reader);
    reader.close();
    // Instances filteredDataSet = sampleFactory.filterDataset(data);
    // filteredDataSet.setClassIndex(filteredDataSet.numAttributes() - 1);
    // return filteredDataSet;

    data.setClassIndex(data.numAttributes() - 1);
    return data;
  }

  public static WekaClassifierWrapper loadWrapper(String modelPath) {
    WekaClassifierWrapper wrapper = null;
    try {
      wrapper = (WekaClassifierWrapper) SerializationHelper.read(modelPath);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return wrapper;
  }

  /**
   * Direct eval call the Evaluation package of Weka, here we cannot control the behavior of
   * classifier, good to be used to get a big picture though. Weka will use some default training
   * stuff
   * 
   * @param cls
   * @throws Exception
   */
  public static void directEval(Classifier cls, Instances trainingData, Instances testingData)
          throws Exception {
    Evaluation eval = new Evaluation(trainingData);

    eval.evaluateModel(cls, testingData);

    System.out.println(eval.toSummaryString("\nResults\n======\n", false));
  }

  /**
   * Simply output prediction result as Arff (last column is prediction)
   * 
   * @param outputFileName
   * @throws IOException
   */
  public static void saveArff(String outputFileName, Instances prediction) throws IOException {
    ArffSaver saver = new ArffSaver();
    saver.setInstances(prediction);
    try {
      saver.setFile(new File(outputFileName));
      saver.writeBatch();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Reads the specified data in the Weka format (e.g., ARFF), and return instances.
   * 
   * @param dataFilePath
   * @return
   */
  public static Instances readData(String dataFilePath) {
    try {
      Instances dataset = DataSource.read(dataFilePath);
      dataset.setClassIndex(dataset.numAttributes() - 1);
      return dataset;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Prints out options of the specified classifier.
   * 
   * @param cls
   */
  public static void printOptions(Classifier cls) {
    System.out.print("Classifier options: ");
    for (int j = 0; j < cls.getOptions().length; j++) {
      if (j != 0) {
        System.out.print(" ");
      }
      System.out.print(cls.getOptions()[j]);
    }
    System.out.println("");
  }

}
