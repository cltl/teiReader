package baseExtraction;

import textTree.IText;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TextHandler {
    IText textTree;
    boolean paginate;
    final boolean shiftPageBreaks = true;
    boolean shiftFootNotes = true;
    final String extension = ".txt";
    final String paginationID = "_p";

    TextHandler(IText textTree, boolean paginate, boolean extractFootNotes) {
        this.textTree = textTree;
        this.paginate = paginate;
        this.shiftFootNotes = extractFootNotes;
    }

    public static TextHandler create(IText textTree, boolean paginate, boolean extractFootNotes) {
        return new TextHandler(textTree, paginate, extractFootNotes);
    }

    public IText getTextTree() {
        return textTree;
    }

    public void process() {
        textTree.shiftChildren(shiftPageBreaks, shiftFootNotes);
    }

    public void write(String outFile) throws IOException {
        if (paginate) {
            outFile += paginationID;
            FileWriter fw = new FileWriter(new File(outFile + "0" + extension));
            fw = textTree.paginate(fw, outFile);
            fw.close();
        } else {
            FileWriter fw = new FileWriter(new File(outFile + extension));
            fw.write(textTree.content());
            fw.close();
        }
    }

}
