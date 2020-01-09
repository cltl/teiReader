package teiExtraction;

import baseExtraction.TextHandler;
import org.junit.jupiter.api.Test;
import textTree.IText;
import textTree.PageBreak;
import textTree.TextLeaf;

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
        assertEquals(ttree.findAll(t -> t instanceof TextLeaf).size(), 24);
    }

    @Test
    public void testPageMerging() {
        TextExtracter te = new TextExtracter();
        handler = te.createTextHandler(testFile2, false, false);
        IText ttree = handler.getTextTree();
        assertNotNull(ttree);
    }

}