package edu.cmu.lti.event_coref.util;

import edu.cmu.lti.util.general.StringUtils;

/**
 * A collection of convenient constants related to event coreference relations, clusters, and others.
 *
 * @author Jun Araki
 */
public class EventCoreferenceConstants {

    /**
     * Types of an event.
     */
    public enum EventType {
        REPORTING("report"),
        DOMAIN("event");

        private final String goldStandardAnnotationStr;

        /**
         * Constructor.
         *
         * @param goldStandardAnnotationStr
         */
        EventType(String goldStandardAnnotationStr) {
            this.goldStandardAnnotationStr = goldStandardAnnotationStr;
        }

        public String getGoldStandardAnnotationStr() {
            return goldStandardAnnotationStr;
        }

        @Override
        public String toString() {
            return getGoldStandardAnnotationStr();
        }
    }

    /**
     * Abstract types of event coreference used in both event coreference relations and clusters.
     * See (Hovy et al., 2013) for the definition of them:
     * <p/>
     * Eduard Hovy, Teruko Mitamura, Felisa Verdejo, Jun Araki, and Andrew Philpot. 2013. Events are
     * Not Simple: Identity, Non-Identity, and Quasi-Identity. In Proceedings of the Workshop on
     * Events: Definition, Detection, Coreference, and Representation, pages 21�28.
     */
    public enum AbstractEventCoreferenceType {
        FULL("full"),
        MEMBER("member"),
        SUBEVENT("subevent"),
        NO("no");

        private final String type;

        AbstractEventCoreferenceType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    /**
     * Types of an event coreference cluster in system output.
     */
    public enum SystemEventCoreferenceClusterType {
        FULL("full"),
        SUBEVENT("subevent"),
        MEMBER("member");

        private final String systemStandardAnnotationStr;

        /**
         * Constructor.
         *
         * @param systemStandardAnnotationStr
         */
        SystemEventCoreferenceClusterType(String systemStandardAnnotationStr) {
            this.systemStandardAnnotationStr = systemStandardAnnotationStr;
        }

        public String getSystemAnnotationStr() {
            return systemStandardAnnotationStr;
        }

        @Override
        public String toString() {
            return getSystemAnnotationStr();
        }
    }

    /**
     * Types of an event coreference cluster in the gold standard.
     */
    public enum GoldStandardEventCoreferenceClusterType {
        FULL("coreference"),
        SUBEVENT("subevent"),
        MEMBER("member");

        private final String goldStandardAnnotationStr;

        /**
         * Constructor.
         *
         * @param goldStandardAnnotationStr
         */
        GoldStandardEventCoreferenceClusterType(String goldStandardAnnotationStr) {
            this.goldStandardAnnotationStr = goldStandardAnnotationStr;
        }

        public String getGoldStandardAnnotationStr() {
            return goldStandardAnnotationStr;
        }

        @Override
        public String toString() {
            return getGoldStandardAnnotationStr();
        }
    }

    /**
     * Types of an event coreference cluster.
     */
    public enum EventCoreferenceClusterType {
        // Type (system annotation, gold standard annotation, abstract type)
        FULL(SystemEventCoreferenceClusterType.FULL, GoldStandardEventCoreferenceClusterType.FULL, AbstractEventCoreferenceType.FULL),
        SUBEVENT(SystemEventCoreferenceClusterType.SUBEVENT, GoldStandardEventCoreferenceClusterType.SUBEVENT, AbstractEventCoreferenceType.SUBEVENT),
        MEMBER(SystemEventCoreferenceClusterType.MEMBER, GoldStandardEventCoreferenceClusterType.MEMBER, AbstractEventCoreferenceType.MEMBER);

        private final SystemEventCoreferenceClusterType systemAnnotation;

        private final GoldStandardEventCoreferenceClusterType goldStandardAnnotation;

        private final AbstractEventCoreferenceType type;

