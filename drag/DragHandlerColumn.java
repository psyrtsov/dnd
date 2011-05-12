package app.dnd.drag;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import app.dnd.DNDContext;
import app.dnd.resources.DNDResources;

/**
 * Created by psyrtsov
 */
public class DragHandlerColumn<T> extends Column<T, T> {
    public static final String MOUSE_DOWN = MouseDownEvent.getType().getName();

    public DragHandlerColumn(DragSource dragSource, DragController dragController) {
        super(new DragHandlerCell<T>(dragSource, dragController));
    }

    @Override
    public T getValue(T item) {
        return item;
    }

    public static class DragHandlerCell<T> extends AbstractCell<T> {
        private final DragSource dragSource;
        private final DragController dragController;

        public DragHandlerCell(DragSource dragSource, DragController dragController) {
            super(MOUSE_DOWN);
            this.dragSource = dragSource;
            this.dragController = dragController;
        }

        @Override
        public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event, ValueUpdater<T> valueUpdater) {
            if (MOUSE_DOWN.equals(event.getType())) {
                DNDContext dndContext = dragSource.startDragging(value);
                dragController.dragStart(dndContext, parent);
                event.stopPropagation();
                event.preventDefault();
                return;
            }

            super.onBrowserEvent(context, parent, value, event, valueUpdater);

        }

        @Override
        public void render(Context context, T value, SafeHtmlBuilder sb) {
            if (value != null) {
                SafeHtml html = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(
                        DNDResources.DND_RESOURCES.dragHandle()).getHTML());
                sb.append(html);
            }
        }

    }
}
