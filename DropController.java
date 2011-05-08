package app.dnd;

import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;

/**
 * Created by psyrtsov
 */
public interface DropController extends MouseMoveHandler {
    void drop(MouseUpEvent event, DNDContext dndContext);
}
