package edu.cmu.lti.event_coref.analysis_engine;

import com.google.common.collect.ArrayListMultimap;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tratz.parse.FullSystemWrapper;
import tratz.parse.FullSystemWrapper.FullSystemResult;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;

import java.io.File;
import java.util.*;

/**
 * Runs FANSE parser, and annotate associated types.
 */
public class FanseAnnotator extends JCasAnnotator_ImplBase {

    public static final String PARAM_MODEL_BASE_DIR = "modelBaseDirectory";
    private static final Logger logger = LoggerFactory.getLogger(FanseAnnotator.class);

    @ConfigurationParameter(name = PARAM_MODEL_BASE_DIR)
    private String modeBaseDir;

    // these file are assume existence in the base directory
    private static final String POS_MODEL = "posTaggingModel.gz",
            PARSE_MODEL = "parseModel.gz",
            POSSESSIVES_MODEL = "possessivesModel.gz",
            NOUN_COMPOUND_MODEL = "nnModel.gz",
            SRL_ARGS_MODELS = "srlArgsWrapper.gz",
            SRL_PREDICATE_MODELS = "srlPredWrapper.gz",
            PREPOSITION_MODELS = "psdModels.gz", WORDNET = "data/wordnet3";

    public final static Boolean DEFAULT_VCH_CONVERT = Boolean.FALSE;

    public final static String DEFAULT_SENTENCE_READER_CLASS = tratz.parse.io.ConllxSentenceReader.class
            .getName();

