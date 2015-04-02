package edu.cmu.lti.utils.general;

import java.util.Collection;
import java.util.List;

/**
 * A utility class for handling CSV files. This class relies not on StringBuffer but on
 * StringBuilder, so expects to be run by a single thread.
 *
 * @author Jun Araki
 */
public class CsvUtils extends AbstractDelimiterSeparatedValuesUtils {

    static {
        separator = ",";
    }

    /**
     * Appends the specified object with a comma to the specified buffer.
     *
     * @param buf
     * @param value
     */
    public static void appendValueWithComma(StringBuilder buf, Object value) {
        appendValueWithComma(buf, value, 0);
    }

    /**
     * Appends the specified object with a comma to the specified buffer. This method puts additional
     * white space with the specified length in front of the string.
     *
     * @param buf
     * @param value
     * @param whiteSpaceLength
     */
    public static void appendValueWithComma(StringBuilder buf, Object value, int whiteSpaceLength) {
        appendValueWithComma(buf, value, whiteSpaceLength, false);
    }

    /**
     * Appends the specified object with a comma to the specified buffer. This method puts additional
     * white space with the specified length in front of the specified string. This method can choose
     * to add double quotations or not with the specified flag.
     *
     * @param buf
     * @param value
     * @param whiteSpaceLength
     * @param addDoubleQuotations
     */
    public static void appendValueWithComma(StringBuilder buf, Object value, int whiteSpaceLength,
                                            boolean addDoubleQuotations) {
        appendValueWithSeparator(buf, value, whiteSpaceLength, addDoubleQuotations, separator);
    }

    /**
     * Appends the specified collection of objects with separators and a line break to the specified
     * buffer.
     *
     * @param buf
     * @param values
     */
    public static void appendCollection(StringBuilder buf, Collection<Object> values) {
        appendCollection(buf, values, 0);
    }

    /**
     * Appends the specified collection of objects with separators and a line break to the specified
     * buffer. This method puts additional white space with the specified length in front of the
     * specified string.
     *
     * @param buf
     * @param values
     * @param whiteSpaceLength
     */
    public static void appendCollection(StringBuilder buf, Collection<Object> values,
                                        int whiteSpaceLength) {
        appendCollection(buf, values, whiteSpaceLength, false);
    }

    /**
     * Appends the specified collection of objects with separators and a line break to the specified
     * buffer. This method puts additional white space with the specified length in front of the
     * specified string. This method can choose to add double quotations or not with the specified
     * flag.
     *
     * @param buf
     * @param values
     * @param whiteSpaceLength
     * @param addDoubleQuotations
     */
    public static void appendCollection(StringBuilder buf, Collection<Object> values,
                                        int whiteSpaceLength, boolean addDoubleQuotations) {
        appendCollection(buf, values, whiteSpaceLength, addDoubleQuotations, separator);
    }

    /**
     * Appends the specified array of objects with separators and a line break to the specified
     * buffer.
     *
     * @param buf
     * @param values
     */
    public static void appendArray(StringBuilder buf, Object[] values) {
        appendArray(buf, values, 0);
    }

    /**
     * Appends the specified array of objects with separators and a line break to the specified
     * buffer. This method puts additional white space with the specified length in front of the
     * specified string.
     *
     * @param buf
     * @param values
     * @param whiteSpaceLength
     */
    public static void appendArray(StringBuilder buf, Object[] values, int whiteSpaceLength) {
        appendArray(buf, values, whiteSpaceLength, false);
    }

    /**
     * Appends the specified array of objects with separators and a line break to the specified
     * buffer. This method puts additional white space with the specified length in front of the
     * specified string. This method can choose to add double quotations or not with the specified
     * flag.
     *
     * @param buf
     * @param values
     * @param whiteSpaceLength
     */
    public static void appendArray(StringBuilder buf, Object[] values, int whiteSpaceLength,
                                   boolean addDoubleQuotations) {
        appendArray(buf, values, whiteSpaceLength, addDoubleQuotations, separator);
    }

    /**
     * Appends the specified collection of objects with separators and a line break to the specified
     * file.
     *
     * @param fileName
     * @param values
     */
    public static void appendCollectionToFile(String fileName, Collection<Object> values) {
        appendCollectionToFile(fileName, values, 0);
    }

    /**
     * Appends the specified collection of objects with separators and a line break to the specified
     * file. This method puts additional white space with the specified length in front of the
     * specified string.
     *
     * @param fileName
     * @param values
     * @param whiteSpaceLength
     */
    public static void appendCollectionToFile(String fileName, Collection<Object> values,
                                              int whiteSpaceLength) {
        appendCollectionToFile(fileName, values, whiteSpaceLength, false);
    }

    /**
     * Appends the specified collection of objects with separators and a line break to the specified
     * file. This method puts additional white space with the specified length in front of the
     * specified string. This method can choose to add double quotations or not with the specified
     * flag.
     *
     * @param fileName
     * @param values
     * @param whiteSpaceLength
     * @param addDoubleQuotations
     */
    public static void appendCollectionToFile(String fileName, Collection<Object> values,
                                              int whiteSpaceLength, boolean addDoubleQuotations) {
        appendCollectionToFile(fileName, values, whiteSpaceLength, addDoubleQuotations, separator);
    }

    /**
     * Returns strings split in the format of a list.
     *
     * @param delimStr
     * @return strings split in the format of a list
     */
    public static List<String> splitDelimiterSeparatedValues(String delimStr) {
        return splitDelimiterSeparatedValues(delimStr, separator);
    }

    /**
     * Returns a list of strings corresponding to the specified line in the specified lines.
     *
     * @param delimStrLines
     * @param lineNo
     * @return a list of strings corresponding to the specified line in the specified lines
     */
    public static List<String> getLineData(String delimStrLines, int lineNo) {
        return getLineData(delimStrLines, lineNo, separator);
    }

    /**
     * Returns a list of strings corresponding to the specified line in the specified file.
     *
     * @param delimStrFilePath
     * @param lineNo
     * @return a list of strings corresponding to the specified line in the specified file
     */
    public static List<String> getLineDataFromFile(String delimStrFilePath, int lineNo) {
        return getLineDataFromFile(delimStrFilePath, lineNo, separator);
    }

    /**
     * Returns all lists of strings corresponding to the specified line.
     *
     * @param delimStrLines
     * @return all lists of strings corresponding to the specified line
     */
    public static List<List<String>> getAllLineData(String delimStrLines) {
        return getAllLineData(delimStrLines, separator);
    }

    /**
     * Returns all lists of strings corresponding to the specified file.
     *
     * @param delimStrFilePath
     * @return all lists of strings corresponding to the specified file
     */
    public static List<List<String>> getAllLineDataFromFile(String delimStrFilePath) {
        return getAllLineDataFromFile(delimStrFilePath, separator);
    }

    /**
     * Returns all lists of strings corresponding to the specified file. The third argument can
     * specify whether or not you contain double quotes that surround an actual value.
     *
     * @param delimStrFilePath
     * @param containDoubleQuotation
     * @return all lists of strings corresponding to the specified file
     */
    public static List<List<String>> getAllLineDataFromFile(String delimStrFilePath,
                                                            boolean containDoubleQuotation) {
        return getAllLineDataFromFile(delimStrFilePath, separator, containDoubleQuotation);
    }

}
