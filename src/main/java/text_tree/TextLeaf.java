package text_tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class TextLeaf extends ATextTree {
    String content;

    TextLeaf(String content, String prefix, String suffix) {
        super(prefix, suffix);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public ATextTree with(String prefix, String suffix) {
        return new TextLeaf(this.content, prefix, suffix);
    }

    @Override
    public String content() {
        return prefix + content + suffix;
    }

    @Override
    public void shiftChildren(boolean shiftPageBreaks, boolean shiftFootNotes) {

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
    protected TextLeaf rightMostLeaf() {
        return this;
    }

}