    FullSystemWrapper fullSystemWrapper = null;

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException {
        super.initialize(aContext);
        try {
            fullSystemWrapper = new FullSystemWrapper(
                    joinPath(modeBaseDir, PREPOSITION_MODELS),
                    joinPath(modeBaseDir, NOUN_COMPOUND_MODEL),
                    joinPath(modeBaseDir, POSSESSIVES_MODEL),
                    joinPath(modeBaseDir, SRL_ARGS_MODELS),
                    joinPath(modeBaseDir, SRL_PREDICATE_MODELS),
                    joinPath(modeBaseDir, POS_MODEL),
                    joinPath(modeBaseDir, PARSE_MODEL),
                    joinPath(modeBaseDir, WORDNET)
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResourceInitializationException();
        }
    }


    private String joinPath(String d, String f) {
        return new File(d, f).getAbsolutePath();
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        UimaConvenience.printProcessLog(aJCas, logger);

        Collection<Sentence> sentList = JCasUtil.select(aJCas, Sentence.class);

        for (Sentence sent : sentList) {
//            System.out.println("Parsing [" + sent.getCoveredText() + "]");
//            System.out.println(sent.getBegin() + " " + sent.getEnd());
            List<Word> wordList = JCasUtil.selectCovered(Word.class, sent);

            if (wordList.size() < 1) {
                logger.error("Sentence does not contain any words! Please check data.");
            }


            Parse par = wordListToParse(wordList);
            tratz.parse.types.Sentence fSent = par.getSentence();
            List<Token> tokens = fSent.getTokens();

            FullSystemResult result = fullSystemWrapper.process(fSent,
                    tokens.size() > 0 && tokens.get(0).getPos() == null, true,
                    true, true, true, true);

            Parse dependencyParse = result.getParse();
            Parse semanticParse = result.getSrlParse();

            tratz.parse.types.Sentence resultSent = dependencyParse
                    .getSentence();
            List<Token> resultTokens = resultSent.getTokens();

            // get Token annotation and convert them to UIMA
            Map<Token, FanseToken> Fanse2UimaMap = new HashMap<Token, FanseToken>();
            int tokenId = 1;
            for (Token token : resultTokens) {
                Word goldStandardToken = wordList.get(token.getIndex() - 1);
                int begin = goldStandardToken.getBegin();
                int end = goldStandardToken.getEnd();
                FanseToken fToken = new FanseToken(aJCas, begin, end);
                fToken.setTokenId(tokenId);
                fToken.setCoarsePos(token.getCoarsePos());
                fToken.setPos(token.getPos());
                fToken.setLexicalSense(token.getLexSense());
//                fToken.addToIndexes();
                tokenId++;

                Fanse2UimaMap.put(token, fToken);
            }

            // now create depedency edges of these nodes
            ArrayListMultimap<FanseToken, FanseDependencyRelation> dependencyHeadRelationMap = ArrayListMultimap
                    .create();
            ArrayListMultimap<FanseToken, FanseDependencyRelation> dependencyChildRelationMap = ArrayListMultimap
                    .create();

            for (Arc arc : dependencyParse.getArcs()) {
                if (arc == null) {
                    continue;
                }

                FanseToken childToken = Fanse2UimaMap.get(arc.getChild());
                FanseToken headToken = Fanse2UimaMap.get(arc.getHead());

                if (childToken != null || headToken != null) {
                    FanseDependencyRelation fArc = new FanseDependencyRelation(
                            aJCas);
                    fArc.setHead(headToken);
                    fArc.setChild(childToken);
                    fArc.setDependency(arc.getDependency());

                    dependencyHeadRelationMap.put(childToken, fArc);
                    dependencyChildRelationMap.put(headToken, fArc);

                    fArc.addToIndexes(aJCas);
                }
            }

            // now creat semantic edges of these nodes
            ArrayListMultimap<FanseToken, FanseSemanticRelation> semanticHeadRelationMap = ArrayListMultimap
                    .create();
            ArrayListMultimap<FanseToken, FanseSemanticRelation> semanticChildRelationMap = ArrayListMultimap
                    .create();

            for (Arc arc : semanticParse.getArcs()) {
                if (arc == null || arc.getSemanticAnnotation() == null) {
                    continue;
                }

                FanseToken childToken = Fanse2UimaMap.get(arc.getChild());
                FanseToken headToken = Fanse2UimaMap.get(arc.getHead());

                if (childToken != null || headToken != null) {
                    FanseSemanticRelation fArc = new FanseSemanticRelation(
                            aJCas);
                    fArc.setHead(headToken);
                    fArc.setChild(childToken);
                    fArc.setSemanticAnnotation(arc.getSemanticAnnotation());

                    semanticHeadRelationMap.put(childToken, fArc);
                    semanticChildRelationMap.put(headToken, fArc);

                    fArc.addToIndexes(aJCas);
                }
            }

            // associate token annotation with arc
            for (FanseToken fToken : Fanse2UimaMap.values()) {
                if (dependencyHeadRelationMap.containsKey(fToken)) {
                    fToken.setHeadDependencyRelations(FSCollectionFactory
                            .createFSList(aJCas,
                                    dependencyHeadRelationMap.get(fToken)));
                }
                if (dependencyChildRelationMap.containsKey(fToken)) {
                    fToken.setChildDependencyRelations(FSCollectionFactory
                            .createFSList(aJCas,
                                    dependencyChildRelationMap.get(fToken)));
                }
                if (semanticHeadRelationMap.containsKey(fToken)) {
                    fToken.setHeadSemanticRelations(FSCollectionFactory
                            .createFSList(aJCas,
                                    semanticHeadRelationMap.get(fToken)));
                }
                if (semanticChildRelationMap.containsKey(fToken)) {
                    fToken.setChildSemanticRelations(FSCollectionFactory
                            .createFSList(aJCas,
                                    semanticChildRelationMap.get(fToken)));
                }

                fToken.addToIndexes(aJCas);
            }
        }
    }

    private Parse wordListToParse(List<Word> words) {
        Token root = new Token("[ROOT]", 0);
        List<Token> tokens = new ArrayList<Token>();
        List<Arc> arcs = new ArrayList<Arc>();

        int tokenNum = 0;
        for (Word word : words) {
            tokenNum++;
            String wordString = word.getCoveredText();
            Token token = new Token(wordString, tokenNum);
            tokens.add(token);
        }

        return new Parse(new tratz.parse.types.Sentence(tokens), root,
                arcs);
    }

}
