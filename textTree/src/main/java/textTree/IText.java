package textTree;

import baseExtraction.ChildVisitor;

import java.io.FileWriter;
import java.util.List;
import java.util.function.Predicate;

public interface IText {

    String content();

    FileWriter paginate(FileWriter fw, String filePfx);

    List<IText> findAll(Predicate<IText> p);

    void accept(ChildVisitor visitor);
}
