package text_tree;

import xjc.tei2.*;

import java.util.stream.Collectors;

public class TextTreeFactory {

    static ATextTree createTextLeaf(String content, String separator, String prefix, String suffix) {
        return new TextLeaf(content, prefix, suffix);
    }

    public static FootNote createFootNote(String id, ATextTree content) {
        return new FootNote(id, content);
    }

    public static PageBreak createPageBreak(String n) {
        return new PageBreak(n);
    }

    static ATextTree create(java.util.List<Object> elements, String separator, String prefix, String suffix, boolean shiftPageBreaks) {
        if (elements.isEmpty())
            return new TextLeaf("", prefix, suffix);
        return new TextTree(elements.stream().map(x -> create(x)).collect(Collectors.toList()),
                separator, prefix, suffix, shiftPageBreaks);
    }

    public static IText create(Object o) {
        if (o instanceof TEI2)
            return create(((TEI2) o).getText().getAnchorsAndGapsAndFigures().stream()
                    .filter(x -> x instanceof Body).findFirst().orElse(null));
        if (o instanceof String)
            return createTextLeaf(((String) o).replace("\n", ""), "", "", "");
        else if (o instanceof Lb)
            return createTextLeaf("\n", "", "", "");
        if (o instanceof Body)
            return create(((Body) o).getArgumentsAndBylinesAndDatelines(), "\n", "", "", false);
        else if (o instanceof Div)
            return create(((Div) o).getArgumentsAndBylinesAndDatelines(), "\n", "", "", false);
        else if (o instanceof Lg) {
            Lg lg = ((Lg) o);
            return create(lg.getArgumentsAndBylinesAndDatelines(), "\n", "[" + lg.getType().toUpperCase() + "]\n", "", true);
        } else if (o instanceof Row)
            return create(((Row) o).getCellsAndTablesAndAnchors(), " ", "", "", true);
        else if (o instanceof Table)
            return create(((Table) o).getHeadsAndAnchorsAndGaps(), "\n", "[TABLE]\n", "", true);
        else if (o instanceof xjc.tei2.List)
            return create(((xjc.tei2.List) o).getAnchorsAndGapsAndFigures(), "", "\n", "", true);
        else if (o instanceof Label)
            return create(((Label) o).getContent(), "", "", " ", true);
        else if (o instanceof Item)
            return create(((Item) o).getContent(), "", "", "\n", true);
        else if (o instanceof Q)
            return create(((Q) o).getContent(), "", "", "", true);
        else if (o instanceof Head)
            return create(((Head) o).getContent(), "", "", "\n", true);
        else if (o instanceof P)
            return create(((P) o).getContent(), "", "", "\n", true);
        else if (o instanceof Hi)
            return create(((Hi) o).getContent(), "", "", "", true);
        else if (o instanceof Cell)
            return create(((Cell) o).getContent(), "", "", "", true);
        else if (o instanceof L)
            return create(((L) o).getContent(), "", "", "", true);
        else if (o instanceof Name)
            return create(((Name) o).getContent(), "", "", "", true);
        else if (o instanceof Note) {
            Note n = (Note) o;
            if (n.getPlace().equals("foot"))
                return createFootNote(n.getN(), create(n.getContent(), "", "\n", "\n", true));
            else
                return create(n.getContent(), "", " [", "]", true);
        } else if (o instanceof Figure)
            return createTextLeaf("", "","[FIGURE]", "");
        else if (o instanceof InterpGrp)
            return NullText.getInstance();
        else if (o instanceof Pb)
            return createPageBreak(((Pb) o).getN());
        else
            throw new IllegalArgumentException("Found unexpected element: " + o.getClass());
    }
}
