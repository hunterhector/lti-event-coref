package edu.cmu.lti.event_coref.analysis_engine.features;

import edu.cmu.lti.event_coref.io.AbstractPlainTextAggregator;
import edu.cmu.lti.event_coref.type.PairwiseEventCoreferenceEvaluation;
import edu.cmu.lti.event_coref.type.PairwiseEventFeature;
import edu.cmu.lti.event_coref.utils.ConstantUtils;
import edu.cmu.lti.event_coref.utils.ml.Feature;
import edu.cmu.lti.event_coref.utils.ml.FeatureConstants.FeatureType;
import edu.cmu.lti.event_coref.utils.ml.FeatureSet;
import edu.cmu.lti.utils.uima.UimaConvenience;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import java.util.List;

/**
 * This is a simple writer to generate a sorted list of all distinct feature names.
 * <p/>
 * Prerequisite annotation: PairwiseEventCoreferenceEvaluation
 *
 * @author Jun Araki
 */
public class FeatureListGenerator extends AbstractPlainTextAggregator {
    public static final String PARAM_TARGET_PECE_COMPONENT_ID = "targetPeceComponentId";
    private static final Logger logger = LoggerFactory.getLogger(FeatureListGenerator.class);

    @ConfigurationParameter(name = PARAM_TARGET_PECE_COMPONENT_ID, mandatory = true)
    private String targetPeceComponentId;

    private FeatureSet featureSet;

    @Override
    public void subInitialize(UimaContext context) {
        featureSet = new FeatureSet();
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        UimaConvenience.printProcessLog(aJCas, logger);

        for (PairwiseEventCoreferenceEvaluation pece : JCasUtil.select(aJCas,
                PairwiseEventCoreferenceEvaluation.class)) {
            // only process on certain pece if specified. null will process all peces
            if (!(targetPeceComponentId == null || targetPeceComponentId.equals(pece.getComponentId()))) {
                continue;
            }

            for (PairwiseEventFeature pef : FSCollectionFactory.create(pece.getPairwiseEventFeatures(),
                    PairwiseEventFeature.class)) {

                String featureName = pef.getName();
                FeatureType featureType = ConstantUtils
                        .getConstant(FeatureType.class, pef.getFeatureType());
                boolean defaultZero = pef.getDefaultZero();

                Feature f = new Feature.Builder(featureName, featureType).defaultZero(defaultZero).build();
                featureSet.addFeature(f);
            }
        }
        logger.info(featureSet.size() + " features found.");
    }

    @Override
    public String getAggregatedTextToPrint() {
        List<Feature> featureList = featureSet.getSortedFeatureList(true);
        StringBuilder buf = new StringBuilder();
        buf.append("#<feature id>,<feature name>,<default zero>,<feature type>");
        buf.append(System.lineSeparator());
        for (Feature f : featureList) {
            buf.append(f.getFeatureId());
            buf.append(",");
            buf.append(f.getFeatureName());
            buf.append(",");
            buf.append(f.isDefaultZero());
            buf.append(",");
            buf.append(f.getFeatureType());
            buf.append(System.lineSeparator());
        }

        return buf.toString();
    }

}
