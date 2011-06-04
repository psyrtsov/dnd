/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package app.dnd.drag;

import app.dnd.DNDContext;
import app.dnd.resources.DNDResources;
import com.google.gwt.cell.client.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

import java.util.HashSet;
import java.util.Set;

/**
 * this is slightly tweaked version of Google's IconCellDecorator
 * 
 * A {@link Cell} decorator that adds an icon to another {@link Cell}.
 *
 * @param <T> the type that this Cell represents
 * @param <C> the type that this Cell represents
 */
@SuppressWarnings({"JavaDoc"})
public abstract class DraggableCellDecorator<T, C> implements Cell<T> {
    public static final String MOUSE_DOWN = MouseDownEvent.getType().getName();
    private final String dragHandlerClass;

    interface Template extends SafeHtmlTemplates {
        @Template("<div style=\"position:relative;padding-{0}:{1}px;zoom:1;\">{2}<div>{3}</div></div>")
        SafeHtml outerDiv(String direction, int width, SafeHtml icon,
                          SafeHtml cellContents);

        /**
         * The wrapper around the image vertically aligned to the bottom.
         */
        @Template("<div class='{2}' style=\"position:absolute;{0}:0px;bottom:0px;line-height:0px;\">{1}</div>")
        SafeHtml imageWrapperBottom(String direction, SafeHtml image, String dragHandlerClass);

        /**
         * The wrapper around the image vertically aligned to the middle.
         */
        @Template("<div class='{3}' class='draggable' style=\"position:absolute;{0}:0px;top:50%;line-height:0px;"
                + "margin-top:-{1}px;\">{2}</div>")
        SafeHtml imageWrapperMiddle(String direction, int halfHeight, SafeHtml image, String dragHandlerClass);

        /**
         * The wrapper around the image vertically aligned to the top.
         */
        @Template("<div class='{2}' style=\"position:absolute;{0}:0px;top:0px;line-height:0px;\">{1}</div>")
        SafeHtml imageWrapperTop(String direction, SafeHtml image, String dragHandlerClass);
    }

    /**
     * The default spacing between the icon and the text in pixels.
     */
    private static final int DEFAULT_SPACING = 6;

    private static Template template;

    private final Cell<C> cell;

    private final String direction = LocaleInfo.getCurrentLocale().isRTL()
            ? "right" : "left";

    private final SafeHtml iconHtml;

    private final int imageWidth;

    private final SafeHtml placeHolderHtml;

    private final DragSource dragSource;
    private final DragController dragController;

    /**
     * Construct a new {@link IconCellDecorator}. The icon and the content will be
     * middle aligned by default.
     *
     * @param dragSource     - drag source
     * @param dragController - drag controller
     * @param cell           the cell to decorate
     */
    public DraggableCellDecorator(DragSource dragSource, DragController dragController, Cell<C> cell) {
        this(dragSource, dragController, DNDResources.INSTANCE, cell, HasVerticalAlignment.ALIGN_MIDDLE, DEFAULT_SPACING);
    }

    /**
     * Construct a new {@link IconCellDecorator}.
     *
     * @param dragSource     - drag source
     * @param dragController - drag controller
     * @param dndResources   dndResources
     * @param cell           the cell to decorate
     * @param valign         the vertical alignment attribute of the contents
     * @param spacing        the pixel space between the icon and the cell
     */
    public DraggableCellDecorator(DragSource dragSource, DragController dragController, DNDResources dndResources, Cell<C> cell,
                           HasVerticalAlignment.VerticalAlignmentConstant valign, int spacing) {
        this.dragSource = dragSource;
        this.dragController = dragController;
        if (template == null) {
            template = GWT.create(Template.class);
        }
        this.cell = cell;
        this.dragHandlerClass = dndResources.css().dragHandler();
        ImageResource icon = dndResources.images().dragHandle();
        this.iconHtml = getImageHtml(icon, valign, false);
        this.imageWidth = icon.getWidth() + spacing;
        this.placeHolderHtml = getImageHtml(icon, valign, true);
    }

    public boolean dependsOnSelection() {
        return cell.dependsOnSelection();
    }

