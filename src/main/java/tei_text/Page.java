package tei_text;

import xjc.tei2.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Page {
    private Pb pb;
    private List<Object> elts;
    private static String pbMarker = "<PB>";
    private static String pDash = "-" + pbMarker;
    private static String pSpace = "\\s+" + pbMarker;
    private static String pDefault = pbMarker;
    private static String noteStr = "(.*)<NOTE (\\([0-9]+\\))>([^<]*)</NOTE>(.*)";
    private static Pattern notePattern = Pattern.compile(noteStr, Pattern.MULTILINE);
    private List<Note> notes;


    protected Page(List<Object> elts) {
        if (elts.isEmpty() || ! (elts.get(0) instanceof Pb))
            throw new IllegalArgumentException("Expected a page break as first element");

        this.pb = (Pb) elts.remove(0);
        this.elts = elts;
        this.notes = new LinkedList<>();
    }

    private String bracket(String s, String affix) { return affix + "[" + s + "]" + affix; }

    public String getPageNumber() {
        return pb.getN();
    }

    public List<Object> getElts() {
        return elts;
    }

    public String toString() {
        String s = "page: " + pb.getN();
        s += "; " + elts.size() + "elts.";
        return s;
    }

    public String contentString(Object o) {
        if (o instanceof String)
            return ((String) o).replace("\n", "");

        List<Object> content;
        if (o instanceof Head) {
            content = ((Head) o).getContent();
            return content.stream().map(x -> contentString(x)).collect(Collectors.joining()) + "\n";
        } else if (o instanceof P)
            content = ((P) o).getContent();
        else if (o instanceof Hi)
            content = ((Hi) o).getContent();
        else if (o instanceof Note) {
            Note n = (Note) o;
            if (n.getPlace().equals("foot")) {
                notes.add(n);
                return n.getN();
            } else
                return bracket(n.getContent().stream().map(x -> contentString(x)).collect(Collectors.joining()), "");
        } else if (o instanceof Lb)
            return "\n\n";
        else if (o instanceof Cell)
            content = ((Cell) o).getContent();
        else if (o instanceof Row)
            return ((Row) o).getCellsAndTablesAndAnchors().stream().map(x -> contentString(x)).collect(Collectors.joining(" "));
        else if (o instanceof Table)
            return bracket("TABLE", "\n") + ((Table) o).getHeadsAndAnchorsAndGaps().stream().map(x -> contentString(x)).collect(Collectors.joining("\n"));
        else if (o instanceof Pb)
            return pbMarker;
        else if (o instanceof Lg) {
            Lg lg = (Lg) o;
            return bracket(lg.getType().toUpperCase(), "\n") + lg.getArgumentsAndBylinesAndDatelines().stream().map(x -> contentString(x)).collect(Collectors.joining("\n"));
        } else if (o instanceof L)
            content = ((L) o).getContent();
        else
            throw new IllegalArgumentException("Found unexpected element: " + o.getClass() + ", page: " + getPageNumber());
        return content.stream().map(x -> contentString(x)).collect(Collectors.joining());
    }

    private String noteContent(Note n) {
        return n.getN() + " " + n.getContent().stream().map(x -> contentString(x)).collect(Collectors.joining());
    }

    public String getContent() {
        String content =  elts.stream().map(e -> contentString(e)).collect(Collectors.joining("\n"));
        content = resolvePageBreaks(content);
        return content + "\n\n" + notes.stream().map(n -> noteContent(n)).collect(Collectors.joining("\n\n"));
    }

    /**
     * pulls notes out of paragraphs.
     *
     * The note's occurrence in a paragraph is marked by the note's identifier in brackets.
     * The note itself is placed after the paragraph.
     */
    private String resolveNotes(String content) {
        LinkedList<String> notes = new LinkedList<>();
        Matcher m = notePattern.matcher(content);
        while (m.find()) {
            content = m.group(1) + m.group(2) + m.group(4);
            notes.addFirst(m.group(2) + " " + m.group(3));
            m = notePattern.matcher(content);
        }
        content = content + "\n\n" + notes.stream().collect(Collectors.joining("\n\n"));
        return content;
    }

    private String resolvePageBreaks(String content) {
        return content.replaceAll(pDash, "")
                .replaceAll(pSpace, " ")
                .replaceAll(pDefault, " ");
    }

    public void write(String pfx) throws IOException {
        FileWriter fw = new FileWriter(new File(pfx + getPageNumber() + ".txt"));
        fw.write(getContent());
        fw.close();
    }
}
