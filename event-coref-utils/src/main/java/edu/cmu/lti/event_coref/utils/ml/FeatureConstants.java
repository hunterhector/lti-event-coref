package edu.cmu.lti.event_coref.utils.ml;

public class FeatureConstants {

  public final static double POSITIVE_BINARY_FEATURE_VALUE = 1.0;

  public final static double NEGATIVE_BINARY_FEATURE_VALUE = 0.0;

  public enum FeatureGroup {
    LEXICAL     ("lexical"),
    SYNTACTIC   ("syntactic"),
    SEMANTIC    ("semantic"),
    DISCOURSE   ("discourse"),
    UNSPECIFIED ("unspecified");

    private final String name;

    // Constructor.
    FeatureGroup(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public enum FeatureType {
    BINARY, NOMINAL, NUMERIC;
  }

  /**
   * Constants for feature information.
   * 
   * @author Jun Araki
   */
  public enum PairwiseEventFeatureInfo {
    // Lexical features
    EXACT_STRING_MATCH      ("ExactStringMatch", FeatureGroup.LEXICAL, FeatureType.BINARY),
    LOWER_CASE_STRING_MATCH ("LowerCaseStringMatch", FeatureGroup.LEXICAL, FeatureType.BINARY),
    LEMMA_MATCH             ("LemmaMatch", FeatureGroup.LEXICAL, FeatureType.BINARY),
    LEVENSHTEIN             ("Levenshtein", FeatureGroup.LEXICAL, FeatureType.NUMERIC),
    JACCARD                 ("Jaccard", FeatureGroup.LEXICAL, FeatureType.NUMERIC),
    JARO                    ("Jaro", FeatureGroup.LEXICAL, FeatureType.NUMERIC),
    JARO_WINKLER            ("JaroWinkler", FeatureGroup.LEXICAL, FeatureType.NUMERIC),

    // Syntactic features
    SAME_PART_OF_SPEECH     ("SamePartOfSpeech", FeatureGroup.SYNTACTIC, FeatureType.BINARY),
    BOTH_NOUN               ("BothNoun", FeatureGroup.SYNTACTIC, FeatureType.BINARY),
    BOTH_VERB               ("BothVerb", FeatureGroup.SYNTACTIC, FeatureType.BINARY),
    BOTH_SINGULAR_NOUN      ("BothSingularNoun", FeatureGroup.SYNTACTIC, FeatureType.BINARY),
    BOTH_PLURAL_NOUN        ("BothPluralNoun", FeatureGroup.SYNTACTIC, FeatureType.BINARY),
    SINGULAR_AND_PLURAL     ("SingularAndPluralNoun", FeatureGroup.SYNTACTIC, FeatureType.BINARY),
    PLURAL_AND_SINGULAR     ("PluralAndSingularNoun", FeatureGroup.SYNTACTIC, FeatureType.BINARY),
    NOUN_MATCH              ("NounMatch", FeatureGroup.SYNTACTIC, FeatureType.BINARY),
    VERB_MATCH              ("VerbMatch", FeatureGroup.SYNTACTIC, FeatureType.BINARY),
    NOUN_OR_VERB_MATCH      ("NounOrVerbMatch", FeatureGroup.SYNTACTIC, FeatureType.BINARY),

    // Semantic features
    WORDNET_SIMILARITY      ("WordNetSimilarity", FeatureGroup.SEMANTIC, FeatureType.NUMERIC),
    VERB_OCEAN              ("VerbOcean", FeatureGroup.SEMANTIC, FeatureType.NUMERIC),
    VERB_ORDER              ("VerbOrder", FeatureGroup.SEMANTIC, FeatureType.NUMERIC),
    NARRATIVE_SCHEMA6       ("NarrativeSchema6", FeatureGroup.SEMANTIC, FeatureType.NUMERIC),
    NARRATIVE_SCHEMA8       ("NarrativeSchema8", FeatureGroup.SEMANTIC, FeatureType.NUMERIC),
    NARRATIVE_SCHEMA10      ("NarrativeSchema10", FeatureGroup.SEMANTIC, FeatureType.NUMERIC),
    NARRATIVE_SCHEMA12      ("NarrativeSchema12", FeatureGroup.SEMANTIC, FeatureType.NUMERIC),
    SUBEVENT_ONTOLOGY_PARENT_CHILD            ("SubeventOntologyParentChild", FeatureGroup.SEMANTIC, FeatureType.BINARY),
    SUBEVENT_ONTOLOGY_PARENT_CHILD_THRESHOLD  ("SubeventOntologyParentChildThreshold", FeatureGroup.SEMANTIC, FeatureType.BINARY),
    SUBEVENT_ONTOLOGY_PARENT_CHILD_RATIO      ("SubeventOntologyParentChildRatio", FeatureGroup.SEMANTIC, FeatureType.NUMERIC),
    SUBEVENT_ONTOLOGY_FORWARD_PARENT_CHILD            ("SubeventOntologyForwardParentChild", FeatureGroup.SEMANTIC, FeatureType.BINARY),
    SUBEVENT_ONTOLOGY_FORWARD_PARENT_CHILD_THRESHOLD  ("SubeventOntologyForwardParentChildThreshold", FeatureGroup.SEMANTIC, FeatureType.BINARY),
    SUBEVENT_ONTOLOGY_FORWARD_PARENT_CHILD_RATIO      ("SubeventOntologyForwardParentChildRatio", FeatureGroup.SEMANTIC, FeatureType.NUMERIC),
    SUBEVENT_ONTOLOGY_BACKWARD_PARENT_CHILD           ("SubeventOntologyBackwardParentChild", FeatureGroup.SEMANTIC, FeatureType.BINARY),
    SUBEVENT_ONTOLOGY_BACKWARD_PARENT_CHILD_THRESHOLD ("SubeventOntologyBackwardParentChildThreshold", FeatureGroup.SEMANTIC, FeatureType.BINARY),
    SUBEVENT_ONTOLOGY_BACKWARD_PARENT_CHILD_RATIO     ("SubeventOntologyBackwardParentChildRatio", FeatureGroup.SEMANTIC, FeatureType.NUMERIC),
    SUBEVENT_ONTOLOGY_SISTER                  ("SubeventOntologySister", FeatureGroup.SEMANTIC, FeatureType.BINARY),
    SUBEVENT_ONTOLOGY_SISTER_THRESHOLD        ("SubeventOntologySisterThreshold", FeatureGroup.SEMANTIC, FeatureType.BINARY),
    SUBEVENT_ONTOLOGY_SISTER_RATIO            ("SubeventOntologySisterRatio", FeatureGroup.SEMANTIC, FeatureType.NUMERIC),
    MENTION_TYPE_MATCH      ("MentionTypeMatch", FeatureGroup.SEMANTIC, FeatureType.BINARY),
    PRESENT_OR_PAST_VERB_MATCH ("PresentOrPastVerbMatch", FeatureGroup.SEMANTIC, FeatureType.BINARY),
    BIO_RELATION            ("BioRelation", FeatureGroup.SEMANTIC, FeatureType.NUMERIC),

    // Discourse features
    FORMER_IS_IN_TITLE                   ("FormerIsInTitle", FeatureGroup.DISCOURSE, FeatureType.BINARY),
    FORMER_IS_IN_FIRST_SENTENCE          ("FormerIsInFirstSentence", FeatureGroup.DISCOURSE, FeatureType.BINARY),
    LATTER_IS_IN_TITLE                   ("LatterIsInTitle", FeatureGroup.DISCOURSE, FeatureType.BINARY),
    LATTER_IS_IN_FIRST_SENTENCE          ("LatterIsInFirstSentence", FeatureGroup.DISCOURSE, FeatureType.BINARY),
    SAME_SENTENCE           ("SameSentence", FeatureGroup.DISCOURSE, FeatureType.BINARY),
    TOKEN_DISTANCE          ("TokenDistance", FeatureGroup.DISCOURSE, FeatureType.NUMERIC),
    SENTECE_DISTANCE        ("SentenceDistance", FeatureGroup.DISCOURSE, FeatureType.NUMERIC),
    PARAGRAPH_DISTANCE      ("ParagraphDistance", FeatureGroup.DISCOURSE, FeatureType.NUMERIC);

    private final String featureName;

    private final FeatureGroup featureGroup;

    private final FeatureType featureType;

    // Constructor.
    PairwiseEventFeatureInfo(String featureName, FeatureGroup featureGroup, FeatureType featureType) {
      this.featureName = featureName;
      this.featureGroup = featureGroup;
      this.featureType = featureType;
    }

    public String getFeatureName() {
      return featureName;
    }

    public FeatureGroup getFeatureGroup() {
      return featureGroup;
    }

    public FeatureType getFeatureType() {
      return featureType;
    }

    public boolean isBinaryFeature() {
      if (getFeatureType() == FeatureType.BINARY) {
        return true;
      }

      return false;
    }

    public boolean isNumericFeature() {
      if (getFeatureType() == FeatureType.NUMERIC) {
        return true;
      }

      return false;
    }

    @Override
    public String toString() {
      return featureName;
    }

    /**
     * Returns the constant corresponding to the specified name.
     * 
     * @param name
     * @return
     */
    public static PairwiseEventFeatureInfo findPairwiseEventFeatureInfo(String name) {
      for (PairwiseEventFeatureInfo featureInfo : PairwiseEventFeatureInfo.values()) {
        if (featureInfo.toString().equals(name)) {
          return featureInfo;
        }
      }

      return null;
    }
  }

  /**
   * Constants for feature names.
   * 
   * @author Jun Araki
   */
  public enum PairwiseEventFeatureOld {
    // Old version
    // Lexical features
    EXACT_STRING_MATCH("ExactStringMatch"), LOWERCASE_STRING_MATCH("LowerCaseStringMatch"), STEM_MATCH(
            "StemMatch"), LOWERCASE_STEM_MATCH("LowerCaseStemMatch"),

    // Syntactic features
    POS_MATCH("PosMatch"), NOUN_PAIR("NounPair"), COMMON_NOUN_PAIR("CommonNounPair"), NONPROPER_NOUN_PAIR(
            "NonProperNounPair"), NOUN_MATCH("NounMatch"), VERB_PAIR("VerbPair"), VERB_MATCH(
            "VerbMatch"), NOUN_OR_VERB_MATCH("NounOrVerbMatch"), NOUN_OR_VERB_UNMATCH(
            "NounOrVerbUnmatch"), NOUN_OR_VERB("NounOrVerb"), NOUN_OR_GERUND("NounOrGerund"), NUMBER_MATCH(
            "NumberMatch"), PLURAL_AND_SINGULAR_NOUN("PluralAndSingularNoun"),

    // Semantic features
    MENTION_TYPE_MATCH("MentionTypeMatch"), WORDNET_SIMILARITY("WordNetSimilarity"), AGENT_MATCH(
            "AgentMatch"), LOCATION_MATCH("LocationMatch"), TIME_MATCH("TimeMatch"), ONTOLOGY_SUBEVENT(
            "OntologySubevent"), PARENT_AND_CHILD_IN_ORDER_IN_SUBEVENT_ONTOLOGY_FROM_GOLD_STANDARD(
            "ParentAndChildInOrderInSubeventOntologyFromGoldStandard"), PARENT_AND_CHILD_PAIR_IN_SUBEVENT_ONTOLOGY_FROM_GOLD_STANDARD(
            "ParentAndChildPairInSubeventOntologyFromGoldStandard"), PARENT_AND_CHILD_IN_ORDER_IN_MEMBER_ONTOLOGY_FROM_GOLD_STANDARD(
            "ParentAndChildInOrderInMemberOntologyFromGoldStandard"), PARENT_AND_CHILD_PAIR_IN_MEMBER_ONTOLOGY_FROM_GOLD_STANDARD(
            "ParentAndChildPairInMemberOntologyFromGoldStandard"),

    // Discourse features
    DEFINITE_DETERMINER_EXISTENCE("DefiniteDeterminerExistence"), SENTENCE_DISTANCE(
            "SentenceDistance"), PARAGRAPH_DISTANCE("ParagraphDistance"),

    // Combinatorial features
    NOUN_MATCH_AND_DEFINITE_DETERMINER_EXISTENCE("NounMatchAndDefiniteDeterminerExistence"), NOUN_MATCH_AND_MENTION_TYPE_MATCH(
            "NounMatchAndMentionTypeMatch"), NOUN_MATCH_AND_WORDNET_SIMILARITY(
            "NounMatchAndWordNetSimilarity"),

    NOUN_OR_VERB_MATCH_AND_DEFINITE_DETERMINER_EXISTENCE(
            "NounOrVerbMatchAndDefiniteDeterminerExistence"), NOUN_OR_VERB_MATCH_AND_MENTION_TYPE_MATCH(
            "NounOrVerbMatchAndMentionTypeMatch"), NOUN_OR_VERB_MATCH_AND_WORDNET_SIMILARITY(
            "NounOrVerbMatchAndWordNetSimilarity"),

    NOUN_OR_VERB_AND_DEFINITE_DETERMINER_EXISTENCE("NounOrVerbAndDefiniteDeterminerExistence"), NOUN_OR_VERB_AND_MENTION_TYPE_MATCH(
            "NounOrVerbAndMentionTypeMatch"), NOUN_OR_VERB_AND_WORDNET_SIMILARITY(
            "NounOrVerbAndWordNetSimilarity"),

    NOUN_OR_GERUND_AND_DEFINITE_DETERMINER_EXISTENCE("NounOrGerundAndDefiniteDeterminerExistence"), NOUN_OR_GERUND_AND_MENTION_TYPE_MATCH(
            "NounOrGerundAndMentionTypeMatch"), NOUN_OR_GERUND_AND_WORDNET_SIMILARITY(
            "NounOrGerundAndWordNetSimilarity"),

    DEFINITE_DETERMINER_EXISTENCE_AND_MENTION_TYPE_MATCH(
            "DefiniteDeterminerExistenceAndMentionTypeMatch"), DEFINITE_DETERMINER_EXISTENCE_AND_WORDNET_SIMILARITY(
            "DefiniteDeterminerExistenceAndWordNetSimilarity"), MENTION_TYPE_MATCH_AND_WORDNET_SIMILARITY(
            "MentionTypeMatchAndWordNetSimilarity");

    private final String name;

    // Constructor.
    PairwiseEventFeatureOld(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

}
