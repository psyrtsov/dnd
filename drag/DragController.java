package app.dnd.drag;

import app.dnd.resources.DNDResources;
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
    private final DNDResources dndResources;
    private DropController dropController;

    public DragController(ComplexPanel panel) {
        this(panel, DNDResources.INSTANCE);
    }

    public DragController(ComplexPanel panel, DNDResources dndResources) {
        this.panel = panel;
        this.dndResources = dndResources;
    }

    public boolean dragStart(DNDContext dndContext, Element parent) {
        panel.add(new DragPanel(parent, dropController, dndContext, dndResources));
        return true;
    }

    public void register(DropController dropController) {
        this.dropController = dropController;
    }
}
