package edu.cmu.lti.utils.general;


import edu.cmu.lti.utils.general.EnglishConstants.PartOfSpeechTag;

/**
 * A utility class for handling the English language.
 *
 * @author Jun Araki
 */
public class EnglishUtils {

    /**
     * Checks whether the specified string is the basic determiner.
     *
     * @param input
     * @return true if the specified string is the basic determiner; false otherwise.
     */
    public static boolean isBasicDeterminer(String input) {
        if (StringUtils.isNullOrEmptyString(input)) {
            return false;
        }

        if (input.equals("the") || input.equals("this") || input.equals("that")
                || input.equals("these") || input.equals("those")) {
            return true;
        }

        return false;
    }

    public static boolean isCommonNoun(String tag) {
        if (StringUtils.isNullOrEmptyString(tag)) {
            return false;
        }

        if (tag.equals(PartOfSpeechTag.NN.toString()) || tag.equals(PartOfSpeechTag.NNS.toString())) {
            return true;
        }

        return false;
    }

    public static boolean isProperNoun(String tag) {
        if (StringUtils.isNullOrEmptyString(tag)) {
            return false;
        }

        if (tag.equals(PartOfSpeechTag.NNP.toString()) || tag.equals(PartOfSpeechTag.NNPS.toString())) {
            return true;
        }

        return false;
    }

    public static boolean isPronoun(String tag) {
        if (StringUtils.isNullOrEmptyString(tag)) {
            return false;
        }

        if (tag.startsWith(PartOfSpeechTag.PRP.toString())) {
            return true;
        }

        return false;
    }

    public static boolean isSingularNoun(String tag) {
        if (StringUtils.isNullOrEmptyString(tag)) {
            return false;
        }

        if (tag.equals(PartOfSpeechTag.NN.toString()) || tag.equals(PartOfSpeechTag.NNP.toString())) {
            return true;
        }

        return false;
    }

    public static boolean isPluralNoun(String tag) {
        if (StringUtils.isNullOrEmptyString(tag)) {
            return false;
        }

        if (tag.equals(PartOfSpeechTag.NNS.toString()) || tag.equals(PartOfSpeechTag.NNPS.toString())) {
            return true;
        }

        return false;
    }

    public static boolean isNoun(String tag) {
        if (isCommonNoun(tag) || isProperNoun(tag) || isPronoun(tag)) {
            return true;
        }

        return false;
    }

    public static boolean isVerb(String tag) {
        if (StringUtils.isNullOrEmptyString(tag)) {
            return false;
        }

        if (tag.startsWith(PartOfSpeechTag.VB.toString())) {
            return true;
        }

        return false;
    }

    public static boolean isPresentVerb(String tag) {
        if (StringUtils.isNullOrEmptyString(tag)) {
            return false;
        }

        if (tag.equals(PartOfSpeechTag.VBP.toString()) || tag.equals(PartOfSpeechTag.VBZ.toString())) {
            return true;
        }

        return false;
    }

    public static boolean isPastVerb(String tag) {
        if (StringUtils.isNullOrEmptyString(tag)) {
            return false;
        }

        if (tag.equals(PartOfSpeechTag.VBD.toString())) {
            return true;
        }

        return false;
    }

    public static boolean isGerund(String tag) {
        if (StringUtils.isNullOrEmptyString(tag)) {
            return false;
        }

        if (tag.equals(PartOfSpeechTag.VBG.toString())) {
            return true;
        }

        return false;
    }

    public static boolean isPrepositionOrSubordinatingConjunction(String tag) {
        if (StringUtils.isNullOrEmptyString(tag)) {
            return false;
        }

        if (tag.equals(PartOfSpeechTag.IN.toString())) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether the specified sentence ends with an appropriate punctuation.
     *
     * @param sentence
     * @return true if the specified sentence ends with an appropriate punctuation; false otherwise.
     */
    public static boolean endsWithPunctuation(String sentence) {
        final String PERIOD = ".";
        final String QMARK = "?";
        final String EMARK = "!";
        final String SQUOTE = "'";
        final String TWO_SQUOTES = SQUOTE + SQUOTE;
        final String DQUOTE = "\"";
        final String PERIOD_DQUOTE = PERIOD + DQUOTE;
        final String QMARK_DQUOTE = QMARK + DQUOTE;
        final String EMARK_DQUOTE = EMARK + DQUOTE;
        final String PERIOD_TWO_SQUOTES = PERIOD + TWO_SQUOTES;
        final String QMARK_TWO_SQUOTES = QMARK + TWO_SQUOTES;
        final String EMARK_TWO_SQUOTES = EMARK + TWO_SQUOTES;

        final String COMMA = ",";
        final String COLON = ":";
        final String SEMICOLON = ";";

        if (sentence.endsWith(PERIOD) || sentence.endsWith(QMARK) || sentence.endsWith(EMARK)
                || sentence.endsWith(PERIOD_DQUOTE) || sentence.endsWith(QMARK_DQUOTE)
                || sentence.endsWith(EMARK_DQUOTE) || sentence.endsWith(PERIOD_TWO_SQUOTES)
                || sentence.endsWith(QMARK_TWO_SQUOTES) || sentence.endsWith(EMARK_TWO_SQUOTES)) {
            return true;
        }

        if (sentence.endsWith(COMMA) || sentence.endsWith(COLON) || sentence.endsWith(SEMICOLON)) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether the specified string is an punctuation.
     *
     * @param input
     * @return true if the specified string is an punctuation; false otherwise.
     */
    public static boolean isPunctuation(String input) {
        final String PERIOD = ".";
        final String QMARK = "?";
        final String EMARK = "!";
        final String SQUOTE = "'";
        final String TWO_SQUOTES = SQUOTE + SQUOTE;
        final String DQUOTE = "\"";

        if (StringUtils.isNullOrEmptyString(input)) {
            return false;
        }

        if (PERIOD.equals(input) || QMARK.equals(input) || EMARK.equals(input)
                || SQUOTE.equals(input) || TWO_SQUOTES.equals(input) || DQUOTE.equals(input)) {
            return true;
        }

        return false;
    }

}
