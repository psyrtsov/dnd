package app.dnd;

import java.util.HashMap;

/**
 * Created by psyrtsov
 */
public class IdMap<T> extends HashMap<T, Integer> {
    private int lastId = 1;

    public synchronized Integer require(T o) {
        Integer id = super.get(o);
        if (id == null) {
            id = lastId++;
            put(o, id);
        }
        return id;
    }
}
