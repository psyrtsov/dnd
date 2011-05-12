package app.dnd.tree;

import app.dnd.DNDContext;
import app.dnd.DropController;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;

import java.util.Stack;

public class CellTreeDropController extends DNDTreeViewModelAdaper implements DropController {
    private CellTree tree;
    private Pos lastPos;
    private DNDTreeViewModelAdaper.DNDNodeInfo positioner = null;

    public void init(CellTree tree, DNDTreeViewModel dndTreeViewModel, Object rootValue) {
        super.init(dndTreeViewModel, rootValue);
        this.tree = tree;
    }

    public void onMouseMove(MouseMoveEvent event) {
        NodeList<Element> dropTargetList = tree.getElement().getElementsByTagName(DNDTreeViewModelAdaper.TAG);
        Element dropTarget = findClosestElement(event, dropTargetList);
        int x = event.getRelativeX(dropTarget);
        int y = event.getRelativeY(dropTarget);
        String key = dropTarget.getAttribute(DNDTreeViewModelAdaper.KEY_ATTR);
        putPositionerAt(new Pos(x >= 16 ? 16 : 0, y > 10 ? 1 : 0, key));
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

    public void putPositionerAt(Pos pos) {
        if (pos.y <= 0) {
            // horizontal shift only affects when cursor below node
            pos.x = 0;
        }
        if (pos.equals(lastPos)) {
            return;
        }
        if (positioner != null) {
            positioner.remove();
            positioner.refresh();
        }
        positioner = putPositioner(pos.x > 0, pos.y > 0, pos.key);
        lastPos = pos;
    }

    public DNDTreeViewModelAdaper.DNDNodeInfo putPositioner(boolean xShifted, boolean insertBelow, String key) {
        DNDTreeViewModelAdaper.DNDNodeInfo relativeNode = getDNDNodeInfo(key);
        if (relativeNode == null) {
            return null;
        }

        if (insertBelow && xShifted) {
            TreeNode treeNode = open(relativeNode, true);
            if (treeNode != null && treeNode.getChildCount() > 0) {
                // if has children  insert positioner below this node without offset
                // user still can insert it as child by shifting slightly to the right
                xShifted = false;
            }
        } else {
            xShifted = false;
        }
        setPositionerOffset(xShifted);
        int idx = relativeNode.indexOf();
        if (insertBelow) {
            idx++;
        }
        @SuppressWarnings({"unchecked"})
        final DNDTreeViewModelAdaper.DNDNodeInfo res = relativeNode.addSibling(idx, getPositionerItem());
        res.refresh();        
        return res;
    }

    public TreeNode open(DNDTreeViewModelAdaper.DNDNodeInfo nodeToOpen, final boolean open) {
        Stack<DNDTreeViewModelAdaper.DNDNodeInfo> nodesToOpen = new Stack<DNDTreeViewModelAdaper.DNDNodeInfo>();
        DNDTreeViewModelAdaper.DNDNodeInfo nodePos = nodeToOpen;
        while (nodePos != null) {
            if (nodePos.getParentDndNodeInfo() != null) { // don't push root
                nodesToOpen.push(nodePos);
            }
            nodePos = nodePos.getParentDndNodeInfo();
        }
        // open subtrees one by one
        TreeNode treeNode = tree.getRootTreeNode();
        while (!nodesToOpen.empty()) {
            nodePos = nodesToOpen.pop();
            final int idx = nodePos.indexOf();
            if (nodesToOpen.isEmpty()) {
                treeNode = treeNode.setChildOpen(idx, open);
            } else {
                treeNode = treeNode.setChildOpen(idx, true);
            }
        }
        return treeNode;
    }

    public void drop(MouseUpEvent event, DNDContext dndContext) {
        if (positioner == null) {
            dndContext.revert();
        } else {
            final boolean positionerOffset = isPositionerOffset();
            final DNDTreeViewModelAdaper.DNDNodeInfo parentDndNodeInfo = super.drop(positioner, dndContext);
            positioner.remove();
            if (positionerOffset) {
                open(parentDndNodeInfo, true);
            }
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
