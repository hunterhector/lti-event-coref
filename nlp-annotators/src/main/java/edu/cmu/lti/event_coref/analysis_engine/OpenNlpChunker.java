package edu.cmu.lti.event_coref.analysis_engine;

import edu.cmu.lti.event_coref.type.OpennlpChunk;
import edu.cmu.lti.event_coref.type.Sentence;
import edu.cmu.lti.event_coref.type.Word;
import edu.cmu.lti.utils.uima.UimaConvenience;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Annotates chunks provided by OpenNLP.
 * 
 * Required annotation: sentence
 */
public class OpenNlpChunker extends JCasAnnotator_ImplBase {
  ChunkerME chunker;

  public static final String PARAM_MODEL_PATH = "chunkerModel";

  @ConfigurationParameter(name = PARAM_MODEL_PATH)
  private String modelFilePath;

  private static final Logger logger = LoggerFactory.getLogger(OpenNlpChunker.class);

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    InputStream modelIn = null;
    ChunkerModel model = null;

    try {
      modelIn = new FileInputStream(modelFilePath);
      model = new ChunkerModel(modelIn);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (modelIn != null) {
        try {
          modelIn.close();
        } catch (IOException e) {
        }
      }
    }

    chunker = new ChunkerME(model);
  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    UimaConvenience.printProcessLog(aJCas, logger);

    // List<Sentence> sentences = UimaConvenience.getAnnotationList(aJCas, Sentence.class);
    List<Sentence> sentences = UimaConvenience.getAnnotationList(aJCas, Sentence.class);
    // for (Sentence sent : sentences) {
    for (Sentence sent : sentences) {
      List<Word> words = JCasUtil.selectCovered(Word.class, sent);

      String tokens[] = new String[words.size()];
      String pos[] = new String[words.size()];

      for (int i = 0; i < words.size(); i++) {
        tokens[i] = words.get(i).getCoveredText();
        pos[i] = words.get(i).getPartOfSpeech();
      }

      String tags[] = chunker.chunk(tokens, pos);

      tag2Uima(aJCas, words, tags);
    }
  }

  private void tag2Uima(JCas aJCas, List<Word> words, String[] tags) {
    int begin = -1;
    int end = -1;

    String previousType = "";

    for (int i = 0; i < words.size(); i++) {
      String tag = tags[i];
      Word word = words.get(i);

      String loc = "";
      String type = "";
      String[] tagParts = tag.split("-");

      if (tagParts.length == 2) {
        loc = tagParts[0];
        type = tagParts[1];
      } else {// otherwise is "O"
        loc = tag;
        type = tag;
      }
      if (loc.equals("O")) {
        if (begin != -1) {
          createChunk(aJCas, begin, end, previousType);
        }
        begin = word.getBegin();
        end = word.getEnd();
      }
      if (loc.equals("B")) {
        if (begin != -1) {
          createChunk(aJCas, begin, end, previousType);
        }
        begin = word.getBegin();
        end = word.getEnd();
      }

      if (loc.equals("I")) {
        end = word.getEnd();
      }

      if (i == words.size() - 1) {
        createChunk(aJCas, begin, end, type);
      }

      previousType = type;
    }

  }

  private void createChunk(JCas aJCas, int begin, int end, String tag) {
    OpennlpChunk chunk = new OpennlpChunk(aJCas);
    chunk.setBegin(begin);
    chunk.setEnd(end);
    chunk.setTag(tag);
    chunk.addToIndexes(aJCas);
  }

}
