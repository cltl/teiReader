package baseExtraction;

import textTree.*;

import java.util.List;
import java.util.function.Predicate;

public interface ChildVisitor {


    void visit(TextTree tree);
    default void visit(TextLeaf tree) {}
    default void visit(FootNote tree) {}
    default void visit(NullText tree) {}
    default void visit(PageBreak tree) {}


    default int findIndex(List<IText> children, Predicate<IText> textPredicate, int from) {
        for (int i = from; i < children.size(); i++) {
            if (textPredicate.test(children.get(i)))
                return i;
        }
        return -1;
    }


    default int findLastIndex(List<IText> children, Predicate<IText> textPredicate, int before) {
        for (int i = before; i >= 0; i--) {
            if (textPredicate.test(children.get(i)))
                return i;
        }
        return -1;
    }
}
