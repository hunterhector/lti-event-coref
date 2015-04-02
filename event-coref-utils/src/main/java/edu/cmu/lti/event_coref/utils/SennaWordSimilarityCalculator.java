package edu.cmu.lti.event_coref.utils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SennaWordSimilarityCalculator {
  Map<String, List<Double>> wordVectors;

    private static final Logger logger = LoggerFactory.getLogger(SennaWordSimilarityCalculator.class);

    public SennaWordSimilarityCalculator(String embeddingsPath, String wordsPath) {
    logger.info("Preparing Senna Words...");
        
    File embeddingsFile = new File(embeddingsPath);
    File wordsFile = new File(wordsPath);

    List<String> words = new ArrayList<String>();
    wordVectors = new HashMap<String, List<Double>>();

    BufferedReader wordsIn;
    try {
      wordsIn = new BufferedReader(new FileReader(wordsFile));

      while (wordsIn.ready()) {
        String word = wordsIn.readLine().trim();
        words.add(word);
      }

      wordsIn.close();

      BufferedReader embeddingsIn = new BufferedReader(new FileReader(embeddingsFile));

      int count = 0;
      while (embeddingsIn.ready()) {
        String embeddings = embeddingsIn.readLine();
        List<String> embeddingElements = Arrays.asList(embeddings.split(" "));
        List<Double> embeddingVector = Lists.transform(embeddingElements,
                new Function<String, Double>() {
                  @Override
                  public Double apply(String from) {
                    return Double.parseDouble(from);
                  }
                });
        wordVectors.put(words.get(count), embeddingVector);
        count ++;
      }
      embeddingsIn.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    logger.info("Done preparing Senna words.");
  }

  public Double getCosineSimilarity(String lemma1, String lemma2) {
    if (!(wordVectors.containsKey(lemma1) && wordVectors.containsKey(lemma2))) {
      return -2.0; // indicating faliure
    }
    List<Double> vector1 = wordVectors.get(lemma1);
    List<Double> vector2 = wordVectors.get(lemma2);

    double v1l2 = 0.0;
    double v2l2 = 0.0;
    double dotProd = 0.0;
    for (int i = 0; i < vector1.size(); i++) {
      double v1 = vector1.get(i);
      double v2 = vector2.get(i);

      dotProd += v1 * v2;
      v1l2 += v1 * v1;
      v2l2 += v2 * v2;
    }
    return dotProd / Math.sqrt(v1l2 * v2l2);
  }

  public static void main(String[] args) {
    // test the calculator
      SennaWordSimilarityCalculator senna = new SennaWordSimilarityCalculator(
              "resources/senna/embeddings.txt", "resources/senna/words.lst");

      Scanner in = new Scanner(System.in);
      String s;
      while (true){
        System.out.println("Enter word1 :");
        String word1 = in.nextLine().trim().toLowerCase();
        System.out.println("Enter word2 :");
        String word2 = in.nextLine().trim().toLowerCase();
        
        System.out.println("Similarity is " + senna.getCosineSimilarity(word1,word2));
      }
//     System.out.println( senna.getCosineSimilarity("fight", "battle"));
//     System.out.println( senna.getCosineSimilarity("gunbattle", "shootout"));
//     System.out.println( senna.getCosineSimilarity("attack","position"));
//     System.out.println( senna.getCosineSimilarity("capture", "attack"));
  }

}
