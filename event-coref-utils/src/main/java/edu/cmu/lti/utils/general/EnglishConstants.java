package edu.cmu.lti.utils.general;

/**
 * Constants for English.
 * 
 * @author Jun Araki
 */
public class EnglishConstants {

  /**
   * Constants for Penn TreeBank part-of-speech tags.
   */
  public enum PartOfSpeechTag {
    // Clause level
    S     ("Simple declarative clause"),
    SBAR  ("Subordinate clause"),
    SBARQ ("Direct question introduced by a wh-word or a wh-phrase"),
    SINV  ("Inverted declarative sentence"),
    SQ    ("Inverted yes/no question"),

    // Phrase level
    ADJP  ("Adjective phrase"),
    ADVP  ("Adverb phrase"),
    NP    ("Noun phrase"),
    PP    ("Prepositional phrase"),
    VP    ("Verb phrase"),

    // Word level
    CC   ("Coordinating conjunction"),
    CD   ("Cardinal number"),
    DT   ("Determiner"),
    IN   ("Preposition or subordinating conjunction"),
    JJ   ("Adjective"),
    JJR  ("Adjective, comparative"),
    JJS  ("Adjective, superlative"),
    NN   ("Noun, singular or mass"),
    NNS  ("Noun, plural"),
    NNP  ("Proper noun, singular"),
    NNPS ("Proper noun, plural"),
    PRP  ("Personal pronoun"),
    PRP$ ("Possessive pronoun"),
    VB   ("Verb, base form"),
    VBD  ("Verb, past tense"),
    VBG  ("Verb, gerund or present participle"),
    VBN  ("Verb, past participle"),
    VBP  ("Verb, non-3rd person singular present"),
    VBZ  ("Verb, 3rd person singular present"),
    RB   ("Adverb"),
    RBR  ("Adverb, comparative"),
    RBS  ("Adverb, superlative");

    private final String description;

    // Constructor.
    PartOfSpeechTag(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }

    public static PartOfSpeechTag findPartOfSpeechTag(String tagStr) {
      if (StringUtils.isNullOrEmptyString(tagStr)) {
        return null;
      }

      for (PartOfSpeechTag tag : PartOfSpeechTag.values()) {
        if (tag.toString().equals(tagStr)) {
          return tag;
        }
      }

      return null;
    }
  }

  public enum Tense {
    // Tense
    FUTURE  ("future"),
    PRESENT ("present"),
    PAST    ("past");

    private final String value;

    // Constructor.
    Tense(String value) {
      this.value = value;
    }

    public String toString() {
      return value;
    }
  }

}
