package app.dnd.drag;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Panel;
import app.dnd.DNDContext;
import app.dnd.DropController;

/**
 * Created by psyrtsov
 */
public class DragController {
    private final Panel panel;
    private DropController dropController;

    public DragController(ComplexPanel panel) {
        this.panel = panel;
    }

    public boolean dragStart(DNDContext dndContext, Element parent) {
        panel.add(new DragPanel(parent, dropController, dndContext));
        return true;
    }

    public void register(DropController dropController) {
        this.dropController = dropController;
    }
}
