package edu.cmu.lti.event_coref;

/**
 * Created with IntelliJ IDEA.
 * Date: 4/1/15
 * Time: 6:16 PM
 * <p/>
 * A couple of default configurations used internally
 *
 * @author Zhengzhong Liu
 */
public class DefaultConfigs {
    public static final String goldStandardViewName = "GoldStandard";
    public static final String[] srcDocInfoViewNames = new String[]{goldStandardViewName};
    public static final String inputViewName = "Input";
    public static final String targetEventType = "event";
    public static final String TypeSystemDescriptorName = "EventCoreferenceAllTypeSystem";
    public static final String modelName = "models/weka_model_random_forest.ser";
    public static final String systemComponentPrefix = "System";
    public static final String featureListName = "feature_lists/featureNames.txt";
}