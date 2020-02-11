package textTree;

import baseExtraction.ChildVisitor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class TextLeaf extends ATextTree {
    String content;

    TextLeaf(String content, String prefix, String suffix, String teiId) {
        super(prefix, suffix, teiId);
        this.content = content;
    }

    public static TextLeaf create(String content, String prefix, String suffix) {
        return new TextLeaf(content, prefix, suffix, null);
    }

    public static TextLeaf create(String content, String prefix, String suffix, String teiId) {
        return new TextLeaf(content, prefix, suffix, teiId);
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public ATextTree with(String prefix, String suffix) {
        return new TextLeaf(this.content, prefix, suffix, this.teiId);
    }

    @Override
    public String content() {
        return prefix + content + suffix;
    }


    @Override
    public FileWriter paginate(FileWriter fw, String filePfx) {
        try {
            fw.write(content());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fw;
    }

    @Override
    public List<IText> findAll(Predicate<IText> p) {
        if (p.test(this))
            return Collections.singletonList(this);
        else
            return Collections.EMPTY_LIST;
    }


    @Override
    public void accept(ChildVisitor visitor) {

    }

    @Override
    public TextLeaf rightMostLeaf() {
        return this;
    }

    public TextLeaf withContent(String s) {
        return new TextLeaf(s, prefix, suffix, this.teiId);
    }
}
