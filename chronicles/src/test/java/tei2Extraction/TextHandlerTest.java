package tei2Extraction;

import baseExtraction.TextHandler;
import textTree.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextHandlerTest {

    static String testDir = "src/test/resources/";
    static String testFile = testDir + "diary.xml";
    static String testFile2 = testDir + "embed_notes.xml";
    static TextHandler handler;


    @Test
    public void testShiftPageBreaks() {
        TextExtracter te = new TextExtracter();
        handler = te.createTextHandler(testFile, false, false);

        List<IText> nodesWithPageBreaks = getAllNodesWithPageBreaks(handler);
        assertEquals(nodesWithPageBreaks.size(), 15);

        // test page-break count
        int pageBreaksFromNodes = (int) nodesWithPageBreaks.stream().map(t -> ((TextTree) t).getChildren()).flatMap(x -> x.stream()).filter(x -> x instanceof PageBreak).count();
        int totalPageBreakCount = handler.getTextTree().findAll(x -> x instanceof PageBreak).size();
        assertEquals(totalPageBreakCount, 16);
        assertEquals(totalPageBreakCount, pageBreaksFromNodes);

        // nodes with non-final page breaks
        int totalNodesNotEndingWithPageBreak = getNodesWithChildrenNotEndingWithPageBreak(nodesWithPageBreaks);
        assertEquals(totalNodesNotEndingWithPageBreak, 15);

        handler.process();

        // page breaks are not shifted
        nodesWithPageBreaks = getAllNodesWithPageBreaks(handler);
        totalNodesNotEndingWithPageBreak = getNodesWithChildrenNotEndingWithPageBreak(nodesWithPageBreaks);
        assertEquals(totalNodesNotEndingWithPageBreak, 15);

    }

    private int getNodesWithChildrenNotEndingWithPageBreak(List<IText> nodesWithPageBreaks) {
        return (int) nodesWithPageBreaks.stream().map(t -> (TextTree) t)
                .filter(t -> !(t.getChildren().get(t.getChildren().size() - 1) instanceof PageBreak)).count();
    }


    private List<IText> getAllNodesWithPageBreaks(TextHandler handler) {
        return handler.getTextTree().findAll(t -> t instanceof TextTree
                && ((TextTree) t).getChildren().stream().anyMatch(c -> c instanceof PageBreak));
    }

    @Test
    public void testShiftNotes() {
        TextExtracter te = new TextExtracter();
        handler = te.createTextHandler(testFile, false, true);

        List<IText> nodesWithNotes = getAllNodesWithNotes(handler.getTextTree());
        assertEquals(nodesWithNotes.size(), 4);

        int noteCountBeforeShifting = (int) nodesWithNotes.stream().map(t -> ((TextTree) t).getChildren()).flatMap(x -> x.stream()).filter(c -> c instanceof FootNote).count();
        assertTrue(noteCountBeforeShifting >= nodesWithNotes.size());
        int childrenCountBeforeShifting = getChildrenCount(nodesWithNotes);
        handler.process();
        // FootNote objects still exist
        assertEquals(getAllNodesWithNotes(handler.getTextTree()).size(), 4);
        // they are added to the previous children count after extraction

        assertEquals(getChildrenCount(nodesWithNotes), noteCountBeforeShifting + childrenCountBeforeShifting);
    }

    private int getChildrenCount(List<IText> nodesWithNotes) {
        return (int) nodesWithNotes.stream().map(t -> ((TextTree) t).getChildren()).flatMap(x -> x.stream()).count();
    }

    private List<IText> getAllNodesWithNotes(IText textTree) {
        List<IText> textTreesWithNotes = textTree.findAll(t -> t instanceof TextTree
                && ((TextTree) t).getChildren().stream().anyMatch(c -> c instanceof FootNote));

        return textTreesWithNotes;
    }

    @Test
    public void testEmbeddedNotes() {
        TextExtracter te = new TextExtracter();
        handler = te.createTextHandler(testFile2, false, true);

        List<IText> nodesWithNotes = getAllNodesWithNotes(handler.getTextTree());
        assertEquals(nodesWithNotes.size(), 5);

        int noteCountBeforeShifting = (int) nodesWithNotes.stream()
                .map(t -> ((TextTree) t).getChildren()).flatMap(x -> x.stream()).filter(c -> c instanceof FootNote).count();

        assertTrue(noteCountBeforeShifting >= nodesWithNotes.size());
        assertEquals(noteCountBeforeShifting, 27);
        int childrenCountBeforeShifting = getChildrenCount(nodesWithNotes);
        handler.process();

        // FootNote objects still exist
        assertEquals(getAllNodesWithNotes(handler.getTextTree()).size(), 5);
        // the nodes are added to the previous children count
        assertEquals(getChildrenCount(nodesWithNotes), noteCountBeforeShifting + childrenCountBeforeShifting);

    }
}