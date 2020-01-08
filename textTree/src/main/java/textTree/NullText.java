package textTree;

import java.io.FileWriter;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class NullText implements IText {
    private static NullText ourInstance = new NullText();

    public static NullText getInstance() {
        return ourInstance;
    }

    private NullText() {}

    public String content() { return null; }

    @Override
    public void shiftChildren(boolean shiftPageBreaks, boolean shiftFootNotes) {
    }

    @Override
    public FileWriter paginate(FileWriter fw, String file) {
        return fw;
    }

    @Override
    public List<IText> findAll(Predicate<IText> p) {
        if (p.test(ourInstance))
            return Collections.singletonList(ourInstance);
        else
            return Collections.EMPTY_LIST;
    }

}
