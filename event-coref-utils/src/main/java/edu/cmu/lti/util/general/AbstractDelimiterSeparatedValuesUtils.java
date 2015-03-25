package edu.cmu.lti.util.general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * An abstract utility class for handling delimiter-separated values.
 *
 * @author Jun Araki
 */
public class AbstractDelimiterSeparatedValuesUtils {

    protected static String separator;

    /**
     * Returns the specified string escaped from special characters.
     *
     * @param input
     * @return the specified string escaped from special characters
     */
    public static String escapeSpecialCharacters(String input) {
        String output = input.replaceAll(System.lineSeparator(), " ");

        StringBuilder buf = new StringBuilder();
        buf.append("\\");
        buf.append('"');
        String oldStr = buf.toString();
        String newStr = oldStr + oldStr;
        output = output.replaceAll(oldStr, newStr);

        return output;
    }

    /**
     * Returns the specified string with double quotations.
     *
     * @param input
     * @return the specified string with double quotations
     */
    public static String addDoubleQuotations(String input) {
        StringBuilder buf = new StringBuilder();
        buf.append('"');
        buf.append(input);
        buf.append('"');

        return buf.toString();
    }

    /**
     * Returns the string canonicalized from the specified string for CSV data.
     *
     * @param input
     * @param addDoubleQuotations
     * @return the string canonicalized from the specified string for CSV data
     */
    public static String canonicalizeString(String input, boolean addDoubleQuotations) {
        if (addDoubleQuotations) {
            return addDoubleQuotations(escapeSpecialCharacters(input));
        } else {
            return escapeSpecialCharacters(input);
        }
    }

    /**
     * Appends the specified object with a separator to the specified buffer.
     *
     * @param buf
     * @param value
     */
    protected static void appendValueWithSeparator(StringBuilder buf, Object value) {
        appendValueWithSeparator(buf, value, 0);
    }

    /**
     * Appends the specified object with a separator to the specified buffer. This method puts
     * additional white space with the specified length in front of the string.
     *
     * @param buf
     * @param value
     * @param whiteSpaceLength
     */
    protected static void appendValueWithSeparator(StringBuilder buf, Object value,
                                                   int whiteSpaceLength) {
        appendValueWithSeparator(buf, value, whiteSpaceLength, false);
    }

    /**
     * Appends the specified object with a separator to the specified buffer. This method puts
     * additional white space with the specified length in front of the specified string. This method
     * can choose to add double quotations or not with the specified flag.
     *
     * @param buf
     * @param value
     * @param whiteSpaceLength
     * @param addDoubleQuotations
     */
    protected static void appendValueWithSeparator(StringBuilder buf, Object value,
                                                   int whiteSpaceLength, boolean addDoubleQuotations) {
        appendValueWithSeparator(buf, value, whiteSpaceLength, addDoubleQuotations, separator);
    }

    /**
     * Appends the specified object with a separator to the specified buffer. This method puts
     * additional white space with the specified length in front of the specified string. This method
     * can choose to add double quotations or not with the specified flag. This method can also
     * specify a separator.
     *
     * @param buf
     * @param value
     * @param whiteSpaceLength
     * @param addDoubleQuotations
     * @param separator
     */
    public static void appendValueWithSeparator(StringBuilder buf, Object value,
                                                int whiteSpaceLength, boolean addDoubleQuotations, String separator) {
        buf.append(StringUtils.getStringOfWhiteSpace(whiteSpaceLength));
        String s = StringUtils.getInitializedString(value);
        buf.append(canonicalizeString(s, addDoubleQuotations));
        buf.append(separator);
    }

    /**
     * Appends the specified object with a line break to the specified buffer.
     *
     * @param buf
     * @param value
     */
    public static void appendValueWithLineBreak(StringBuilder buf, Object value) {
        appendValueWithLineBreak(buf, value, 0);
    }

    /**
     * Appends the specified object with a line break to the specified buffer. This method puts
     * additional white space with the specified length in front of the specified string.
     *
     * @param buf
     * @param value
     * @param whiteSpaceLength
     */
    public static void appendValueWithLineBreak(StringBuilder buf, Object value, int whiteSpaceLength) {
        appendValueWithLineBreak(buf, value, whiteSpaceLength, false);
    }

    /**
     * Appends the specified object with a line break to the specified buffer. This method puts
     * additional white space with the specified length in front of the specified string. This method
     * can choose to add double quotations or not with the specified flag.
     *
     * @param buf
     * @param value
     * @param whiteSpaceLength
     * @param addDoubleQuotations
     */
    public static void appendValueWithLineBreak(StringBuilder buf, Object value,
                                                int whiteSpaceLength, boolean addDoubleQuotations) {
        buf.append(StringUtils.getStringOfWhiteSpace(whiteSpaceLength));
        String s = StringUtils.getInitializedString(value);
        buf.append(canonicalizeString(s, addDoubleQuotations));
        buf.append(System.lineSeparator());
    }