        /**
         * Constructor.
         *
         * @param systemAnnotation
         * @param goldStandardAnnotation
         * @param type
         */
        EventCoreferenceClusterType(SystemEventCoreferenceClusterType systemAnnotation,
                                    GoldStandardEventCoreferenceClusterType goldStandardAnnotation,
                                    AbstractEventCoreferenceType type) {
            this.systemAnnotation = systemAnnotation;
            this.goldStandardAnnotation = goldStandardAnnotation;
            this.type = type;
        }

        public SystemEventCoreferenceClusterType getSystemAnnotation() {
            return systemAnnotation;
        }

        public String getSystemAnnotationStr() {
            return systemAnnotation.toString();
        }

        public GoldStandardEventCoreferenceClusterType getGoldStandardAnnotation() {
            return goldStandardAnnotation;
        }

        public String getGoldStandardAnnotationStr() {
            return goldStandardAnnotation.toString();
        }

        public AbstractEventCoreferenceType getType() {
            return type;
        }

        @Override
        public String toString() {
            return getSystemAnnotationStr();
        }

        /**
         * Returns the event coreference cluster type corresponding to the specified string,
         * which is expected to be in the system annotation.
         *
         * @param systemAnnotationStr
         * @return the event coreference cluster type corresponding to the specified string,
         * which is expected to be in the system annotation
         */
        public static EventCoreferenceClusterType findConstant(String systemAnnotationStr) {
            if (StringUtils.isNullOrEmptyString(systemAnnotationStr)) {
                return null;
            }

            for (EventCoreferenceClusterType clusterType : EventCoreferenceClusterType.values()) {
                if (clusterType.toString().equals(systemAnnotationStr)) {
                    return clusterType;
                }
            }

            return null;
        }

        public boolean isPartialIdentityType() {
            if (this.equals(SUBEVENT) || this.equals(MEMBER)) {
                return true;
            }
            return false;
        }
    }

    /**
     * Types of an event coreference relation in system output.
     */
    public enum SystemEventCoreferenceRelationType {
        FULL("full"),
        SUBEVENT("subevent"),
        SUBEVENT_FORWARD("subeventForward"),
        SUBEVENT_BACKWARD("subeventBackward"),
        SUBEVENT_SISTER("subeventSister"),
        MEMBER("member"),
        MEMBER_FORWARD("memberForward"),
        MEMBER_BACKWARD("memberBackward"),
        MEMBER_SISTER("memberSister"),
        NO("no");

        private final String systemAnnotationStr;

        /**
         * Constructor.
         *
         * @param systemAnnotationStr
         */
        SystemEventCoreferenceRelationType(String systemAnnotationStr) {
            this.systemAnnotationStr = systemAnnotationStr;
        }

        public String getSystemAnnotationStr() {
            return systemAnnotationStr;
        }

        @Override
        public String toString() {
            return getSystemAnnotationStr();
        }
    }

    /**
     * Types of an event coreference relation in the gold standard.
     */
    public enum GoldStandardEventCoreferenceRelationType {
        FULL("coreference"),
        SUBEVENT("subeventOf"),
        MEMBER("memberOf"),
        NO(null),
        REPORTING("inReporting");

        private final String goldStandardAnnotationStr;

        /**
         * Constructor.
         *
         * @param goldStandardAnnotationStr
         */
        GoldStandardEventCoreferenceRelationType(String goldStandardAnnotationStr) {
            this.goldStandardAnnotationStr = goldStandardAnnotationStr;
        }

        public String getGoldStandardAnnotationStr() {
            return goldStandardAnnotationStr;
        }

        @Override
        public String toString() {
            return getGoldStandardAnnotationStr();
        }
    }

