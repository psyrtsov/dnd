package app.dnd.tree;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;
import app.dnd.DNDContext;
import app.dnd.DropController;

import java.util.Stack;

public class CellTreeDropController implements DropController {
    private final DNDTreeViewModel model;
    private CellTree tree;
    private Pos lastPos;
    private DNDTreeViewModel.DNDNodeInfo positioner = null;

    public CellTreeDropController(DNDTreeViewModel model, CellTree tree) {
        this.model = model;
        this.tree = tree;
    }

    public void onMouseMove(MouseMoveEvent event) {
        NodeList<Element> dropTargetList = tree.getElement().getElementsByTagName(DNDTreeViewModel.TAG);
        Element dropTarget = findClosestElement(event, dropTargetList);
        int x = event.getRelativeX(dropTarget);
        int y = event.getRelativeY(dropTarget);
        String key = dropTarget.getAttribute(DNDTreeViewModel.KEY_ATTR);
        String c = putPositionerAt(new Pos(x >= 16 ? 16 : 0, y > 10 ? 1 : 0, key));
    }

    private Element findClosestElement(MouseMoveEvent event, NodeList<Element> dropTargetList) {
        int top = 0; // element list order is from top to bottom
        int bottom = dropTargetList.getLength() - 1;
        int topY = Integer.MAX_VALUE;
        int bottomY = Integer.MAX_VALUE;
        while (true) {
            final int topToBottomDistance = bottom - top;
            if (topToBottomDistance <= 1) {
                break;
            }
            int middle = top + (topToBottomDistance / 2);
            Element dropTarget = dropTargetList.getItem(middle);
            // since all of tree nodes are vertical list all we care about are Y coordinates
            int y = event.getRelativeY(dropTarget);
            if (y > 0) { // if mouse is below this drop target
                top = middle; // look at bottom half
                topY = y;
            } else {
                bottom = middle; // look at top half
                bottomY = y;
            }
        }
        // make sure we have Y set for both borders
        if (topY == Integer.MAX_VALUE) {
            Element dropTarget = dropTargetList.getItem(top);
            topY = event.getRelativeY(dropTarget);
        }
        if (bottomY == Integer.MAX_VALUE) {
            Element dropTarget = dropTargetList.getItem(bottom);
            bottomY = event.getRelativeY(dropTarget);
        }
        Element dropTarget;
        // pick dropTarget that closer to the mouse position
        if (Math.abs(topY) > Math.abs(bottomY)) {
            dropTarget = dropTargetList.getItem(bottom);
        } else {
            dropTarget = dropTargetList.getItem(top);
        }
        return dropTarget;
    }

    public String putPositionerAt(Pos pos) {
        if (pos.y <= 0) {
            // horizontal shift only affects when cursor below node
            pos.x = 0;
        }
        if (pos.equals(lastPos)) {
            return "eq";
        }
        if (positioner != null) {
            positioner.remove();
            positioner.refresh();
        }
        DNDTreeViewModel.DNDNodeInfo newPositioner = null;
        DNDTreeViewModel.DNDNodeInfo relativeToDNDNodeInfo = model.getDNDNodeInfo(pos.key);
        if (relativeToDNDNodeInfo != null) {
            newPositioner = insertPositioner(pos.x, pos.y, relativeToDNDNodeInfo);
            if (newPositioner == null) {
                return "null";
            }
            newPositioner.refresh();
        }
        positioner = newPositioner;
        lastPos = pos;
        return "";
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    private DNDTreeViewModel.DNDNodeInfo insertPositioner(int x, int y, DNDTreeViewModel.DNDNodeInfo relativeNode) {
        if (y > 0 && x > 0) {
            Stack<DNDTreeViewModel.DNDNodeInfo> nodesToOpen = new Stack<DNDTreeViewModel.DNDNodeInfo>();
            DNDTreeViewModel.DNDNodeInfo nodePos = relativeNode;
            while (nodePos != null) {
                nodesToOpen.push(nodePos);
                nodePos = model.getDNDNodeInfo(nodePos.getParentKey());
            }
            TreeNode treeNode = tree.getRootTreeNode();
            while (!nodesToOpen.empty()) {
                nodePos = nodesToOpen.pop();
                treeNode = treeNode.setChildOpen(nodePos.indexOf(), true);
            }
            if (treeNode != null && treeNode.getChildCount() > 0) {
                // if has children  insert positioner below this node without offset
                // user still can insert it as child by shifting slightly down
                x = 0;
            }
        } else {
            x = 0;
        }
        model.setPositionerOffset(x > 0);
        int idx = relativeNode.indexOf() + y;
        return relativeNode.addSibling(idx, model.getPositionerItem());
    }

    public void drop(MouseUpEvent event, DNDContext dndContext) {
        if (positioner == null) {
            dndContext.revert();
        } else {
            model.drop(positioner, dndContext);
            positioner.remove();
            positioner.refresh();
        }
        positioner = null;
    }

    public static class Pos {
        private int x;
        private int y;
        private String key;

        public Pos(int x, int y, String key) {
            this.x = x;
            this.y = y;
            this.key = key;
        }

        @SuppressWarnings({"SimplifiableIfStatement"})
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pos pos = (Pos) o;

            if (x != pos.x) return false;
            if (y != pos.y) return false;
            return !(key != null ? !key.equals(pos.key) : pos.key != null);

        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + (key != null ? key.hashCode() : 0);
            return result;
        }
    }
}
