package app.dnd.drag;

import app.dnd.DNDContext;

/**
 * Created by psyrtsov
 */
public interface DragSource<T> {
    DNDContext startDragging(T item);
}
