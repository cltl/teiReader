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

        // nodes with movable, non-final page breaks before shifting page breaks
        List<IText> nodesWithMovablePageBreaks = getNodesWithMovablePageBreaks(nodesWithPageBreaks);
        assertEquals(nodesWithMovablePageBreaks.size(), 11);
        int movableNodesNotEndingWithPageBreak = getNodesWithChildrenNotEndingWithPageBreak(nodesWithMovablePageBreaks);
        assertEquals(movableNodesNotEndingWithPageBreak, 11);
        int totalNodesNotEndingWithPageBreak = getNodesWithChildrenNotEndingWithPageBreak(nodesWithPageBreaks);
        assertEquals(totalNodesNotEndingWithPageBreak, 15);

        handler.process();

        // all nodes with movable page-breaks now end with page breaks
        nodesWithPageBreaks = getAllNodesWithPageBreaks(handler);
        movableNodesNotEndingWithPageBreak = getNodesWithChildrenNotEndingWithPageBreak(getNodesWithMovablePageBreaks(nodesWithPageBreaks));
        assertEquals(movableNodesNotEndingWithPageBreak, 0);
        totalNodesNotEndingWithPageBreak = getNodesWithChildrenNotEndingWithPageBreak(nodesWithPageBreaks);
        assertEquals(totalNodesNotEndingWithPageBreak, 4);

    }

    private int getNodesWithChildrenNotEndingWithPageBreak(List<IText> nodesWithPageBreaks) {
        return (int) nodesWithPageBreaks.stream().map(t -> (TextTree) t)
                .filter(t -> !(t.getChildren().get(t.getChildren().size() - 1) instanceof PageBreak)).count();
    }

    private List<IText> getNodesWithMovablePageBreaks(List<IText> nodesWithPageBreaks) {
        return nodesWithPageBreaks.stream().map(t -> (TextTree) t).filter(t -> t.hasMovablePageBreaks()).collect(Collectors.toList());
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
        // all FootNote objects have been replaced by ATextTree objects
        assertEquals(getAllNodesWithNotes(handler.getTextTree()).size(), 0);
        // the nodes that contained Notes get the notes' trees as added children

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

        // all FootNote objects have been replaced by ATextTree objects
        assertEquals(getAllNodesWithNotes(handler.getTextTree()).size(), 0);
        // the nodes that contained Notes get the notes' trees as added children
        assertEquals(getChildrenCount(nodesWithNotes), noteCountBeforeShifting + childrenCountBeforeShifting);

    }
}