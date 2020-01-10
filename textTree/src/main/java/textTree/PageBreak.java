package textTree;

import baseExtraction.ChildVisitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class PageBreak implements IText {
    String pageNumber;

    PageBreak(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    public static PageBreak create(String n) {
        return new PageBreak(n);
    }

    public String content() {
        return "";
    }


    @Override
    public FileWriter paginate(FileWriter fw, String filePfx) {
        try {
            fw.close();
            fw = new FileWriter(new File(filePfx + pageNumber + ".txt"));
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

    public String getPageNumber() {
        return pageNumber;
    }
}
