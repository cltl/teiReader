package teiExtraction;

import baseExtraction.TextHandler;
import org.junit.jupiter.api.Test;
import textTree.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextExtracterTest {

    static String testDir = "src/test/resources/";
    static String testFile = testDir + "test.xml";
    static String testFile2 = testDir + "test-pb.xml";
    static TextHandler handler;

    @Test
    public void testShortTEIExtraction() {
        TextExtracter te = new TextExtracter();
        handler = te.createTextHandler(testFile, false, false);
        IText ttree = handler.getTextTree();
        assertEquals(ttree.findAll(t -> t instanceof PageBreak).size(), 1);
        assertEquals(ttree.findAll(t -> t instanceof TextLeaf).size(), 12);
    }

    @Test
    public void testPageMerging() {
        TextExtracter te = new TextExtracter();
        handler = te.createTextHandler(testFile2, false, false);
        IText ttree = handler.getTextTree();
        List<IText> ttrees = ttree.findAll(t -> t instanceof TextTree);

        // Tree containing page break
        TextTree pbTree = (TextTree) ttrees.get(2);
        int pbIndex = handler.findIndex(pbTree.getChildren(), t -> t instanceof PageBreak, 1);
        int textIndex = handler.findLastIndex(pbTree.getChildren(), t -> t instanceof ATextTree, pbIndex);
        TextLeaf rightmostLeaf = ((ATextTree) pbTree.getChildren().get(textIndex)).rightMostLeaf();
        assert rightmostLeaf.getContent().endsWith("-");

        // contents prior to modification
        String content1WithoutHyphen = rightmostLeaf.getContent().substring(0, rightmostLeaf.getContent().length() - 1);
        String content2 = pbTree.getChildren().get(handler.findIndex(pbTree.getChildren(), t -> t instanceof ATextTree, pbIndex)).content();

        handler.process();
        pbTree = (TextTree) handler.getTextTree().findAll(t -> t instanceof TextTree).get(2);
        assertTrue(pbTree.getChildren().get(textIndex).content().endsWith(content1WithoutHyphen + content2));

    }

}