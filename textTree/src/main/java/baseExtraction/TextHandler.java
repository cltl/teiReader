package baseExtraction;

import textTree.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextHandler implements ChildVisitor {
    boolean resolvePageBreaks;
    boolean extractFootNotes;


    TextHandler(boolean extractFootNotes, boolean resolvePageBreaks) {

        this.extractFootNotes = extractFootNotes;
        this.resolvePageBreaks = resolvePageBreaks;
    }

    public static TextHandler create(boolean extractFootNotes, boolean resolvePageBreaks) {
        return new TextHandler(extractFootNotes, resolvePageBreaks);
    }

    public IText process(IText textTree) {
        textTree.accept(this);
        return textTree;
    }

    @Override
    public void visit(TextTree tree) {
        List<IText> children = tree.getChildren();
        if (extractFootNotes)
            children = extractNotes(children);
        if (resolvePageBreaks)
            children = resolvePageBreakTransitions(children);

        tree.setChildren(children);
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
                    Pattern p = Pattern.compile(".*[a-w]-$");   // excludes money amounts in missives
                    if (p.matcher(rightMostLeaf.getContent()).matches()) {
                        System.out.println(rightMostLeaf.getTeiId());
                        rightMostLeaf.setContent(rightMostLeaf.getContent().substring(0, rightMostLeaf.getContent().length() - 1));
                        ATextTree nextText = (ATextTree) children.remove(nextTextIndex);
                        ATextTree currentText = (ATextTree) children.remove(textIndex);
                        children.add(textIndex, ATextTree.join(currentText, nextText));
                    }
                }
            }
            pageBreak = findIndex(children, c -> c instanceof PageBreak, pageBreak + 1);
        }
        return children;
    }

}
