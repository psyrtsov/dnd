package app.dnd.tree;

/**
 * Created by psyrtsov
 */
public interface DNDTreeViewModel {
    public<T> boolean moveNode(T item, T newParent, int idx);
}