    public Set<String> getConsumedEvents() {
        Set<String> consumedEvents = cell.getConsumedEvents();
        if (consumedEvents == null) {
            consumedEvents = new HashSet<String>();
        }
        if (!consumedEvents.contains(MOUSE_DOWN)) {
            consumedEvents.add(MOUSE_DOWN);
        }
        return consumedEvents;
    }

    public boolean handlesSelection() {
        return cell.handlesSelection();
    }

    public boolean isEditing(Context context, Element parent, T value) {
        return cell.isEditing(context, getCellParent(parent), getValue(value));
    }

    public void onBrowserEvent(Context context, Element parent, final T value,
                               NativeEvent event, ValueUpdater<T> valueUpdater) {
        if (MOUSE_DOWN.equals(event.getType())) {
            EventTarget eventTarget = event.getEventTarget();
            if (Element.is(eventTarget)) {
              Element target = eventTarget.cast();
              Element wrapper = target.getParentElement();
              if (wrapper != null && dragHandlerClass.equals(wrapper.getClassName())) {
                  DNDContext dndContext = dragSource.startDragging(value);
                  dragController.dragStart(dndContext, parent);
                  event.stopPropagation();
                  event.preventDefault();
                  return;
              }
            }
        }
        final Element cellParent = getCellParent(parent);
        cell.onBrowserEvent(context, cellParent, getValue(value), event, getValueUpdater());
    }

    public void render(Context context, T value, SafeHtmlBuilder sb) {
        SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
        cell.render(context, getValue(value), cellBuilder);

        sb.append(template.outerDiv(direction, imageWidth, isIconUsed(value)
                ? getIconHtml(value) : placeHolderHtml, cellBuilder.toSafeHtml()));
    }

    public boolean resetFocus(Context context, Element parent, T value) {
        return cell.resetFocus(context, getCellParent(parent), getValue(value));
    }

    public void setValue(Context context, Element parent, T value) {
        cell.setValue(context, getCellParent(parent), getValue(value));
    }

    /**
     * Get the safe HTML string that represents the icon. Override this method to
     * change the icon based on the value.
     *
     * @param value the value being rendered
     * @return the HTML string that represents the icon
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected SafeHtml getIconHtml(T value) {
        return iconHtml;
    }

    /**
     * Check if the icon should be used for the value. If the icon should not be
     * used, a placeholder of the same size will be used instead. The default
     * implementations returns true.
     *
     * @param value the value being rendered
     * @return true to use the icon, false to use a placeholder
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected boolean isIconUsed(T value) {
        return true;
    }

    /**
     * Get the HTML representation of an image. Visible for testing.
     *
     * @param res           the {@link ImageResource} to render as HTML
     * @param valign        the vertical alignment
     * @param isPlaceholder if true, do not include the background image
     * @return the rendered HTML
     */
    SafeHtml getImageHtml(ImageResource res, HasVerticalAlignment.VerticalAlignmentConstant valign,
                          boolean isPlaceholder) {
        // Get the HTML for the image.
        SafeHtml image;
        if (isPlaceholder) {
            image = SafeHtmlUtils.fromTrustedString("<div></div>");
        } else {
            AbstractImagePrototype proto = AbstractImagePrototype.create(res);
            image = SafeHtmlUtils.fromTrustedString(proto.getHTML());
        }

        // Create the wrapper based on the vertical alignment.
        if (HasVerticalAlignment.ALIGN_TOP == valign) {
            return template.imageWrapperTop(direction, image, dragHandlerClass);
        } else if (HasVerticalAlignment.ALIGN_BOTTOM == valign) {
            return template.imageWrapperBottom(direction, image, dragHandlerClass);
        } else {
            int halfHeight = (int) Math.round(res.getHeight() / 2.0);
            return template.imageWrapperMiddle(direction, halfHeight, image, dragHandlerClass);
        }
    }

    /**
     * Get the parent element of the decorated cell.
     *
     * @param parent the parent of this cell
     * @return the decorated cell's parent
     */
    private Element getCellParent(Element parent) {
        final Element element = parent.getFirstChildElement().getFirstChildElement();
        return element.getChild(1).cast();
    }

    public abstract C getValue(T value);

    public ValueUpdater<C> getValueUpdater() {
        return null;
    }
}
