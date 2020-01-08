package tei_text;

import textTree.*;
import xjc.tei2.*;

import java.util.stream.Collectors;

public class TextTreeFactory {

    static ATextTree create(java.util.List<Object> elements, String separator, String prefix, String suffix, boolean shiftPageBreaks) {
        if (elements.isEmpty())
            return TextLeaf.create("", prefix, suffix);
        return TextTree.create(createLoop(elements), separator, prefix, suffix, shiftPageBreaks);
    }

    private static java.util.List<IText> createLoop(java.util.List<Object> elements) {
        return elements.stream().map(TextTreeFactory::create).collect(Collectors.toList());
    }

    public static IText create(Object o) {
        if (o instanceof TEI2)
            return create(((TEI2) o).getText().getAnchorsAndGapsAndFigures().stream()
                    .filter(x -> x instanceof Body).findFirst().orElse(null));
        if (o instanceof String)
            return TextLeaf.create(((String) o).replace("\n", ""),  "", "");
        else if (o instanceof Lb)
            return TextLeaf.create("\n", "",  "");
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
                return FootNote.create(n.getN(), create(n.getContent(), "", "\n", "\n", true));
            else
                return create(n.getContent(), "", " [", "]", true);
        } else if (o instanceof Figure)
            return TextLeaf.create("","[FIGURE]", "");
        else if (o instanceof InterpGrp)
            return NullText.getInstance();
        else if (o instanceof Pb)
            return PageBreak.create(((Pb) o).getN());
        else
            throw new IllegalArgumentException("Found unexpected element: " + o.getClass());
    }
}
