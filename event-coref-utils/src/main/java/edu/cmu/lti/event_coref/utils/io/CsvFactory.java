package edu.cmu.lti.event_coref.utils.io;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;

/**
 * A CSVUtil based on open CSV
 * 
 * @author Zhengzhong Liu, Hector
 * 
 */
public class CsvFactory {
  public static CSVWriter getCSVWriter(String outputPath) {
    File file = new File(outputPath);
    return getCSVWriter(file);
  }

  public static CSVWriter getCSVWriter(String outputPath, char sep, char escape) {
    File file = new File(outputPath);
    return getCSVWriter(file, sep, escape);
  }

  public static CSVWriter getCSVWriter(File file) {
    CSVWriter csvWriter = null;

    if (file.exists()) {
      file.delete();
    }

    try {
      file.createNewFile();
      FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
      csvWriter = new CSVWriter(fileWriter);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return csvWriter;
  }

  public static CSVWriter getCSVWriter(File file, char sep, char escape) {
    CSVWriter csvWriter = null;

    if (file.exists()) {
      file.delete();
    }

    try {
      file.createNewFile();
      FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
      csvWriter = new CSVWriter(fileWriter, sep, escape);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return csvWriter;
  }

  public static CSVReader getCSVReader(String inputPath) {
    File file = new File(inputPath);
    return getCSVReader(file);
  }

  public static CSVReader getCSVReader(String inputPath, char sep, char escape) {
    File file = new File(inputPath);
    return getCSVReader(file, sep, escape);
  }

  public static CSVReader getCSVReader(File file) {
    CSVReader reader = null;
    try {
      reader = new CSVReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return reader;
  }

  public static CSVReader getCSVReader(File file, char sep, char escape) {
    CSVReader reader = null;
    try {
      reader = new CSVReader(new FileReader(file), sep, escape);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return reader;
  }

}
