package app.dnd.tree;

import app.dnd.IdMap;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.*;
import app.dnd.DNDContext;
import app.dnd.drag.DragSource;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by psyrtsov
 * psdo: remove req of same generic type for whole tree model
 * psdo: nodes directly under root are not droppable
 */
public abstract class DNDTreeViewModel<T> implements TreeViewModel, DragSource<T> {
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private final IdMap idMap = new IdMap();
    private Map<String, DNDNodeInfo> cache = new HashMap<String, DNDNodeInfo>();
    private boolean positionerOffset = false;
    private final T rootValue;

    public DNDTreeViewModel(T rootValue) {
        this.rootValue = rootValue;
    }

    public DNDNodeInfo getDNDNodeInfo(String key) {
        return cache.get(key);
    }

    public void setPositionerOffset(boolean offset) {
        positionerOffset = offset;
    }

    @SuppressWarnings({"unchecked"})
    public ListDataProvider<T> createDataProvider(Object parent, List<T> children, ProvidesKey<T> keyProvider) {
        ListDataProvider<T> dataProvider = new ListDataProvider<T>(keyProvider);
        Integer pid = idMap.require(parent);
        if (children != null) {
            dataProvider.setList(children);
            for (T item : children) {
                Object key = idMap.require(item);
                final DNDNodeInfo dndNodeInfo = new DNDNodeInfo(item, dataProvider, pid);
                cache.put(key.toString(), dndNodeInfo);
            }
        }
        return dataProvider;
    }

    public T getPositionerItem() {
        return null;
    }

    public DNDContext startDragging(T item) {
        final Object key = idMap.get(item);
        final DNDNodeInfo dndNodeInfo = cache.get(key.toString());
        final int savedIdx = dndNodeInfo.indexOf();
        dndNodeInfo.remove();
        return new DNDContext(key) {
            @Override
            public void revert() {
                dndNodeInfo.restore(savedIdx);
                dndNodeInfo.refresh();
            }
        };
    }

    public void drop(DNDNodeInfo positioner, DNDContext dndContext) {
        int idx = positioner.indexOf();
        Object key = dndContext.getKey();
        final DNDNodeInfo dndNodeInfo = cache.get(key.toString());
        String parentKey;
        if (!positionerOffset) {
            parentKey = positioner.getParentKey();
        } else {
            // when positioner is shifted we should use previous node as parent
            T parentItem = positioner.dataProvider.getList().get(idx - 1);
            parentKey = idMap.get(parentItem).toString();
            idx = 0;
        }
        DNDNodeInfo parentNodeInfo = cache.get(parentKey);
        moveNode(dndNodeInfo.item, parentNodeInfo == null? rootValue: parentNodeInfo.item, idx);
    }

    protected abstract boolean moveNode(T item, T newParent, int idx);

    public class DNDNodeInfo {
        public final T item;
        /**
         * this is parent's data provider that contains this node
         */
        private ListDataProvider<T> dataProvider;
        private Integer parentKey;

        public DNDNodeInfo(T item, ListDataProvider<T> dataProvider, Integer parentKey) {
            this.item = item;
            this.dataProvider = dataProvider;
            this.parentKey = parentKey;
        }

        public int indexOf() {
            List<?> list = dataProvider.getList();
            return list.indexOf(item);
        }

        public boolean remove() {
            return dataProvider.getList().remove(item);
        }

        public void refresh() {
            dataProvider.refresh();
        }

        public String getParentKey() {
            return parentKey.toString();
        }

        DNDNodeInfo addSibling(int idx, T newItem) {
            List<T> list = dataProvider.getList();
            if (list.size() <= idx) {
                list.add(newItem);
            } else {
                list.add(idx, newItem);
            }
            return new DNDNodeInfo(newItem, dataProvider, parentKey);
        }

        public void restore(int savedIdx) {
            dataProvider.getList().add(savedIdx, item);
        }
    }

    public static final String TAG = "dropTarget";
    public static final String KEY_ATTR = "key";

    interface Template extends SafeHtmlTemplates {
        @Template("<" + TAG + " " + KEY_ATTR + "=\"{0}\"/>")
        SafeHtml dropTarget(String key);

        @Template("<div style=\"overflow:hidden;margin-left:0px;\" class=\"positioner\"> &nbsp; </div>")
        SafeHtml positioner();

        @Template("<div style=\"overflow:hidden;margin-left:16px;\" class=\"positioner\"> &nbsp; </div>")
        SafeHtml offsetPositioner();
    }

    private static Template template;

    public class DNDCompositeCell<T> extends CompositeCell<T> {
        public DNDCompositeCell(List<HasCell<T, ?>> hasCells) {
            super(hasCells);
            if (template == null) {
                template = GWT.create(Template.class);
            }
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public void render(Context context, T value, SafeHtmlBuilder sb) {
            if (value == getPositionerItem()) {
                if (positionerOffset) {
                    sb.append(template.offsetPositioner());
                } else {
                    sb.append(template.positioner());
                }
            } else {
                final SafeHtml html = template.dropTarget(idMap.require(value).toString());
                sb.append(html);
                super.render(context, value, sb);
            }
        }
    }
}
