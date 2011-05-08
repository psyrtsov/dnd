package app.dnd.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Created by psyrtsov
 */
public interface DNDResources extends ClientBundle {
    public static final DNDResources DND_RESOURCES = GWT.create(DNDResources.class);

    @Source("drag_handle.png")
    ImageResource dragHandle();
}
