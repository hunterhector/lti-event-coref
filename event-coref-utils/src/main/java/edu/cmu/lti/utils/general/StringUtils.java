package edu.cmu.lti.utils.general;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for handling strings.
 *
 * @author Jun Araki
 */
public class StringUtils {

    /**
     * Returns the initialized string of the specified object. This string is an empty string if the
     * object is null.
     *
     * @param o
     * @return the initialized string of the specified object
     */
    public static String getInitializedString(Object o) {
        String s = "";
        if (o != null) {
            s = o.toString();
        }

        return s;
    }

    /**
     * Tests whether the specified string is null or an empty string.
     *
     * @param input
     * @return true if the specified string is null or an empty string; false otherwise
     */
    public static boolean isNullOrEmptyString(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * Returns true if the specified input is a string corresponding to a numeric value.
     *
     * @param input
     * @return
     */
    public static boolean isNumeric(String input) {
        return (isInteger(input) || isDouble(input));
    }

    /**
     * Returns true if the specified input is a string corresponding to an integer.
     *
     * @param input
     * @return true if the specified input is a string corresponding to an integer; false otherwise
     */
    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns an integer converted from the specified string.
     *
     * @param input
     * @return an integer converted from the specified string
     */
    public static Integer convertStringToInteger(String input) {
        if (isInteger(input)) {
            return Integer.parseInt(input);
        }
        return null;
    }

    /**
     * Returns a string converted from the specified integer.
     *
     * @param input
     * @return a string converted from the specified integer
     */
    public static String convertIntegerToString(Integer input) {
        if (input == null) {
            return null;
        }
        return Integer.toString(input);
    }

    /**
     * Returns true if the specified input is a decimal number of the type Double.
     *
     * @param input
     * @return true if the specified input is a decimal number of the type Double; false otherwise
     */
    public static boolean isDouble(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Double convertStringToDouble(String input) {
        if (isDouble(input)) {
            return Double.parseDouble(input);
        }
        return null;
    }

    public static String convertDoubleToString(Double input) {
        return Double.toString(input);
    }

    public static String replaceTagWithWhitespace(String input, String tagRegex) {
        Pattern p = Pattern.compile(tagRegex);
        Matcher m = p.matcher(input);
        while (m.find()) {
            int start = m.start();
            int end = m.end();

            StringBuilder buf = new StringBuilder();
            buf.append(input.substring(0, start));
            buf.append(getStringOfWhiteSpace(end - start));
            buf.append(input.substring(end));

            input = buf.toString();
        }

        return input;
    }

    /**
     * Returns a sequence of white space with specified length.
     *
     * @param length
     * @return a sequence of white space with specified length
     */
    public static String getStringOfWhiteSpace(int length) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < length; i++) {
            buf.append(" ");
        }

        return buf.toString();
    }

    /**
     * Returns a string concatenating all the specified values with a separator.
     *
     * @param values
     * @param separator
     * @return a string concatenating all the specified values with a separator
     */
    public static String concatenate(List<Object> values, String separator) {
        if (values == null || values.size() == 0) {
            return null;
        }

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i != 0) {
                buf.append(separator);
            }
            buf.append(values.get(i));
        }

        return buf.toString();
    }

    /**
     * Returns a string that we obtain by stripping beginning and ending double quotations.
     *
     * @param input
     * @return a string that we obtain by stripping beginning and ending double quotations
     */
    public static String stripBeginningAndEndingDoubleQuotations(String input) {
        return input.replaceAll("^\"|\"$", "");
    }

    /**
     * Returns a list of lines of the specified input string.
     *
     * @param input
     * @return a list of lines of the specified input string
     */
    public static List<String> splitToLines(String input) {
        return Arrays.asList(input.split(System.lineSeparator()));
    }

    /**
     * Pad the specified character to the left side of the specified string until it reachs the
     * specified length.
     *
     * @param input
     * @param paddingCharacter
     * @param totalLength
     * @return a string with the specified characters padded to the left side of the specified input
     */
    public static String padStringToLeft(String input, char paddingCharacter, int totalLength) {
        int len = input.length();
        if (len >= totalLength) {
            return input;
        }

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < totalLength - len; i++) {
            buf.append(paddingCharacter);
        }
        buf.append(input);

        return buf.toString();
    }

    /**
     * Pad the specified character to the right side of the specified string until it reachs the
     * specified length.
     *
     * @param input
     * @param paddingCharacter
     * @param totalLength
     * @return a string with the specified characters padded to the right side of the specified input
     */
    public static String padStringToRight(String input, char paddingCharacter, int totalLength) {
        int len = input.length();
        if (len >= totalLength) {
            return input;
        }

        StringBuilder buf = new StringBuilder();
        buf.append(input);
        for (int i = 0; i < totalLength - len; i++) {
            buf.append(paddingCharacter);
        }

        return buf.toString();
    }

    /**
     * Splits the specified string with white spaces, and returns a list of non-whitespace strings.
     *
     * @param input
     * @return returns a list of non-whitespace strings
     */
    public static List<String> splitToNonWhiteSpaceStrings(String input) {
        return Arrays.asList(input.split("\\s+"));
    }

    /**
     * Returns the input string trimmed with the specified characters.
     *
     * @param input
     * @param cs
     * @return the input string trimmed with the specified characters
     */
    public static String trim(String input, CharSequence cs) {
        String regexHead = "^[" + cs.toString() + "]+";
        String regexTail = "[" + cs.toString() + "]+$";
        return input.replaceAll(regexHead, "").replaceAll(regexTail, "");
    }

    /**
     * Test if the input string contains no letter or digit
     *
     * @param input
     * @return true if contains no letter of digit.
     */
    public static boolean noLetterOrDigit(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (Character.isLetterOrDigit(input.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the Levenshtein distance between the specified two strings.
     *
     * @param str1
     * @param str2
     * @return the Levenshtein distance between the specified two strings
     */
    public static int getLevenshteinDistance(String str1, String str2) {
        int strlen1 = str1.length();
        int strlen2 = str2.length();
        int[][] distance = new int[strlen1 + 1][strlen2 + 1];

        for (int i = 0; i <= strlen1; i++) {
            distance[i][0] = i;
        }
        for (int j = 1; j <= strlen2; j++) {
            distance[0][j] = j;
        }

        for (int i = 1; i <= strlen1; i++) {
            for (int j = 1; j <= strlen2; j++) {
                int a = distance[i - 1][j] + 1;
                int b = distance[i][j - 1] + 1;
                int c = distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1);
                distance[i][j] = Math.min(Math.min(a, b), c);
            }
        }

        return distance[str1.length()][str2.length()];
    }

    public static String capitalize(String inputStr) {
        StringBuilder buf = new StringBuilder();
        buf.append(inputStr.substring(0, 1).toUpperCase());
        buf.append(inputStr.substring(1));
        return buf.toString();
    }

    public static String decapitalize(String inputStr) {
        StringBuilder buf = new StringBuilder();
        buf.append(inputStr.substring(0, 1).toLowerCase());
        buf.append(inputStr.substring(1));
        return buf.toString();
    }

    /**
     * Return false while the comparer is null or not equal to the target.
     * The target is required to be a not-null string
     *
     * @param comparer
     * @param target
     * @return
     */
    public static boolean notNullAndEquals(String comparer, String target) {
        if (target == null) {
            throw new IllegalArgumentException(
                    "Target string should be a real value string, cannot be null");
        }
        if (comparer == null) {
            return false;
        } else if (comparer.equals(target)) {
            return true;
        } else {
            return false;
        }
    }

}
