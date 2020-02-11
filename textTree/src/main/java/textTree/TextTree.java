package textTree;

import baseExtraction.ChildVisitor;

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

    TextTree(List<IText> children, String separator, String prefix, String suffix, String teiId) {
        super(prefix, suffix, teiId);
        this.children = children;
        this.separator = separator;
        if (teiId != null)
            passId();
    }

    public static TextTree create(List<IText> children, String separator, String prefix, String suffix, String teiId) {
        return new TextTree(children, separator, prefix, suffix, teiId);
    }

    public static TextTree create(List<IText> children, String separator, String prefix, String suffix) {
        return new TextTree(children, separator, prefix, suffix, null);
    }

    public List<IText> getChildren() {
        return children;
    }

    public void setChildren(List<IText> children) {
        this.children = children;
    }


    /**
     * Passes TextTree TEI ids down to children with null TEI ids (missives)
     * @return
     */
    private void passId() {
        for (IText child: children) {
            if (child instanceof ATextTree && ((ATextTree) child).getTeiId() == null)
                ((ATextTree) child).setTeiId(teiId);
        }
    }

    @Override
    public ATextTree with(String prefix, String suffix) {
        return new TextTree(this.children, this.separator, prefix, suffix, this.teiId);
    }

    @Override
    public String content() {
        return prefix + children.stream().map(IText::content).filter(c -> c != null).collect(Collectors.joining(separator)) + suffix;
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


    @Override
    public void accept(ChildVisitor visitor) {
        visitor.visit(this);
        children.stream().forEach(c -> c.accept(visitor));
    }



    public TextLeaf rightMostLeaf() {
        // TODO check TextTree depth is actually 1 when calling this method
        // Perhaps better to filter ATextTree instances first, and then get the BOTTOM-right leaf
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
