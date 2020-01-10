package baseExtraction;

import textTree.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TextHandler implements ChildVisitor {
    IText textTree;
    boolean paginate;
    final boolean resolvePageBreaks = true;
    boolean extractFootNotes;
    final String extension = ".txt";
    final String paginationID = "_p";

    TextHandler(IText textTree, boolean paginate, boolean extractFootNotes) {
        this.textTree = textTree;
        this.paginate = paginate;
        this.extractFootNotes = extractFootNotes;
    }

    public static TextHandler create(IText textTree, boolean paginate, boolean extractFootNotes) {
        return new TextHandler(textTree, paginate, extractFootNotes);
    }

    public IText getTextTree() {
        return textTree;
    }

    public void process() {
        visit(this.textTree);
    }

    public void write(String outFile) throws IOException {
        if (paginate) {
            outFile += paginationID;
            FileWriter fw = new FileWriter(new File(outFile + "0" + extension));
            fw = textTree.paginate(fw, outFile);
            fw.close();
        } else {
            FileWriter fw = new FileWriter(new File(outFile + extension));
            fw.write(textTree.content());
            fw.close();
        }
    }

    @Override
    public void visit(IText tree) {
        tree.accept(this);
    }

    @Override
    public List<IText> modifiesChildren(List<IText> children) {
        if (extractFootNotes)
            children = extractNotes(children);
        if (resolvePageBreaks)
            children = resolvePageBreakTransitions(children);
        return children;
    }

    /**
     * Lists footnotes, replaces footnotes in original location by their id, and appends complete footnotes to the list
     * of children.
     *
     * @param children
     * @return children with extracted notes
     */
    public List<IText> extractNotes(List<IText> children) {
               List<IText> footnotes = children.stream().filter(c -> c instanceof FootNote).collect(Collectors.toList());
        List<IText> newChildren = new LinkedList<>();
        for (IText c: children) {
            if (c instanceof FootNote)
                newChildren.add(TextLeaf.create(((FootNote) c).getId(), " [", "]"));
            else
                newChildren.add(c);
        }
        newChildren.addAll(footnotes);
        
        return newChildren;
    }


    /**
     * Merges text across page breaks if the last text before a page break ends with a hyphen.
     * Removes the hyphen, and moves the content of both text parts to the first text part.
     * @param children
     * @return
     */
    public List<IText> resolvePageBreakTransitions(List<IText> children) {
        int pageBreak = findIndex(children, c -> c instanceof PageBreak, 0);
        while (pageBreak != -1) {
            int textIndex = findLastIndex(children, c -> c instanceof ATextTree, pageBreak);
            int nextTextIndex = findIndex(children, c -> c instanceof ATextTree, pageBreak);
            if (textIndex != -1 && nextTextIndex != -1) {
                TextLeaf rightMostLeaf = ((ATextTree) children.get(textIndex)).rightMostLeaf();
                if (rightMostLeaf != null && rightMostLeaf.getContent().endsWith("-")) {
                    rightMostLeaf.setContent(rightMostLeaf.getContent().substring(0, rightMostLeaf.getContent().length() - 1));
                    ATextTree nextText = (ATextTree) children.remove(nextTextIndex);
                    ATextTree currentText = (ATextTree) children.remove(textIndex);
                    children.add(textIndex, ATextTree.join(currentText, nextText));
                }
            }
            pageBreak = findIndex(children, c -> c instanceof PageBreak, pageBreak + 1);
        }
        return children;
    }
}
