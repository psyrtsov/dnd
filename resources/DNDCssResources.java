package app.dnd.resources;

import com.google.gwt.resources.client.CssResource;

/**
 * Created by psyrtsov
 */
public interface DNDCssResources extends CssResource {
    public static final String DEFAULT_CSS = "app/dnd/resources/dnd.css";

    String positioner();

    String dragHandler();

    String dragPanel();
}
