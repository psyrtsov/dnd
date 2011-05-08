package app.dnd.drag;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.SimplePanel;
import app.dnd.DNDContext;
import app.dnd.DropController;

public final class DragPanel
        extends SimplePanel implements Event.NativePreviewHandler, MouseMoveHandler, MouseUpHandler {
    private final DropController dropController;
    private final DNDContext dndContext;
    private final HandlerRegistration nativePreviewHandlerRegistration;
    private final HandlerRegistration mouseMoveRegistration;
    private final HandlerRegistration mouseUpRegistration;

    public DragPanel(Element el, DropController dropController, DNDContext dndContext) {
        super(el);
        this.dropController = dropController;
        this.dndContext = dndContext;
        nativePreviewHandlerRegistration = Event.addNativePreviewHandler(this);
        mouseMoveRegistration = addDomHandler(this, MouseMoveEvent.getType());
        mouseUpRegistration = addDomHandler(this, MouseUpEvent.getType());
        DOM.setCapture(getElement());
        DOM.setStyleAttribute(getElement(), "position", "absolute");
        moveTo(el.getAbsoluteLeft(), el.getAbsoluteTop());
    }

    public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        final EventTarget eventTarget = event.getNativeEvent().getEventTarget();
        Element el = Element.as(eventTarget);
        if (event.getTypeInt() == Event.ONMOUSEDOWN &&
                DOM.isOrHasChild(getElement(), (com.google.gwt.user.client.Element) el)) {
            event.getNativeEvent().preventDefault();
        }
    }

    public void onMouseMove(MouseMoveEvent event) {
        int newX = Math.max(0, event.getX() + getAbsoluteLeft() - 16);
        int newY = Math.max(0, event.getY() + getAbsoluteTop() - 8);
        moveTo(newX, newY);
        dropController.onMouseMove(event);
    }

    private void moveTo(int newX, int newY) {
        DOM.setStyleAttribute(getElement(), "left", newX + "px");
        DOM.setStyleAttribute(getElement(), "top", newY + "px");
    }

    public void onMouseUp(MouseUpEvent event) {
        DOM.releaseCapture(getElement());
        nativePreviewHandlerRegistration.removeHandler();
        mouseMoveRegistration.removeHandler();
        mouseUpRegistration.removeHandler();
        removeFromParent();
        dropController.drop(event, dndContext);
    }
}