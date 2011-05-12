package app.dnd;

/**
 * Created by psyrtsov
 */
public abstract class DNDContext {
    private final Object key;

    public DNDContext(Object key) {
        this.key = key;
    }

    public Object getKey() {
        return key;
    }

    public abstract void init(DropController dropController);
    public abstract void revert();
}
