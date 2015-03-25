package edu.cmu.lti.event_coref.util;

import edu.cmu.lti.event_coref.type.Word;
import edu.cmu.lti.util.general.*;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides miscellaneous utilities for event coreference.
 *
 * @author Jun Araki
 */
public class EventCoreferenceMiscUtils {

    /**
     * Returns the alignment between the gold standard tokens (Word) and specified tokens.
     *
     * @param aJCas
     * @param clazz
     */
    public static <T extends Annotation> Map<T, Word> getTokenAlignment(JCas aJCas, Class<T> clazz) {
        Map<T, Collection<Word>> wordsCoveredByTokenMap = JCasUtil.indexCovered(aJCas, clazz,
                Word.class);
        Map<T, Collection<Word>> wordsCoveringTokenMap = JCasUtil.indexCovering(aJCas, clazz,
                Word.class);

        Map<T, Word> token2Word = new HashMap<T, Word>();
        for (T token : JCasUtil.select(aJCas, clazz)) {
            if (token.getBegin() <= 0 || token.getEnd() <= 0) {
                continue;
            }

            Collection<Word> wordsCoveredByToken = wordsCoveredByTokenMap.get(token);
            if (!CollectionUtils.isNullOrEmptyCollection(wordsCoveredByToken)) {
                // A token is larger than a word, and covers multiple words.
                for (Word word : wordsCoveredByToken) {
                    if (word.getBegin() <= 0 || word.getEnd() <= 0) {
                        continue;
                    }
                    token2Word.put(token, word);
                }
            } else {
                Collection<Word> wordsCoveringToken = wordsCoveringTokenMap.get(token);
                if (!CollectionUtils.isNullOrEmptyCollection(wordsCoveringToken)) {
                    // A token is smaller than a word, and multiple tokens are covered by a word.
                    for (Word word : wordsCoveringToken) {
                        if (word.getBegin() <= 0 || word.getEnd() <= 0) {
                            continue;
                        }
                        token2Word.put(token, word);
                    }
                } else {
//                    // A token does not cover a token, and is not covered by a token.
//                    System.out.println(String.format(
//                            "The token : %s [%d, %d] cannot be aligned with any words",
//                            token.getCoveredText(), token.getBegin(), token.getEnd()));
                }
            }
        }

        return token2Word;
    }

    /**
     * Returns a short name for the speicified pathname.
     *
     * @param pathname
     * @return a short name for the speicified pathname
     */
    public static String getShortName(String pathname) {
        String fileOrDirName = FileUtils.getName(pathname);
        String extension = ".src.xml.txt";
        if (fileOrDirName.endsWith(extension)) {
            return fileOrDirName.split(extension)[0];
        }

        return fileOrDirName;
    }

    /**
     * Print the specified values in the format of a row of the Trac table.
     *
     * @param values
     */
    public static void printDataForTracTable(Object... values) {
        StringBuffer formatBuf = new StringBuffer();
        for (int i = 0; i < values.length; i++) {
            formatBuf.append("||  %s");
        }
        formatBuf.append("||");

        String printData = String.format(formatBuf.toString(), values);
        System.out.println(printData);
    }

    public static String getFormattedScore(Double number, int decimalSpaces) {
        return MathUtils.getRoundedDecimalNumber(number, decimalSpaces, true,
                "(null)");
    }

    public static String getFormattedCountTotal(Double number) {
        return StringUtils.convertDoubleToString(number);
    }

    public static String getFilenameFromUri(String uri) {
        if (uri.startsWith("file:/")) {
            return uri.substring(6);
        }
        return uri;
    }

    /**
     * Gets rid of all unnecessary tag information from the specified text data.
     *
     * @param input
     * @return text without unnecessary tag information
     */
    public static String replaceTagsWithWhitespace(String input) {
        int initialLength = input.length();

        input = StringUtils.replaceTagWithWhitespace(input, "<DOC .*>");
        input = StringUtils.replaceTagWithWhitespace(input, "</DOC>");
        input = StringUtils.replaceTagWithWhitespace(input, "<DATETIME>.*</DATETIME>");
        input = StringUtils.replaceTagWithWhitespace(input, "<HEADLINE>");
        input = StringUtils.replaceTagWithWhitespace(input, "</HEADLINE>");
        input = StringUtils.replaceTagWithWhitespace(input, "<TEXT>");
        input = StringUtils.replaceTagWithWhitespace(input, "</TEXT>");
        input = StringUtils.replaceTagWithWhitespace(input, "<P>");
        input = StringUtils.replaceTagWithWhitespace(input, "</P>");

        int resultingLength = input.length();
        if (initialLength != resultingLength) {
            System.err.println("The resulting string has a different legnth from the initial string!");
        }

        return input;
    }

    /**
     * Returns the current date. This method hides a concrete implementation, relying on TimeUtils.
     *
     * @return the current date
     */
    public static String getCurrentDate() {
        return TimeUtils.getCurrentYYYYMMDD();
    }

    /**
     * Tests whether the specified integer represents a valid date. This method hides a concrete
     * implementation, relying on TimeUtils.
     *
     * @param input
     * @return true if the specified integer represents a valid date; false otherwise
     */
    public static boolean isValidDate(String input) {
        return TimeUtils.isValidYYYYMMDD(input);
    }

}
