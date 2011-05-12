package app.dnd.drag;

import app.dnd.DNDContext;

/**
 * Created by psyrtsov
 */
public interface DragSource {
    DNDContext startDragging(Object item);
}
