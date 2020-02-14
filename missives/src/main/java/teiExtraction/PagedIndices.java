package teiExtraction;

import baseExtraction.ChildVisitor;
import textTree.*;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class PagedIndices implements ChildVisitor {

    Pattern pageRef = Pattern.compile("[ ,\\d]+");
    Pattern hasPageRef = Pattern.compile("\\d+");
    Pattern hasTextRef = Pattern.compile("[;,] zie ");

    public PagedIndices() {}

    public IText process(IText textTree) {
        textTree.accept(this);
        return textTree;
    }

    @Override
    public void visit(TextTree tree) {
        List<IText> children = tree.getChildren();
        children = isolateEntries(children);
        children = trim(children);
        tree.setChildren(children);
    }

    private List<IText> trim(List<IText> children) {

        if (children.stream().anyMatch(c -> isLineBreak(c))) {
            List<IText> newChildren = new LinkedList<>();
            List<ATextTree> toMerge = new LinkedList<>();
            for (IText c: children) {
                if (isLineBreak(c)) {
                    newChildren.add(mergeTextBetweenLineBreaks(toMerge));
                    newChildren.add(c);
                    toMerge = new LinkedList<>();
                } else if (c instanceof ATextTree)
                    toMerge.add((ATextTree) c);
            }
            if (! toMerge.isEmpty())
                newChildren.add(mergeTextBetweenLineBreaks(toMerge));
            return newChildren;
        }
        return children;
    }

    private TextLeaf mergeTextBetweenLineBreaks(List<ATextTree> toMerge) {
        return TextLeaf.create(toMerge.stream().reduce(TextLeaf.create("", "", ""),
                (l1, l2) -> (ATextTree) ATextTree.join(l1, l2)).content().trim(), "", "");
    }

    private List<IText> isolateEntries(List<IText> children) {
        LinkedList<IText> newChildren = new LinkedList<>();
        for (IText c: children) {
            if (c instanceof NullText)
                continue;
            if (! newChildren.isEmpty() &&
                    followsOnEntry(c, newChildren))
                newChildren = updateEntry(newChildren, c);
            else
                newChildren.add(c);
        }
        return (List) newChildren;
    }

    private LinkedList<IText> updateEntry(LinkedList<IText> newChildren, IText c) {
        newChildren.removeLast();
        IText forelast = newChildren.removeLast();
        newChildren.add(ATextTree.join((ATextTree) forelast, (ATextTree) c));
        return newChildren;
    }

    private boolean isLineBreak(IText t) {
        return t instanceof TextLeaf && ((TextLeaf) t).getContent().equals("\n");
    }

    private boolean followsOnEntry(IText c, LinkedList<IText> newChildren) {
        return isLineBreak(newChildren.getLast())
                && (isPageReference(c)
                || previousEntryMissesReference(newChildren)
                || completesPreviousTextReference(c, newChildren));
    }

    private IText forelast(LinkedList<IText> items) {
        return items.get(items.size() - 2);
    }

    private boolean completesPreviousTextReference(IText c, LinkedList<IText> newChildren) {
        return c.content() != null && hasNoReference(c) && hasTextRef.matcher(forelast(newChildren).content()).find();
    }

    private boolean previousEntryMissesReference(LinkedList<IText> newChildren) {
        return hasNoReference(forelast(newChildren));
    }

    private boolean isPageReference(IText c) {
        return c.content() != null && pageRef.matcher(c.content()).matches();
    }

    private boolean hasNoReference(IText t) {
        String c = t.content();
        return ! (hasPageRef.matcher(c).find()
                || hasTextRef.matcher(c).find());

    }
}
