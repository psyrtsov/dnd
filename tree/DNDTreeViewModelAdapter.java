package app.dnd.tree;

import app.dnd.DNDContext;
import app.dnd.IdMap;
import app.dnd.drag.DragSource;
import app.dnd.resources.DNDCssResources;
import app.dnd.resources.DNDResources;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by psyrtsov
 */
public abstract class DNDTreeViewModelAdapter implements DragSource {
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private final IdMap idMap = new IdMap();
    /**
     * psdo: investigate possibility of using data from model instead of cache
     */    
    private Map<String, DNDNodeInfo> cache = new HashMap<String, DNDNodeInfo>();
    private boolean positionerOffset = false;
    private DNDTreeViewModel dndTreeViewModel;
    private Object rootValue;

    public void clear() {
        idMap.clear();
        cache.clear();
    }

    protected void init(DNDTreeViewModel dndTreeViewModel, Object rootValue) {
        this.dndTreeViewModel = dndTreeViewModel;
        this.rootValue = rootValue;
    }

    public DNDNodeInfo getDNDNodeInfo(String key) {
        return cache.get(key);
    }

    public boolean isPositionerOffset() {
        return positionerOffset;
    }

    public void setPositionerOffset(boolean offset) {
        positionerOffset = offset;
    }

    /**
     * this method has to be invoked on parent object when
     * @param parent - parent data item
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void refresh(Object parent) {
        final Integer pidObj = idMap.get(parent);
        if (pidObj == null) {
            throw new RuntimeException("Unknown tree value "+parent);
        }
        String pid = pidObj.toString();
        final DNDNodeInfo parentDndNodeInfo = cache.get(pid);
        final ListDataProvider dataProvider = parentDndNodeInfo.dataProvider;
        if (dataProvider == null) {
            open(parentDndNodeInfo, true);
            return;
        }
        final List list = dataProvider.getList();
        for (Object item : list) {
            String key = idMap.require(item).toString();
            DNDNodeInfo dndNodeInfo = cache.get(key);
            if (dndNodeInfo == null) {
                dndNodeInfo = new DNDNodeInfo(item, parentDndNodeInfo);
                cache.put(key, dndNodeInfo);
            }
        }
        dataProvider.refresh();
    }

    @SuppressWarnings({"unchecked"})
    public<T> ListDataProvider<T> createDataProvider(Object parent, List children, ProvidesKey<T> keyProvider) {
        String parentKey = idMap.require(parent).toString();
        DNDNodeInfo parentDndNodeInfo = cache.get(parentKey);
        if (parentDndNodeInfo == null) {
            parentDndNodeInfo = new DNDNodeInfo(parent, null);
            cache.put(parentKey, parentDndNodeInfo);
        }
        ListDataProvider<T> dataProvider = new ListDataProvider<T>(keyProvider);
        parentDndNodeInfo.dataProvider = dataProvider;
        dataProvider.setList(children);
        for (Object item : children) {
            Object key = idMap.require(item);
            final DNDNodeInfo dndNodeInfo = new DNDNodeInfo(item, parentDndNodeInfo);
            cache.put(key.toString(), dndNodeInfo);
        }
        return dataProvider;
    }

    public Object getPositionerItem() {
        return null;
    }

    public DNDContext startDragging(Object item) {
        final String key = idMap.get(item).toString();
        final DNDNodeInfo dndNodeInfo = cache.get(key);
        if (dndNodeInfo == null) {
            GWT.log("dndNodeInfo is null for "+ item);
        }
        final int savedIdx = dndNodeInfo.indexOf();
        final DNDNodeInfo parentDndNodeInfo = dndNodeInfo.parentDndNodeInfo;
        dndNodeInfo.remove();
        if (parentDndNodeInfo.dataProvider.getList().isEmpty()) {
            // this is only way I could make CellTree hide "no data" when I'm taking last child from it 
            open(parentDndNodeInfo, false);
        }
        
        return new DNDContext(key) {
            @Override
            public void revert() {
                dndNodeInfo.restore(savedIdx);
                dndNodeInfo.refresh();
            }
        };
    }

    protected abstract TreeNode open(DNDNodeInfo parentDndNodeInfo, boolean open) ;

    public DNDNodeInfo drop(DNDNodeInfo positioner, DNDContext dndContext) {
        int idx = positioner.indexOf();
        Object key = dndContext.getKey();
        final DNDNodeInfo dndNodeInfo = cache.get(key.toString());
        DNDNodeInfo parentNodeInfo;
        if (!positionerOffset) {
            parentNodeInfo = positioner.getParentDndNodeInfo();
        } else {
            // when positioner is shifted we should use previous node as parent
            Object parentItem = positioner.getParentDndNodeInfo().dataProvider.getList().get(idx - 1);
            String parentKey = idMap.get(parentItem).toString();
            parentNodeInfo = cache.get(parentKey);
            idx = 0;
        }
        dndTreeViewModel.moveNode(dndNodeInfo.item, parentNodeInfo == null? rootValue: parentNodeInfo.item, idx);
        return dndNodeInfo.parentDndNodeInfo = parentNodeInfo;
    }

    public<T> DNDCompositeCell<T> createDropableCell(List<HasCell<T, ?>> cellList) {
        return new DNDCompositeCell<T>(cellList);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public<T> DNDCompositeCell<T> createDropableCell(List<HasCell<T, ?>> cellList, DNDResources dndResources) {
        return new DNDCompositeCell<T>(cellList, dndResources);
    }

    public class DNDNodeInfo {
        public final Object item;
        private DNDNodeInfo parentDndNodeInfo;
        private ListDataProvider dataProvider;

        public DNDNodeInfo(Object item, DNDNodeInfo parentDndNodeInfo) {
            this.item = item;
            this.parentDndNodeInfo = parentDndNodeInfo;
        }

        public int indexOf() {
            if (parentDndNodeInfo == null) {
                GWT.log("parentDndNodeInfo is null for "+ item);
            }
            final ListDataProvider provider = parentDndNodeInfo.dataProvider;
            List<?> list = provider.getList();
            return list.indexOf(item);
        }

        public boolean remove() {
            return parentDndNodeInfo.dataProvider.getList().remove(item);
        }

        public void refresh() {
            parentDndNodeInfo.dataProvider.refresh();
        }

        public DNDNodeInfo getParentDndNodeInfo() {
            return parentDndNodeInfo;
        }

        @SuppressWarnings({"unchecked"})
        DNDNodeInfo addSibling(int idx, Object newItem) {
            List list = parentDndNodeInfo.dataProvider.getList();
            if (list.size() <= idx) {
                list.add(newItem);
            } else {
                list.add(idx, newItem);
            }
            return new DNDNodeInfo(newItem, parentDndNodeInfo);
        }

        @SuppressWarnings({"unchecked"})
        public void restore(int savedIdx) {
            parentDndNodeInfo.dataProvider.getList().add(savedIdx, item);
        }

        @Override
        public String toString() {
            return item.toString();
        }
    }

    public static final String TAG = "dropTarget";
    public static final String KEY_ATTR = "key";

    interface Template extends SafeHtmlTemplates {
        @Template("<" + TAG + " " + KEY_ATTR + "=\"{0}\"/>")
        SafeHtml dropTarget(String key);

        @SafeHtmlTemplates.Template("<div style=\"overflow:hidden;margin-left:0px;\" class=\"{0}\"> &nbsp; </div>")
        SafeHtml positioner(String classes);

        @SafeHtmlTemplates.Template("<div style=\"overflow:hidden;margin-left:16px;\" class=\"{0}\"> &nbsp; </div>")
        SafeHtml offsetPositioner(String classes);
    }

    private static Template template = GWT.create(Template.class);

    public class DNDCompositeCell<T> extends CompositeCell<T> {
        private final DNDCssResources dndCssResources;

        public DNDCompositeCell(List<HasCell<T, ?>> hasCells) {
            this(hasCells, DNDResources.INSTANCE);
        }

        public DNDCompositeCell(List<HasCell<T, ?>> hasCells, DNDResources dndResources) {
            super(hasCells);
            this.dndCssResources = dndResources.css();
            dndCssResources.ensureInjected();
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public void render(Context context, T value, SafeHtmlBuilder sb) {
            if (value == getPositionerItem()) {
                if (positionerOffset) {
                    sb.append(template.offsetPositioner(dndCssResources.positioner()));
                } else {
                    sb.append(template.positioner(dndCssResources.positioner()));
                }
            } else {
                final SafeHtml html = template.dropTarget(idMap.require(value).toString());
                sb.append(html);
                super.render(context, value, sb);
            }
        }
    }
}
