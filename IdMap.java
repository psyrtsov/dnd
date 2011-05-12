package app.dnd;

import java.util.HashMap;

/**
 * Created by psyrtsov
 */
public class IdMap extends HashMap<Object, Integer> {
    private int lastId = 1;

    public synchronized Integer require(Object o) {
        Integer id = super.get(o);
        if (id == null) {
            id = lastId++;
            put(o, id);
        }
        return id;
    }
}
