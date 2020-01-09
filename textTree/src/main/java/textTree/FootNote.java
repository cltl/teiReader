package textTree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class FootNote implements IText {
    ATextTree textTree;
    String id;

    FootNote(String id, ATextTree textTree) {
        this.id = id;
        this.textTree = textTree;
    }

    public static FootNote create(String id, ATextTree content) {
        return new FootNote(id, content);
    }

    @Override
    public String content() {
        if (id != null)
            return "[" + id + "]";
        else return "";
    }

    @Override
    public void shiftChildren(boolean shiftPageBreaks, boolean shiftFootNotes) {
        // NOTE: footnote contents are transformed to ATextTree objects before their children are visited
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
        List<IText> matching = new LinkedList<>();
        if (p.test(this))
            matching.add(this);
        matching.addAll(this.textTree.findAll(p));
        return matching;
    }

    ATextTree getTextTree() {
        return this.textTree;
    }

    public String getId() {
        return id;
    }
}
