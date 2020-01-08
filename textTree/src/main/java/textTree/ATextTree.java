package textTree;


import java.util.List;
import java.util.function.Predicate;

public abstract class ATextTree implements IText {
    String prefix;
    String suffix;

    ATextTree(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public abstract ATextTree with(String prefix, String suffix);

    public abstract String content();

    public abstract void shiftChildren(boolean shiftPageBreaks, boolean shiftFootNotes);

    protected abstract TextLeaf rightMostLeaf();

    public abstract List<IText> findAll(Predicate<IText> p);
}
