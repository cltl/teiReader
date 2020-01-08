package textTree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a TEI tree as a tree of text elements
 */
public class TextTree extends ATextTree {
    String separator;
    List<IText> children;
    boolean movablePageBreaks;

    TextTree(List<IText> children, String separator, String prefix, String suffix, boolean shiftPageBreaks) {
        super(prefix, suffix);
        this.children = children;
        this.separator = separator;
        this.movablePageBreaks = shiftPageBreaks;
    }

    public static TextTree create(List<IText> children, String separator, String prefix, String suffix, boolean shiftPageBreaks) {
        return new TextTree(children, separator, prefix, suffix, shiftPageBreaks);
    }
    public boolean hasMovablePageBreaks() {
        return movablePageBreaks;
    }

    public List<IText> getChildren() {
        return children;
    }

    @Override
    public ATextTree with(String prefix, String suffix) {
        return new TextTree(this.children, this.separator, prefix, suffix, this.movablePageBreaks);
    }

    @Override
    public String content() {
        return prefix + children.stream().map(IText::content).filter(c -> c != null).collect(Collectors.joining(separator)) + suffix;
    }

    @Override
    public void shiftChildren(boolean shiftPageBreaks, boolean shiftFootNotes) {
        if (children.stream().anyMatch(c -> c instanceof FootNote)) {
            shiftNotesOrChangeToText(shiftFootNotes);
        }
        if (movablePageBreaks && shiftPageBreaks && children.stream().anyMatch(c -> c instanceof PageBreak)) {
            resolvePageBreakTransitions();
            movePageBreaks();
        }
        children.stream().forEach(c -> c.shiftChildren(shiftPageBreaks, shiftFootNotes));
    }

    @Override
    public FileWriter paginate(FileWriter fw, String filePfx) {
        try {
            fw.write(prefix);
            fw = children.get(0).paginate(fw, filePfx);
            for (int i = 1; i < children.size(); i++) {
                fw.write(separator);
                fw = children.get(i).paginate(fw, filePfx);
            }
            fw.write(suffix);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fw;
    }

    private void shiftNotesOrChangeToText(boolean shiftFootNotes) {
        if (shiftFootNotes) {
            children.addAll(children.stream()
                    .filter(c -> c instanceof FootNote)
                    .map(c -> ((FootNote) c))
                    .map(fn -> fn.getTextTree().with("\n" + fn.content() + " ", ""))
                    .collect(Collectors.toList()));
            List<IText> newChildren = new LinkedList<>();
            for (IText c: children) {
                if (c instanceof FootNote)
                    newChildren.add(new TextLeaf(c.content(), " ", ""));
                else
                    newChildren.add(c);
            }
            children = newChildren;
        } else {
            List<IText> newChildren = new LinkedList<>();
            for (IText c: children) {
                if (c instanceof FootNote)
                    newChildren.add(((FootNote) c).getTextTree().with(" [" + ((FootNote) c).getId() + " ", "]"));
                else
                    newChildren.add(c);
            }
            children = newChildren;
        }
    }

    private void movePageBreaks() {
        List<IText> pageBreaks = children.stream().filter(c -> c instanceof PageBreak).collect(Collectors.toList());
        children.removeAll(pageBreaks);
        children.addAll(pageBreaks);    // moves page breaks to the end of the list
    }

    private void resolvePageBreakTransitions() {
        for (int i = 1; i < children.size(); i++) {
            if (children.get(i) instanceof PageBreak && children.get(i - 1) instanceof ATextTree) {
                TextLeaf rightMostLeaf = ((ATextTree) children.get(i - 1)).rightMostLeaf();
                if (rightMostLeaf != null && rightMostLeaf.getContent().endsWith("-"))
                    rightMostLeaf.setContent(rightMostLeaf.getContent().substring(0, rightMostLeaf.getContent().length() - 1));
            }
        }
    }

    protected TextLeaf rightMostLeaf() {
        List<TextLeaf> leaves = children.stream().filter(c -> c instanceof TextLeaf).map(c -> (TextLeaf) c).collect(Collectors.toList());
        if (leaves.isEmpty())
            return null;
        else
            return leaves.get(leaves.size() - 1);
    }


    @Override
    public List<IText> findAll(Predicate<IText> p) {
        List<IText> matching = new LinkedList<>();
        if (p.test(this))
            matching.add(this);
        matching.addAll(children.stream().map(c -> c.findAll(p)).flatMap(x -> x.stream()).collect(Collectors.toList()));
        return matching;
    }
}
