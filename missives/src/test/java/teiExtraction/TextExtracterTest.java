package teiExtraction;

import baseExtraction.TextHandler;
import baseExtraction.TextWriter;
import org.junit.jupiter.api.Test;
import textTree.*;

import java.io.IOException;
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
        IText ttree = te.load(testFile);
        assertEquals(ttree.findAll(t -> t instanceof PageBreak).size(), 1);
        assertEquals(ttree.findAll(t -> t instanceof TextLeaf).size(), 12);
        TextWriter writer = TextWriter.create(false, false);
        try {
            writer.write(ttree, "data", "testFile_raw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPageMerging() {
        TextExtracter te = new TextExtracter();
        IText ttree = te.load(testFile2);
        handler = TextHandler.create(false, true);

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

        handler.process(ttree);
        pbTree = (TextTree) ttree.findAll(t -> t instanceof TextTree).get(2);
        assertTrue(pbTree.getChildren().get(textIndex).content().endsWith(content1WithoutHyphen + content2));
    }

    @Test
    public void testNoteSplitting() {
        TextExtracter te = new TextExtracter();
        IText ttree = te.load(testFile);
        int nodeCount = ttree.findAll(x -> true).size();
        int notesCount = ttree.findAll(x -> x instanceof FootNote).size();
        assertEquals(nodeCount, 54);
        assertEquals(ttree.findAll(x -> x instanceof PageBreak).size(), 1);
        assertEquals(ttree.findAll(x -> x instanceof NullText).size(), 12);
        assertEquals(ttree.findAll(x -> x instanceof TextTree).size(), 28);
        assertEquals(ttree.findAll(x -> x instanceof TextLeaf).size(), 12);
        assertEquals(notesCount, 1);

        FootNote note = (FootNote) ttree.findAll(x -> x instanceof FootNote).get(0);
        TextWriter writer = TextWriter.create(false, true);
        try {
            writer.write(ttree, "data", "testFile");
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(ttree.findAll(x -> true).size(), 54 - note.findAll(x -> true).size());
        assertEquals(ttree.findAll(x -> x instanceof FootNote).size(), 0);
    }
}