    /**
     * Types of an event coreference relation.
     */
    public enum EventCoreferenceRelationType {
        FULL(SystemEventCoreferenceRelationType.FULL, GoldStandardEventCoreferenceRelationType.FULL, AbstractEventCoreferenceType.FULL, 1),
        SUBEVENT(SystemEventCoreferenceRelationType.SUBEVENT, GoldStandardEventCoreferenceRelationType.SUBEVENT, AbstractEventCoreferenceType.SUBEVENT, 3),
        MEMBER(SystemEventCoreferenceRelationType.MEMBER, GoldStandardEventCoreferenceRelationType.MEMBER, AbstractEventCoreferenceType.MEMBER, 2),
        NO(SystemEventCoreferenceRelationType.NO, GoldStandardEventCoreferenceRelationType.NO, AbstractEventCoreferenceType.NO, 4);

        private final SystemEventCoreferenceRelationType systemAnnotation;

        private final GoldStandardEventCoreferenceRelationType goldStandardAnnotation;

        private final AbstractEventCoreferenceType type;

        private final int index;

        /**
         * Constructor.
         *
         * @param systemAnnotation
         * @param goldStandardAnnotation
         * @param type
         * @param index
         */
        EventCoreferenceRelationType(SystemEventCoreferenceRelationType systemAnnotation,
                                     GoldStandardEventCoreferenceRelationType goldStandardAnnotation,
                                     AbstractEventCoreferenceType type, int index) {
            this.systemAnnotation = systemAnnotation;
            this.goldStandardAnnotation = goldStandardAnnotation;
            this.type = type;
            this.index = index;
        }

        public SystemEventCoreferenceRelationType getSystemAnnotation() {
            return systemAnnotation;
        }

        public String getSystemAnnotationStr() {
            return systemAnnotation.toString();
        }

        public GoldStandardEventCoreferenceRelationType getGoldStandardAnnotation() {
            return goldStandardAnnotation;
        }

        public String getGoldStandardAnnotationStr() {
            return goldStandardAnnotation.toString();
        }

        public AbstractEventCoreferenceType getType() {
            return type;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return getSystemAnnotationStr();
        }

        public static int size() {
            return EventCoreferenceRelationType.values().length;
        }

        /**
         * Returns the index of the event coreference relation type corresponding to the specified
         * string, which is expected to be in the system annotation.
         *
         * @param systemAnnotationStr
         * @return the index of the event coreference relation type corresponding to the specified
         * string, which is expected to be in the system annotation
         */
        public static int getIndex(String systemAnnotationStr) {
            for (EventCoreferenceRelationType relationType : EventCoreferenceRelationType.values()) {
                if (relationType.toString().equals(systemAnnotationStr)) {
                    return relationType.getIndex();
                }
            }

            return -1;
        }

        /**
         * Returns the event coreference relation type corresponding to the specified string
         * corresponding to the system annotation.
         *
         * @param systemAnnotationStr
         * @return the event coreference relation type corresponding to the specified string
         * corresponding to the system annotation
         */
        public static EventCoreferenceRelationType findConstant(String systemAnnotationStr) {
            if (StringUtils.isNullOrEmptyString(systemAnnotationStr)) {
                return null;
            }

            for (EventCoreferenceRelationType relationType : EventCoreferenceRelationType.values()) {
                if (relationType.toString().equals(systemAnnotationStr)) {
                    return relationType;
                }
            }

            return null;
        }
    }

