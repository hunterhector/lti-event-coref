package edu.cmu.cs.lti.ark.fn.pipeline.parsing;

import edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence;

/**
 * Created with IntelliJ IDEA.
 * Date: 1/24/15
 * Time: 1:16 PM
 *
 * @author Zhengzhong Liu
 */
public interface ParserImpl {
    public Sentence parse(Sentence input) throws ParsingException;
}
