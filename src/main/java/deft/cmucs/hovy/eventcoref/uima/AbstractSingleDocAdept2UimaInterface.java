package deft.cmucs.hovy.eventcoref.uima;

import adept.common.Document;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;

/**
 * Created with IntelliJ IDEA.
 * Date: 3/26/15
 * Time: 8:52 PM
 *
 * @author Zhengzhong Liu
 */

public abstract class AbstractSingleDocAdept2UimaInterface {
    abstract public JCas getJCas(Document document) throws UIMAException;
}
