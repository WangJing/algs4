import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;

public class KdTree {
    private class KdTreeNode implements Comparable<Point2D> {

        private final Point2D p;
        private final RectHV rect;

        private KdTreeNode left;
        private KdTreeNode right;
        private int size;

        public KdTreeNode(Point2D p, RectHV rect) {
            this.p = p;
            this.rect = rect;
            this.size = 1;
        }

        public final Point2D getPoint() {
            return p;
        }

        public final RectHV getRect() {
            return rect;
        }


        public KdTreeNode subTreeNode(Point2D other){ return null; };

        public void draw(){ return; }

        @Override
        public int compareTo(Point2D o) {
            return 0;
        };
    }

    private class HorizontalKdTreeNode extends KdTreeNode {

        public HorizontalKdTreeNode(Point2D p, RectHV rect) {
            super(p, rect);
        }

        @Override
        public int compareTo(Point2D other) {
            return Double.compare(getPoint().y(), other.y());
        }

        @Override
        public void draw() {
            // StdDraw.setPenColor(StdDraw.BLACK);
            // StdDraw.setPenRadius(0.005);
            // StdDraw.point(getPoint().x(), getPoint().y());

            // StdDraw.setPenColor(StdDraw.BLUE);
            // StdDraw.setPenRadius(0.001);
            // StdDraw.line(getRect().xmin(), getPoint().y(), getRect().xmax(), getPoint().y());
        }

        @Override
        public KdTree.KdTreeNode subTreeNode(Point2D other) {
            int cmp = compareTo(other);
            if (cmp < 0) // right branch (upper)
                return new VerticalKdTreeNode(other,
                        new RectHV(getRect().xmin(), getPoint().y(), getRect().xmax(), getRect().ymax()));
            else if (cmp >= 0) // left branch (lower), go left when equal
                return new VerticalKdTreeNode(other,
                        new RectHV(getRect().xmin(), getRect().ymin(), getRect().xmax(), getPoint().y()));
            return null;
        }
    }

    private class VerticalKdTreeNode extends KdTreeNode {

        public VerticalKdTreeNode(Point2D p, RectHV rect) {
            super(p, rect);
        }

        @Override
        public int compareTo(Point2D other) {
            return Double.compare(getPoint().x(), other.x());
        }

        @Override
        public KdTree.KdTreeNode subTreeNode(Point2D other) {
            int cmp = compareTo(other);
            // right branch
            if (cmp < 0)
                return new HorizontalKdTreeNode(other,
                        new RectHV(getPoint().x(), getRect().ymin(), getRect().xmax(), getRect().ymax()));
            // left branch, go left when equal
            else if (cmp >= 0)
                return new HorizontalKdTreeNode(other,
                        new RectHV(getRect().xmin(), getRect().ymin(), getPoint().x(), getRect().ymax()));
            return null;
        }

        @Override
        public void draw() {
            // StdDraw.setPenColor(StdDraw.BLACK);
            // StdDraw.setPenRadius(0.005);
            // StdDraw.point(getPoint().x(), getPoint().y());

            // StdDraw.setPenColor(StdDraw.RED);
            // StdDraw.setPenRadius(0.001);
            // StdDraw.line(getPoint().x(), getRect().ymin(), getPoint().x(), getRect().ymax());
        }

    }

    private KdTreeNode root = null;
    private static final Point2D P_INFINITY = new Point2D(100, 100);

    public KdTree() {
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int size() {
        return root == null ? 0 : root.size;
    }

    public void insert(Point2D p) {
        if (root == null)
            root = new VerticalKdTreeNode(p, new RectHV(0, 0, 1, 1));
            // root = new HorizontalKdTreeNode(p, new RectHV(0, 0, 1, 1));
        else
            insert(root, null, p);
    }

    private KdTreeNode insert(KdTreeNode node, KdTreeNode parent, Point2D point) {
        if (node == null)
            return parent.subTreeNode(point);
        // do not insert duplicated node
        if (node.p.equals(point)) return node; 
        int cmp = node.compareTo(point);
        if (cmp >= 0) // left subtree, go left when equal
            node.left = insert(node.left, node, point);
        else if (cmp < 0)
            node.right = insert(node.right, node, point);
        // re-calculate node number
        int leftSize = node.left != null ? node.left.size : 0;
        int rightSize = node.right != null ? node.right.size : 0;
        node.size = leftSize + rightSize + 1;
        return node;
    }

    public boolean contains(Point2D p) {
        KdTreeNode x = root;
        while (x != null) {
            if (x.p.equals(p)) return true;
            int cmp = x.compareTo(p);
            if (cmp >= 0)   // go left when equal
                x = x.left;
            else if (cmp < 0)
                x = x.right;
        }
        return false;
    }

    public void draw() {
        // StdDraw.setXscale(0, 1);
        // StdDraw.setYscale(0, 1);
        // draw(root);
        // StdDraw.show();
        // return;
    }

    private void draw(KdTreeNode n) {
        // if (n == null)
        //     return;
        // n.draw();
        // draw(n.left);
        // draw(n.right);
    }

    public Iterable<Point2D> range(RectHV rect) {
        List<Point2D> inRangeNodes = new ArrayList<>();
        range(root, rect, inRangeNodes);

        return new Iterable<Point2D>() {
            @Override
            public Iterator<Point2D> iterator() {
                return inRangeNodes.iterator();
            }

        };
    }

    private void range(KdTreeNode x, RectHV rect, List<Point2D> nodes) {
        if (x == null)
            return;
        if (!x.rect.intersects(rect))
            return;
        if (rect.contains(x.p))
            nodes.add(x.p);
        range(x.left, rect, nodes);
        range(x.right, rect, nodes);
    }

    public Point2D nearest(Point2D p) {
        return nearest(root, p);
    }

    private Point2D nearest(KdTreeNode x, Point2D target) {
        if (x == null) return null;

        KdTreeNode firstSubTree, secondSubTree;
        int cmp = x.compareTo(target);

        if (cmp < 0) {
            firstSubTree = x.right;
            secondSubTree = x.left;
        } else {  // go left when equal
            firstSubTree = x.left;
            secondSubTree = x.right;
        }

        Point2D nearestPoint = x.p;
        if (firstSubTree != null) {
            Point2D tmp = nearest(firstSubTree, target);
            if (tmp != null && tmp.distanceTo(target) < nearestPoint.distanceTo(target)) nearestPoint = tmp;
        }

        if (secondSubTree != null && secondSubTree.rect.distanceSquaredTo(target) < nearestPoint.distanceSquaredTo(target)) {
            Point2D tmp = nearest(secondSubTree, target);
            if (tmp != null && tmp.distanceSquaredTo(target) < nearestPoint.distanceSquaredTo(target)) nearestPoint = tmp;
        }
        return nearestPoint;
    }

    public static void main(String[] args) {
    }


}
