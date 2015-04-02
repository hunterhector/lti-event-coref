package edu.cmu.lti.event_coref.utils.ml;

import edu.cmu.lti.event_coref.utils.ml.FeatureConstants.FeatureGroup;
import edu.cmu.lti.event_coref.utils.ml.FeatureConstants.FeatureType;
import edu.cmu.lti.utils.general.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * A general feature factory based on the Builder pattern. This implementation does not rely on any
 * <p/>
 * Basic usage: Feature f = new Feature.Builder(<feature name>, <feature type>).build();
 *
 * @author Jun Araki
 */
public class Feature implements Comparable<Feature> {

    private final String featureName;

    private final FeatureType featureType;

    /**
     * This must be numeric for the sorting purpose.
     */
    private int featureId;

    private boolean defaultZero;

    private FeatureGroup featureGroup;

    /**
     * Any comment about this feature
     */
    private String comment;

    public static class Builder {
        // Required parameters
        private final String featureName;
        private final FeatureType featureType;

        // Optional parameters - initialized to default values
        private int featureId = -1;
        private boolean defaultZero = true;
        private FeatureGroup featureGroup = FeatureGroup.UNSPECIFIED;
        private String comment = "";

        public Builder(String featureName, FeatureType featureType) {
            this.featureName = featureName;
            this.featureType = featureType;
        }

        public Builder featureId(int value) {
            featureId = value;
            return this;
        }

        public Builder defaultZero(boolean value) {
            defaultZero = value;
            return this;
        }

        public Builder featureGroup(FeatureGroup value) {
            featureGroup = value;
            return this;
        }

        public Builder comment(String value) {
            comment = value;
            return this;
        }

        public Feature build() {
            return new Feature(this);
        }
    }

    private Feature(Builder builder) {
        featureId = builder.featureId;
        featureName = builder.featureName;
        featureType = builder.featureType;
        defaultZero = builder.defaultZero;
        featureGroup = builder.featureGroup;
        comment = builder.comment;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Feature)) {
            return false;
        }

        Feature f = (Feature) obj;
        if (featureName.equals(f.getFeatureName())) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(featureName).toHashCode();
    }

    @Override
    public int compareTo(Feature f) {
        int cmp = 0;
        if (featureId < f.getFeatureId()) {
            return -1;
        } else if (featureId > f.getFeatureId()) {
            return 1;
        }

        if (featureGroup != null && f.getFeatureGroup() != null) {
            cmp = featureGroup.toString().compareTo(f.getFeatureGroup().toString());
            if (cmp != 0) {
                return cmp;
            }
        }

        if (!StringUtils.isNullOrEmptyString(featureName) && !StringUtils.isNullOrEmptyString(f.getFeatureName())) {
            cmp = featureName.compareTo(f.getFeatureName());
            if (cmp != 0) {
                return cmp;
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(featureName);
        buf.append(" (type:" + featureType.name());
        buf.append(", id:" + featureId);
        if (defaultZero) {
            buf.append(", default zero:yes");
        } else {
            buf.append(", default zero:no");
        }
        if (featureGroup != null) {
            buf.append(", group:" + featureGroup.name());
        }
        buf.append(")");

        return buf.toString();
    }

    public int getFeatureId() {
        return featureId;
    }

    public String getFeatureName() {
        return featureName;
    }

    public boolean isDefaultZero() {
        return defaultZero;
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public FeatureGroup getFeatureGroup() {
        return featureGroup;
    }

    public String getComment() {
        return comment;
    }

    public void setFeatureId(int featureId) {
        this.featureId = featureId;
    }

    public void setDefaultZero(boolean defaultZero) {
        this.defaultZero = defaultZero;
    }

    public void setFeatureGroup(FeatureGroup featureGroup) {
        this.featureGroup = featureGroup;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
