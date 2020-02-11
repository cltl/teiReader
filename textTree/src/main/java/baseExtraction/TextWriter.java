package baseExtraction;

import textTree.FootNote;
import textTree.IText;
import textTree.TextTree;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class TextWriter implements ChildVisitor {
    boolean paginate;
    boolean splitNotes;
    final String paginationID = "_p";
    final String notesExtension = "_notes";
    final String extension = ".txt";



    private TextWriter(boolean paginate, boolean splitNotes) {
        this.paginate = paginate;
        this.splitNotes = splitNotes;
    }

    public static TextWriter create(boolean p, boolean s) {
        return new TextWriter(p, s);
    }

    @Override
    public void visit(TextTree tree) {
        tree.setChildren(
                tree.getChildren().stream()
                        .filter(c -> ! (c instanceof FootNote))
                        .collect(Collectors.toList()));
    }


    public void write(IText textTree, String outdir, String outFile) throws IOException {
        outFile = prepareOutputDir(outdir, outFile) + outFile;
        if (paginate) {
            outFile += paginationID;
            FileWriter fw = new FileWriter(new File(outFile + "0" + extension));
            fw = textTree.paginate(fw, outFile);
            fw.close();
        } else if (splitNotes) {
            FileWriter fw = new FileWriter(new File(outFile + notesExtension + extension));
            for (IText n : textTree.findAll(t -> t instanceof FootNote)) {
                try {
                    fw.write(n.content() + "\n\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            fw.close();
            fw = new FileWriter(new File(outFile + extension));
            textTree.accept(this);
            fw.write(textTree.content());
            fw.close();

        } else {
            FileWriter fw = new FileWriter(new File(outFile + extension));
            fw.write(textTree.content());
            fw.close();
        }
    }

    private String prepareOutputDir(String outdir, String fileName) {
        if (! outdir.endsWith("/"))
            outdir += '/';
        if (paginate)
            outdir += fileName + '/';
        if (! outdir.isEmpty()) {
            Path path = Paths.get(outdir);
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(Paths.get(outdir));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return outdir;
    }

}
