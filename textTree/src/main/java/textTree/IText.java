package textTree;

import java.io.FileWriter;
import java.util.List;
import java.util.function.Predicate;

public interface IText {

    String content();

    void shiftChildren(boolean shiftPageBreaks, boolean shiftFootNotes);

    FileWriter paginate(FileWriter fw, String filePfx);

    List<IText> findAll(Predicate<IText> p);
}
