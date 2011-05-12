package app.dnd.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

/**
 * Created by psyrtsov
 */
public interface DNDResources extends ClientBundle {
    public static final DNDResources INSTANCE = GWT.create(DNDResources.class);

    @Source(DNDCssResources.DEFAULT_CSS)
    DNDCssResources css();

    DNDImageResources images();
}
