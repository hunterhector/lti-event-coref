package edu.cmu.lti.event_coref.analysis_engine;

import edu.cmu.cs.lti.ark.fn.data.prep.formats.Token;
import edu.cmu.cs.lti.ark.fn.parsing.SemaforParseResult;
import edu.cmu.cs.lti.ark.fn.pipeline.SemaforFullPipeline;
import edu.cmu.cs.lti.ark.fn.pipeline.parsing.ParsingException;
import edu.cmu.lti.event_coref.type.*;
import edu.cmu.lti.utils.annotator.AbstractLoggingAnnotator;
import edu.cmu.lti.utils.uima.UimaAnnotationUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.maltparser.core.exception.MaltChainedException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Required Stanford Corenlp ssplit, tokenize, pos, lemma
 * <p/>
 * Created with IntelliJ IDEA.
 *
 * @author Zhengzhong Liu
 *         Date: 1/23/15
 *         Time: 11:01 PM
 */
public class SemaforAnnotator extends AbstractLoggingAnnotator {

    public static final String SEMAFOR_MODEL_PATH = "modelPath";

    public static final String COMPONENT_ID = SemaforAnnotator.class.getSimpleName();

    SemaforFullPipeline semafor;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        File semaforModelDir = new File((String) aContext.getConfigParameterValue(SEMAFOR_MODEL_PATH));

        try {
            logger.info(String.format("Initializing from model : %s", semaforModelDir.getCanonicalPath()));
            semafor = new SemaforFullPipeline(semaforModelDir);
        } catch (IOException | URISyntaxException | ClassNotFoundException | MaltChainedException e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        annotateSemafor(aJCas);
        for (JCas view : getAdditionalViews(aJCas)) {
            annotateSemafor(view);
        }
    }


    private void annotateSemafor(JCas aJCas) {
        for (Sentence sentence : JCasUtil.select(aJCas, Sentence.class)) {
            List<Token> semaforTokens = new ArrayList<>();

            List<StanfordCorenlpToken> words = JCasUtil.selectCovered(StanfordCorenlpToken.class, sentence);

            for (int i = 0; i < words.size(); i++) {
                StanfordCorenlpToken word = words.get(i);
                semaforTokens.add(new Token(i + 1, word.getCoveredText(), word.getLemma(), null, word.getPos(), null, null, null, null, null));
            }

            try {
                SemaforParseResult result = semafor.parse(semaforTokens);
                annotateSemaforSentence(aJCas, sentence, result);
            } catch (ParsingException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void annotateSemaforSentence(JCas aJCas, Sentence sentence, SemaforParseResult result) {
        List<StanfordCorenlpToken> words = JCasUtil.selectCovered(StanfordCorenlpToken.class, sentence);
        int frameId = 0;

        for (SemaforParseResult.Frame frame : result.frames) {
            SemaforAnnotationSet annotationSet = new SemaforAnnotationSet(aJCas, sentence.getBegin(), sentence.getEnd());
            UimaAnnotationUtils.finishAnnotation(annotationSet, COMPONENT_ID, frameId, aJCas);
            frameId++;

            SemaforParseResult.Frame.NamedSpanSet target = frame.target;
            annotationSet.setFrameName(target.name);
            List<SemaforLayer> layers = new ArrayList<>();

            SemaforLabel targetLabel = namedSpan2Label(aJCas, words, target, "Target");
            SemaforLayer targetLayer = new SemaforLayer(aJCas, targetLabel.getBegin(), targetLabel.getEnd());
            targetLayer.setName("Target");
            FSArray targetLabelArray = new FSArray(aJCas, 1);
            targetLabelArray.set(0, targetLabel);
            targetLayer.setLabels(targetLabelArray);
            layers.add(targetLayer);


            int roleId = 0;
            for (SemaforParseResult.Frame.ScoredRoleAssignment roleAssignment : frame.annotationSets) {
                SemaforLayer layer = new SemaforLayer(aJCas);
                UimaAnnotationUtils.finishAnnotation(layer, COMPONENT_ID, roleId, aJCas);
                roleId++;

                int rank = roleAssignment.rank;
                double score = roleAssignment.score;

                layer.setName("FE");
                layer.setRank(rank);
//                layer.setScore(score);
                layers.add(layer);

                List<SemaforLabel> labels = new ArrayList<>();

                for (SemaforParseResult.Frame.NamedSpanSet frameElement : roleAssignment.frameElements) {
                    labels.add(namedSpan2Label(aJCas, words, frameElement, null));
                }
                layer.setLabels(FSCollectionFactory.createFSArray(aJCas, labels));
            }
            annotationSet.setLayers(FSCollectionFactory.createFSArray(aJCas, layers));
        }
    }

    private SemaforLabel namedSpan2Label(JCas aJCas, List<StanfordCorenlpToken> words, SemaforParseResult.Frame.NamedSpanSet namedSpanSet, String name) {
        //assume only continous span is predicted, so return only one label
        int first = -1;
        int last = -1;
        for (SemaforParseResult.Frame.Span span : namedSpanSet.spans) {
            if (first == -1 || span.start < first) {
                first = span.start;
            }
            if (last == -1 || span.end > last) {
                last = span.end - 1;
            }
        }

        SemaforLabel label = new SemaforLabel(aJCas, words.get(first).getBegin(), words.get(last).getEnd());
        if (name == null) {
            label.setName(namedSpanSet.name);
        } else {
            label.setName(name);
        }
        UimaAnnotationUtils.finishAnnotation(label, COMPONENT_ID, 0, aJCas);
        return label;
    }
}
