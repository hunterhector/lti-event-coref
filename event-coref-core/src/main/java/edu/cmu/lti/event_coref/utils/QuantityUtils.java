/**
 *
 */
package edu.cmu.lti.event_coref.utils;

import edu.cmu.lti.event_coref.type.NumberAnnotation;

/**
 * @author Zhengzhong Liu, Hector
 */
public class QuantityUtils {
    public static Double numberCompare(NumberAnnotation numberAnno1, NumberAnnotation numberAnno2) throws Exception {
        if (numberAnno1 != null && numberAnno2 != null) {
            String numberStr1 = numberAnno1.getNormalizedString();
            String numberStr2 = numberAnno2.getNormalizedString();

            try {
                double number1 = Double.parseDouble(numberStr1);
                double number2 = Double.parseDouble(numberStr2);
                if (number1 == number2) {
                    return 0.0;
                }
                if (number1 > number2) {
                    return 1.0;
                }
                if (number2 > number1) {
                    return -1.0;
                }
            } catch (NumberFormatException e) {
                throw new Exception(String.format("Number [%s, %s] cannot be format", numberStr1, numberStr2), e);
            }
        }

        return null;
    }
}
