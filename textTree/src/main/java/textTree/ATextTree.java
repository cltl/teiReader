package textTree;


import baseExtraction.ChildVisitor;

import java.util.List;
import java.util.function.Predicate;

public abstract class ATextTree implements IText {
    String prefix;
    String suffix;

    ATextTree(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public static IText join(ATextTree tree1, ATextTree tree2) {
        if (tree1 instanceof TextTree) {
            if (tree2 instanceof TextTree)
                ((TextTree) tree1).getChildren().addAll(((TextTree) tree2).getChildren());
            else
                ((TextTree) tree1).getChildren().add(tree2);
            return tree1;
        } else {
            if (tree2 instanceof TextTree) {
                ((TextTree) tree2).getChildren().add(0, tree1);
                return tree2;
            } else
                return ((TextLeaf) tree1).withContent(((TextLeaf) tree1).getContent() + ((TextLeaf) tree2).getContent());
        }
    }


    public abstract ATextTree with(String prefix, String suffix);

    public abstract String content();

    public abstract void accept(ChildVisitor visitor);

    public abstract TextLeaf rightMostLeaf();

    public abstract List<IText> findAll(Predicate<IText> p);

}