    /**
     * Types of a detailed event coreference relation.
     */
    public enum DetailedEventCoreferenceRelationType {
        FULL(SystemEventCoreferenceRelationType.FULL, GoldStandardEventCoreferenceRelationType.FULL, AbstractEventCoreferenceType.FULL),
        MEMBER(SystemEventCoreferenceRelationType.MEMBER, GoldStandardEventCoreferenceRelationType.MEMBER, AbstractEventCoreferenceType.MEMBER),
        MEMBER_FORWARD(SystemEventCoreferenceRelationType.MEMBER_FORWARD, GoldStandardEventCoreferenceRelationType.MEMBER, AbstractEventCoreferenceType.MEMBER),
        MEMBER_BACKWARD(SystemEventCoreferenceRelationType.MEMBER_BACKWARD, GoldStandardEventCoreferenceRelationType.MEMBER, AbstractEventCoreferenceType.MEMBER),
        MEMBER_SISTER(SystemEventCoreferenceRelationType.MEMBER_SISTER, null, AbstractEventCoreferenceType.NO),
        SUBEVENT(SystemEventCoreferenceRelationType.SUBEVENT, GoldStandardEventCoreferenceRelationType.SUBEVENT, AbstractEventCoreferenceType.SUBEVENT),
        SUBEVENT_FORWARD(SystemEventCoreferenceRelationType.SUBEVENT_FORWARD, GoldStandardEventCoreferenceRelationType.SUBEVENT, AbstractEventCoreferenceType.SUBEVENT),
        SUBEVENT_BACKWARD(SystemEventCoreferenceRelationType.SUBEVENT_BACKWARD, GoldStandardEventCoreferenceRelationType.SUBEVENT, AbstractEventCoreferenceType.SUBEVENT),
        SUBEVENT_SISTER(SystemEventCoreferenceRelationType.SUBEVENT_SISTER, null, AbstractEventCoreferenceType.NO),
        NO(SystemEventCoreferenceRelationType.NO, GoldStandardEventCoreferenceRelationType.NO, AbstractEventCoreferenceType.NO);

        private final SystemEventCoreferenceRelationType systemAnnotation;

        private final GoldStandardEventCoreferenceRelationType goldStandardAnnotation;

        private final AbstractEventCoreferenceType type;

        /**
         * Constructor.
         *
         * @param systemAnnotation
         * @param goldStandardAnnotation
         * @param type
         */
        DetailedEventCoreferenceRelationType(SystemEventCoreferenceRelationType systemAnnotation,
                                             GoldStandardEventCoreferenceRelationType goldStandardAnnotation,
                                             AbstractEventCoreferenceType type) {
            this.systemAnnotation = systemAnnotation;
            this.goldStandardAnnotation = goldStandardAnnotation;
            this.type = type;
        }

        public SystemEventCoreferenceRelationType getSystemAnnotation() {
            return systemAnnotation;
        }

        public String getSystemAnnotationStr() {
            return systemAnnotation.toString();
        }

        public GoldStandardEventCoreferenceRelationType getGoldStandardAnnotation() {
            return goldStandardAnnotation;
        }

        public String getGoldStandardAnnotationStr() {
            return goldStandardAnnotation.toString();
        }

        public AbstractEventCoreferenceType getType() {
            return type;
        }

        @Override
        public String toString() {
            return getSystemAnnotationStr();
        }

        public static int size() {
            return DetailedEventCoreferenceRelationType.values().length;
        }

        public static int getIndex(String systemAnnotationStr) {
            int index = 0;
            for (DetailedEventCoreferenceRelationType relationType : DetailedEventCoreferenceRelationType
                    .values()) {
                if (relationType.toString().equals(systemAnnotationStr)) {
                    return index;
                }
                index++;
            }

            return -1;
        }

        /**
         * Returns the directed event coreference relation type corresponding to the specified string.
         *
         * @param systemAnnotationStr
         * @return the directed event coreference relation type corresponding to the specified string
         */
        public static DetailedEventCoreferenceRelationType findConstant(String systemAnnotationStr) {
            if (StringUtils.isNullOrEmptyString(systemAnnotationStr)) {
                return null;
            }

            for (DetailedEventCoreferenceRelationType relationType : DetailedEventCoreferenceRelationType
                    .values()) {
                if (relationType.toString().equals(systemAnnotationStr)) {
                    return relationType;
                }
            }

            return null;
        }
    }

    /**
     * Constants for DBpedia.
     */
    public enum DBpediaProperty {
        NULL("null"), // Initial value just for convenience

        COUNTRY("country"), TIMEZONE("timezone"), IS_PART_OF("isPartOf");

        private final String property;

        // Constructor.
        DBpediaProperty(String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
        }
    }

    /**
     * Constants for ontology event types.
     */
    public enum OntologyTreeType {
        RAW_STRING, STEM, LEMMA;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

}
