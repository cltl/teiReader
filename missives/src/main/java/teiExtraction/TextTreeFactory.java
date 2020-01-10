package teiExtraction;

import textTree.*;
import xjc.tei.*;

import java.util.List;
import java.util.stream.Collectors;

public class TextTreeFactory {
    static ATextTree create(java.util.List<Object> elements, String separator, String prefix, String suffix) {
        if (elements.isEmpty())
            return TextLeaf.create("", prefix, suffix);
        return TextTree.create(createLoop(elements), separator, prefix, suffix);
    }

    static ATextTree createRow(java.util.List<Cell> elements, String separator, String prefix, String suffix) {
        if (elements.isEmpty())
            return TextLeaf.create("", prefix, suffix);
        return TextTree.create(elements.stream().map(TextTreeFactory::create).collect(Collectors.toList()), separator, prefix, suffix);
    }

    private static java.util.List<IText> createLoop(java.util.List<Object> elements) {
        return elements.stream().map(TextTreeFactory::create).collect(Collectors.toList());
    }

    public static IText create(Object o) {
        List<Object> a;
        if (o instanceof TEI) {
            a = ((TEI) o).getTexts().stream().map(t -> t.getIndicesAndInterpsAndInterpGrps()).flatMap(x -> x.stream()).collect(Collectors.toList());
            return create(a, "\n", "", "");
        }
        if (o instanceof String)
            return TextLeaf.create(((String) o).replace("\n", ""),  "", "");
        if (o instanceof Body)
            return create(((Body) o).getIndicesAndInterpsAndInterpGrps(), "\n", "", "");
        else if (o instanceof Div)
            return create(((Div) o).getBylinesAndDatelinesAndArguments(), "\n", "", "");
        else if (o instanceof Lb)
            return NullText.getInstance();
        else if (o instanceof Head)
            return create(((Head) o).getContent(), "", "", "\n");
        else if (o instanceof P)
            return create(((P) o).getContent(), "", "", "\n");
        else if (o instanceof Hi)
            return create(((Hi) o).getContent(), "", "", "");
        else if (o instanceof Cell)
            return create(((Cell) o).getContent(), "", "", "");
        else if (o instanceof Row)
            return createRow(((Row) o).getCells(), " ", "", "");
        else if (o instanceof Table)
            return create(((Table) o).getHeadsAndIndicesAndInterps(), "\n", "\n", "");
        else if (o instanceof Note) {
            Note n = (Note) o;
            if (n.getPlaces().isEmpty() || n.getPlaces() == null)
                return FootNote.create(null, create(n.getContent(), "", "", ""));
            else
                return create(n.getContent(), "", "", "");
        }
        else if (o instanceof InterpGrp)
            return NullText.getInstance();
        else if (o instanceof Pb)
            return PageBreak.create(((Pb) o).getN());
        else
            throw new IllegalArgumentException("Found unexpected element: " + o.getClass());
    }
}
