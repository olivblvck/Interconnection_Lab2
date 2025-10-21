package pl.uni.graphs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;


import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.SingleGraph;


public class TraversalAlgorithms {

    /** Holder for diameter & radius. */
    public static class DR {
        public final double diameter;
        public final double radius;
        public DR(double d, double r) { this.diameter = d; this.radius = r; }
    }

    /**
     * Dijkstra using an ArrayList as a priority queue sorted by distance.
     * Edge weights are read via Tools.weight(e). Distances stored as node attribute "dist".
     */
    public static void dijkstra(Graph g, Node source) {
        // init
        for (Node v : g) {
            v.setAttribute("dist", Double.POSITIVE_INFINITY);
            v.removeAttribute("pred");
        }
        source.setAttribute("dist", 0.0);

        ArrayList<Node> pq = new ArrayList<>();
        pq.add(source);

        while (!pq.isEmpty()) {
            // extract-min (front of the list)
            Node u = pq.remove(0);

            Iterator<Node> it = u.neighborNodes().iterator();

            while (it.hasNext()) {
                Node v = it.next();
                Edge e = u.getEdgeBetween(v);
                double alt = u.getNumber("dist") + Tools.weight(e);

                // relaxation
                if (alt < v.getNumber("dist")) {
                    v.setAttribute("dist", alt);
                    v.setAttribute("pred", u.getId());

                    // decrease-key: remove if present, then insert sorted by new distance
                    pq.remove(v);
                    int idx = 0;
                    while (idx < pq.size() && pq.get(idx).getNumber("dist") <= alt) idx++;
                    pq.add(idx, v);
                }
            }
        }
    }

    /**
     * Computes eccentricity for every node using Dijkstra from each source.
     * Stores per-node "ecc" and graph attributes "diameter" and "radius".
     */
    public static DR computeEccentricities(Graph g) {
        double diameter = Double.NEGATIVE_INFINITY;
        double radius   = Double.POSITIVE_INFINITY;

        for (Node s : g) {
            dijkstra(g, s);
            double ecc = 0.0;
            for (Node v : g) {
                double d = v.getNumber("dist");
                if (d < Double.POSITIVE_INFINITY && d > ecc) ecc = d;
            }
            s.setAttribute("ecc", ecc);
            diameter = Math.max(diameter, ecc);
            radius   = Math.min(radius,   ecc);
        }

        g.setAttribute("diameter", diameter);
        g.setAttribute("radius",   radius);
        return new DR(diameter, radius);
    }

    /** Colors nodes blue→red according to eccentricity position between radius and diameter. */
    public static void applyEccentricityHeatmap(Graph g) {
        double diameter = g.getNumber("diameter");
        double radius   = g.getNumber("radius");

        for (Node v : g) {
            double ecc = v.getNumber("ecc");
            v.setAttribute("ui.style", colorForEcc(ecc, radius, diameter));
            v.setAttribute("ui.label", String.format("%s\nEcc=%.2f", v.getId(), ecc));
        }
    }

    /** CSS color string for a given eccentricity. */
    private static String colorForEcc(double ecc, double radius, double diameter) {
        double t = (diameter > radius) ? (ecc - radius) / (diameter - radius) : 0.0;
        if (t < 0) t = 0; if (t > 1) t = 1;
        int r = (int) Math.round(255 * t);
        int g = 0;
        int b = (int) Math.round(255 * (1 - t));
        return String.format("fill-color: rgb(%d,%d,%d); size: 15px;", r, g, b);
    }

    /** Clears traversal marks/styles used by spanning-tree demos. */
    public static void resetTraversal(Graph g) {
        for (Node n : g) {
            n.removeAttribute("visited");
            n.removeAttribute("parent");
        }
        g.edges().forEach(e -> e.removeAttribute("ui.style"));
    }

    /**
     * Spanning tree built by BFS (breadth-first). Edges in the tree are colored red & thicker.
     * Queue is implemented with ArrayList to stick to the lab’s hint.
     */
    /** BFS spanning tree – only new edges (tree edges) are red. */
    public static void bfsSpanningTree(Graph g, Node start) {
        resetTraversal(g);
        Queue<Node> q = new LinkedList<>();

        start.setAttribute("visited", true);
        q.add(start);

        while (!q.isEmpty()) {
            Node u = q.poll();
            for (Node v : u.neighborNodes().toList()) {
                if (!v.hasAttribute("visited")) {
                    v.setAttribute("visited", true);
                    v.setAttribute("parent", u.getId());

                    Edge e = u.getEdgeBetween(v);
                    if (e != null) e.setAttribute("ui.style", "fill-color: black; size: 3px;");


                    q.add(v);
                }
            }
        }
    }

