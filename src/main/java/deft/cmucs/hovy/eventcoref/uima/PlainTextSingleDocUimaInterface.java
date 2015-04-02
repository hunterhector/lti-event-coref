package deft.cmucs.hovy.eventcoref.uima;

import adept.common.Document;
import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.fit.component.ViewCreatorAnnotator;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * Date: 3/26/15
 * Time: 10:07 PM
 *
 * @author Zhengzhong Liu
 */
public class PlainTextSingleDocUimaInterface extends AbstractSingleDocAdept2UimaInterface {
    public String encoding = "UTF-8";

    public static final String goldStandardViewName = "GoldStandard";
    public static final Set<String> srcDocInfoViewNames = new HashSet<>();
    public static final String inputViewName = "Input";

    static {
        srcDocInfoViewNames.add(goldStandardViewName);
    }

    public PlainTextSingleDocUimaInterface(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public JCas getJCas(Document document) throws UIMAException {
        List<JCas> srcDocInfoViews = new ArrayList<JCas>();

        String text = document.getValue();

        JCas aJCas = JCasFactory.createJCas();
        JCas inputView = null;

        try {
            if (inputViewName != null && !inputViewName.isEmpty()) {
                inputView = ViewCreatorAnnotator.createViewSafely(aJCas,
                        inputViewName);
            }
            if (srcDocInfoViewNames != null && !srcDocInfoViewNames.isEmpty()) {
                for (String srcDocInfoViewName : srcDocInfoViewNames) {
                    srcDocInfoViews.add(ViewCreatorAnnotator.createViewSafely(
                            aJCas, srcDocInfoViewName));
                }
                if (!srcDocInfoViewNames.contains(inputViewName)) {
                    // The input view should also have source document
                    // information.
                    srcDocInfoViews.add(inputView);
                }
            }
        } catch (Exception e) {
            throw new CollectionException(e);
        }

        // put document in CAS
        if (inputView != null) {
            // This view is intended to be used in order to put an original
            // document text to a view other
            // than the default view.
            inputView.setDocumentText(text);
        } else {
            aJCas.setDocumentText(text);
        }

        // Also store location of source document in CAS. This information is
        // critical
        // if CAS Consumers will need to know where the original document
        // contents are located.
        // For example, the Semantic Search CAS Indexer writes this information
        // into the
        // search index that it creates, which allows applications that use the
        // search index to
        // locate the documents that satisfy their semantic queries.
        SourceDocumentInformation srcDocInfo = new SourceDocumentInformation(
                aJCas);
        srcDocInfo.setUri(document.getUri());
        srcDocInfo.setOffsetInSource(0);
        srcDocInfo.setDocumentSize(text.length());
        srcDocInfo.addToIndexes();

        for (JCas view : srcDocInfoViews) {
            srcDocInfo = new SourceDocumentInformation(view);
            srcDocInfo.setUri(document.getUri());
            srcDocInfo.setOffsetInSource(0);
            srcDocInfo.setDocumentSize(text.length());
            srcDocInfo.addToIndexes();
        }

        return aJCas;
    }
}