    /**
     * Appends the specified object with a line break to the specified file.
     *
     * @param fileName
     * @param value
     */
    public static void appendValueWithLineBreakToFile(String fileName, Object value) {
        appendValueWithLineBreakToFile(fileName, value, 0);
    }

    /**
     * Appends the specified file with a line break to the specified buffer. This method puts
     * additional white space with the specified length in front of the specified string.
     *
     * @param fileName
     * @param value
     * @param whiteSpaceLength
     */
    public static void appendValueWithLineBreakToFile(String fileName, Object value,
                                                      int whiteSpaceLength) {
        appendValueWithLineBreakToFile(fileName, value, whiteSpaceLength, false);
    }

    /**
     * Appends the specified object with a line break to the specified file. This method puts
     * additional white space with the specified length in front of the specified string. This method
     * can choose to add double quotations or not with the specified flag. This method can also
     * specify a separator.
     *
     * @param fileName
     * @param value
     * @param whiteSpaceLength
     * @param addDoubleQuotations
     */
    public static void appendValueWithLineBreakToFile(String fileName, Object value,
                                                      int whiteSpaceLength, boolean addDoubleQuotations) {
        StringBuilder buf = new StringBuilder();
        appendValueWithLineBreak(buf, value, whiteSpaceLength, addDoubleQuotations);
        FileUtils.appendFile(fileName, buf.toString());
    }

    /**
     * Appends the specified collection of objects with separators and a line break to the specified
     * buffer. This method puts additional white space with the specified length in front of the
     * specified string. This method can choose to add double quotations or not with the specified
     * flag. This method can also specify a separator.
     *
     * @param buf
     * @param values
     * @param whiteSpaceLength
     * @param addDoubleQuotations
     * @param separator
     */
    public static void appendCollection(StringBuilder buf, Collection<Object> values,
                                        int whiteSpaceLength, boolean addDoubleQuotations, String separator) {
        int size = values.size();
        int i = 0;
        for (Object value : values) {
            if (i < size - 1) {
                appendValueWithSeparator(buf, value, whiteSpaceLength, addDoubleQuotations, separator);
            } else {
                appendValueWithLineBreak(buf, value, whiteSpaceLength, addDoubleQuotations);
            }
            i++;
        }
    }

    /**
     * Appends the specified array of objects with separator and a line break to the specified buffer.
     * This method puts additional white space with the specified length in front of the specified
     * string. This method can choose to add double quotations or not with the specified flag. This
     * method can also specify a separator.
     *
     * @param buf
     * @param values
     * @param whiteSpaceLength
     * @param addDoubleQuotations
     * @param separator
     */
    public static void appendArray(StringBuilder buf, Object[] values, int whiteSpaceLength,
                                   boolean addDoubleQuotations, String separator) {
        appendCollection(buf, Arrays.asList(values), whiteSpaceLength, addDoubleQuotations, separator);
    }

    /**
     * Appends the specified collection of objects with separators and a line break to the specified
     * file. This method puts additional white space with the specified length in front of the
     * specified string. This method can choose to add double quotations or not with the specified
     * flag. This method can also specify a separator.
     *
     * @param fileName
     * @param values
     * @param whiteSpaceLength
     * @param addDoubleQuotations
     * @param separator
     */
    public static void appendCollectionToFile(String fileName, Collection<Object> values,
                                              int whiteSpaceLength, boolean addDoubleQuotations, String separator) {
        StringBuilder buf = new StringBuilder();
        appendCollection(buf, values, whiteSpaceLength, addDoubleQuotations, separator);
        FileUtils.appendFile(fileName, buf.toString());
    }

    /**
     * Returns strings split in the format of a list.
     *
     * @param delimStr
     * @param separator
     * @return strings split in the format of a list
     */
    protected static List<String> splitDelimiterSeparatedValues(String delimStr, String separator) {
        return splitDelimiterSeparatedValues(delimStr, separator, true);
    }

    /**
     * Returns strings split in the format of a list. The third argument can specify whether or not
     * you contain double quotes that surround an actual value.
     *
     * @param delimStr
     * @param separator
     * @param containDoubleQuotation
     * @return strings split in the format of a list
     */
    protected static List<String> splitDelimiterSeparatedValues(String delimStr, String separator,
                                                                boolean containDoubleQuotation) {
        List<String> delimStrList = new ArrayList<String>();
        if (delimStr.contains(separator)) {
            String[] delimStrs = delimStr.split(separator);
            for (int i = 0; i < delimStrs.length; i++) {
                String value = delimStrs[i].trim();
                if (!containDoubleQuotation) {
                    if (!StringUtils.isNullOrEmptyString(value) && value.length() >= 2
                            && value.startsWith("\"")
                            && value.endsWith("\"")) {
                        // "data" should be data.
                        value = value.substring(1, value.length() - 1);
                    }
                }
                delimStrList.add(value);
            }
        }

        return delimStrList;
    }