    /**
     * Spanning tree built by DFS (depth-first). Edges in the tree are colored red & thicker.
     * Uses an explicit stack and the “peek + first-unvisited-neighbor” pattern to add tree edges only on discovery.
     */
    /** DFS spanning tree – uses stack, marks only discovery edges red. */
    public static void dfsSpanningTree(Graph g, Node start) {
        resetTraversal(g);
        Stack<Node> stack = new Stack<>();

        start.setAttribute("visited", true);
        stack.push(start);

        while (!stack.isEmpty()) {
            Node u = stack.peek();
            Node next = null;

            for (Node v : u.neighborNodes().toList()) {
                if (!v.hasAttribute("visited")) {
                    next = v;
                    break;
                }
            }

            if (next != null) {
                next.setAttribute("visited", true);
                next.setAttribute("parent", u.getId());

                Edge e = u.getEdgeBetween(next);
                if (e != null) e.setAttribute("ui.style", "fill-color: red; size: 3px;");

                stack.push(next);
            } else {
                stack.pop();
            }
        }
    }

    public static void bfsEvolution(Graph g, Node source, int delayMs) {
        // wyczyść poprzednie oznaczenia
        for (Node n : g) {
            n.removeAttribute("visited");
            n.removeAttribute("ui.class");
        }
        for (Edge e : g.edges().toList()) {    // <— WSZYSTKIE krawędzie, tylko do resetu
            e.removeAttribute("ui.class");
        }

        // kolejka jako ArrayList (zgodnie z poleceniem)
        ArrayList<Node> q = new ArrayList<>();
        int head = 0;

        source.setAttribute("visited", true);
        source.setAttribute("ui.class", "visited");
        q.add(source);
        Tools.pause(delayMs);

        while (head < q.size()) {
            Node u = q.get(head++);

            // KLUCZOWA ZMIANA: iterujemy po SĄSIADACH u
            for (Node v : u.neighborNodes().toList()) {   // <— tylko realni sąsiedzi
                if (!v.hasAttribute("visited")) {
                    // krawędź drzewa (między u a v); w grafach prostych będzie jedna
                    Edge e = u.getEdgeBetween(v);
                    if (e != null) {
                        e.setAttribute("ui.class", "frontier");
                    }

                    v.setAttribute("ui.class", "queued");
                    Tools.pause(delayMs);

                    v.setAttribute("visited", true);
                    v.setAttribute("ui.class", "visited");
                    if (e != null) {
                        e.setAttribute("ui.class", "tree");
                    }
                    q.add(v);

                    Tools.pause(delayMs);
                }
            }
        }
    }

    // importy: Graph, Node, Edge, ArrayDeque, ArrayList
    public static void dfsEvolution(Graph g, Node source, int delayMs) {
        // reset
        for (Node n : g) { n.removeAttribute("visited"); n.removeAttribute("ui.class"); }
        for (Edge e : g.edges().toList()) e.removeAttribute("ui.class");

        Deque<Node> stack = new ArrayDeque<>();
        source.setAttribute("visited", true);
        source.setAttribute("ui.class", "visited");
        stack.push(source);
        Tools.pause(delayMs);

        while (!stack.isEmpty()) {
            Node u = stack.peek();
            Node next = null; Edge via = null;
            List<Node> nbrs = new ArrayList<>(u.neighborNodes().toList());
            Collections.shuffle(nbrs); // losowa kolejność odwiedzania sąsiadów

            for (Node v : nbrs) {
                if (!v.hasAttribute("visited")) {
                    next = v;
                    via = u.getEdgeBetween(v);
                    break;
                }
            }
            if (next == null) {
                // backtrack
                u.setAttribute("ui.class", "backtracked");
                stack.pop();
                Tools.pause(delayMs);
            } else {
                if (via != null) via.setAttribute("ui.class", "stackEdge");
                next.setAttribute("ui.class", "stack");
                Tools.pause(delayMs);

                next.setAttribute("visited", true);
                next.setAttribute("ui.class", "visited");
                if (via != null) via.setAttribute("ui.class", "tree");
                stack.push(next);
                Tools.pause(delayMs);
            }
        }
    }

}
