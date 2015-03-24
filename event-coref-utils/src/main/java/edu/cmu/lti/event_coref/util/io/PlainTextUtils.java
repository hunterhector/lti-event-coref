package edu.cmu.lti.event_coref.util.io;

import edu.cmu.lti.util.general.ErrorUtils;
import edu.cmu.lti.util.general.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides convenient utilities for text processing such as detagging.
 *
 * @author Jun Araki
 */
public class PlainTextUtils {
    private static final Logger logger = LoggerFactory.getLogger(PlainTextUtils.class);

    /**
     * Returns a part of the specified text surrounded by the specified tag.
     *
     * @param text
     * @param tagName
     * @param allowEmpty
     * @return a part of the specified text surrounded by the specified tag
     */
    public static String findTagAnnotation(String text, String tagName, boolean allowEmpty) {
        String regex = "(<" + tagName + ".*>(.+)</" + tagName + ">)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);

        if (m.find()) {
            String annotatedStr = m.group(2);
            if (!allowEmpty) {
                ErrorUtils.terminateIfTrue(StringUtils.isNullOrEmptyString(annotatedStr),
                        "Empty annotation: " + annotatedStr);
            }

            return m.group(1);
        }

        return null;
    }

    /**
     * Removes all tags of the specified input.
     *
     * @param input
     * @return a string where all tags of the specified input are removed
     */
    public static String detag(String input) {
        List<String> allTags = getAllTags(input);
        for (String tag : allTags) {
            input = replaceWithWhiteSpace(input, tag);
        }

        return input;
    }

    /**
     * Replaces the specified input with the same length as the tag annotation.
     *
     * @param input
     * @return the specified input with the same length as the tag annotation
     */
    public static String replaceWithWhiteSpace(String input, String tagAnnotation) {
        if (StringUtils.isNullOrEmptyString(tagAnnotation)) {
            logger.warn("The specified tag annotation is empty.");
            return input;
        }

        input = input.replaceAll(tagAnnotation, generateWhiteSpace(tagAnnotation));
        return input;
    }

    /**
     * Replaces the specified input with the specified tag annotations.
     *
     * @param input
     * @param tagAnnotations
     * @return the specified input with the specified tag annotations
     */
    public static String replaceWithWhiteSpace(String input, Set<String> tagAnnotations) {
        for (String tagAnnotation : tagAnnotations) {
            input = replaceWithWhiteSpace(input, tagAnnotation);
        }
        return input;
    }

    /**
     * Returns a sequence of whitespace with the same length as the specified tag.
     *
     * @param tag
     * @return a sequence of whitespace with the same length as the specified tag
     */
    public static String generateWhiteSpace(String tag) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < tag.length(); i++) {
            buf.append(" ");
        }

        return buf.toString();
    }

    public static List<String> getAllTags(String input) {
        List<String> result = new ArrayList<String>();
        String tmp = input;
        while (findTag(tmp) != null) {
            int[] offset = findTag(tmp);
            String tag = tmp.substring(offset[0], offset[1] + 1);
            result.add(tag);
            tmp = tmp.substring(offset[1] + 1, tmp.length());
        }

        return result;
    }

    /**
     * Returns the begin and end offsets of the specified tag.
     *
     * @param tag
     * @return the begin and end offsets of the specified tag
     */
    public static int[] findTag(String tag) {
        int[] result = null;
        int start = -1;
        int end = -1;

        start = tag.indexOf('<');
        if (start >= 0) {
            end = tag.indexOf('>', start);
        }

        if (start >= 0 && end > start) {
            result = new int[2];
            result[0] = start;
            result[1] = end;
        }

        return result;
    }

}