    /**
     * Returns a list of strings corresponding to the specified line of the specified lines.
     *
     * @param delimStrLines
     * @param lineNo
     * @param separator
     * @return a list of strings corresponding to the specified line of the specified lines
     */
    protected static List<String> getLineData(String delimStrLines, int lineNo, String separator) {
        return getLineData(delimStrLines, lineNo, separator, true);
    }

    /**
     * Returns a list of strings corresponding to the specified line of the specified lines. The third
     * argument can specify whether or not you contain double quotes that surround an actual value.
     *
     * @param delimStrLines
     * @param lineNo
     * @param separator
     * @param containDoubleQuotation
     * @return a list of strings corresponding to the specified line of the specified lines
     */
    protected static List<String> getLineData(String delimStrLines, int lineNo, String separator,
                                              boolean containDoubleQuotation) {
        List<String> lines = StringUtils.splitToLines(delimStrLines);

        if (lines.size() < lineNo) {
            return (new ArrayList<String>());
        }

        return splitDelimiterSeparatedValues(lines.get(lineNo - 1), separator, containDoubleQuotation);
    }

    /**
     * Returns a list of strings corresponding to the specified line in the specified file.
     *
     * @param delimStrFilePath
     * @param lineNo
     * @param separator
     * @return a list of strings corresponding to the specified line in the specified file
     */
    protected static List<String> getLineDataFromFile(String delimStrFilePath, int lineNo,
                                                      String separator) {
        String delimFileContent = FileUtils.readFile(delimStrFilePath);
        return getLineData(delimFileContent, lineNo, separator, true);
    }

    /**
     * Returns a list of strings corresponding to the specified line in the specified file. The third
     * argument can specify whether or not you contain double quotes that surround an actual value.
     *
     * @param delimStrFilePath
     * @param lineNo
     * @param separator
     * @param containDoubleQuotation
     * @return a list of strings corresponding to the specified line in the specified file
     */
    protected static List<String> getLineDataFromFile(String delimStrFilePath, int lineNo,
                                                      String separator, boolean containDoubleQuotation) {
        String delimFileContent = FileUtils.readFile(delimStrFilePath);
        return getLineData(delimFileContent, lineNo, separator, containDoubleQuotation);
    }

    /**
     * Returns all lists of strings corresponding to the specified line.
     *
     * @param delimStrLines
     * @param separator
     * @return all lists of strings corresponding to the specified line
     */
    protected static List<List<String>> getAllLineData(String delimStrLines, String separator) {
        return getAllLineData(delimStrLines, separator, true);
    }

    /**
     * Returns all lists of strings corresponding to the specified line. The third argument can
     * specify whether or not you contain double quotes that surround an actual value.
     *
     * @param delimStrLines
     * @param separator
     * @param containDoubleQuotation
     * @return all lists of strings corresponding to the specified line
     */
    protected static List<List<String>> getAllLineData(String delimStrLines, String separator,
                                                       boolean containDoubleQuotation) {
        List<String> lines = StringUtils.splitToLines(delimStrLines);

        List<List<String>> allLineData = new ArrayList<List<String>>();
        for (String line : lines) {
            List<String> csvStringList = splitDelimiterSeparatedValues(line, separator,
                    containDoubleQuotation);
            if (ListUtils.isNullOrEmptyList(csvStringList)) {
                continue;
            }

            allLineData.add(csvStringList);
        }

        return allLineData;
    }

    /**
     * Returns all lists of strings corresponding to the specified file.
     *
     * @param delimStrFilePath
     * @param separator
     * @return all lists of strings corresponding to the specified file
     */
    protected static List<List<String>> getAllLineDataFromFile(String delimStrFilePath,
                                                               String separator) {
        return getAllLineDataFromFile(delimStrFilePath, separator, true);
    }

    /**
     * Returns all lists of strings corresponding to the specified file. The third argument can
     * specify whether or not you contain double quotes that surround an actual value.
     *
     * @param delimStrFilePath
     * @param separator
     * @param containDoubleQuotation
     * @return all lists of strings corresponding to the specified file
     */
    protected static List<List<String>> getAllLineDataFromFile(String delimStrFilePath,
                                                               String separator, boolean containDoubleQuotation) {
        String delimFileContent = FileUtils.readFile(delimStrFilePath);
        return getAllLineData(delimFileContent, separator, containDoubleQuotation);
    }

}
