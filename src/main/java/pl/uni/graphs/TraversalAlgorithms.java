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

    public static class DR { public final double diameter, radius;
        public DR(double d, double r){ this.diameter=d; this.radius=r; }
    }

    public static void dijkstra(Graph g, Node source) {
        for (Node v : g) {
            v.setAttribute("dist", Double.POSITIVE_INFINITY);
            v.removeAttribute("pred");
        }
        source.setAttribute("dist", 0.0);

        ArrayList<Node> pq = new ArrayList<>();
        pq.add(source);

        while (!pq.isEmpty()) {
            Node u = pq.remove(0);
            Iterator<Node> it = u.neighborNodes().iterator();
            while (it.hasNext()) {
                Node v = it.next();
                Edge e = u.getEdgeBetween(v);
                double alt = u.getNumber("dist") + Tools.weight(e);

                if (alt < v.getNumber("dist")) {
                    v.setAttribute("dist", alt);
                    v.setAttribute("pred", u.getId());

                    pq.remove(v);
                    int idx = 0;
                    while (idx < pq.size() && pq.get(idx).getNumber("dist") <= alt) idx++;
                    pq.add(idx, v);
                }
            }
        }
    }

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

    public static void applyEccentricityHeatmap(Graph g) {
        double diameter = g.getNumber("diameter");
        double radius   = g.getNumber("radius");

        for (Node v : g) {
            double ecc = v.getNumber("ecc");
            v.setAttribute("ui.style", colorForEcc(ecc, radius, diameter));
            v.setAttribute("ui.label", String.format("%s\nEcc=%.2f", v.getId(), ecc));
        }
    }

    private static String colorForEcc(double ecc, double radius, double diameter) {
        double t = (diameter > radius) ? (ecc - radius) / (diameter - radius) : 0.0;
        if (t < 0) t = 0; if (t > 1) t = 1;
        int r = (int) Math.round(255 * t);
        int g = 0;
        int b = (int) Math.round(255 * (1 - t));
        return String.format("fill-color: rgb(%d,%d,%d); size: 15px;", r, g, b);
    }

    public static void resetTraversal(Graph g) {
        for (Node n : g) {
            n.removeAttribute("visited");
            n.removeAttribute("parent");
        }
        g.edges().forEach(e -> e.removeAttribute("ui.style"));
    }

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
        for (Node n : g) {
            n.removeAttribute("visited");
            n.removeAttribute("ui.class");
        }
        for (Edge e : g.edges().toList()) {
            e.removeAttribute("ui.class");
        }

        ArrayList<Node> q = new ArrayList<>();
        int head = 0;

        source.setAttribute("visited", true);
        source.setAttribute("ui.class", "visited");
        q.add(source);
        Tools.pause(delayMs);

        while (head < q.size()) {
            Node u = q.get(head++);

            for (Node v : u.neighborNodes().toList()) {
                if (!v.hasAttribute("visited")) {
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

    public static void dfsEvolution(Graph g, Node source, int delayMs) {
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
            Collections.shuffle(nbrs);

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

    public static int bfsTree(Graph g, Node source) {
        for (Node n : g) { n.removeAttribute("visited"); n.removeAttribute("pred"); }
        Deque<Node> q = new ArrayDeque<>();
        source.setAttribute("visited", true);
        q.add(source);
        int treeEdges = 0;

        long seed = 67890L;
        java.util.Random rng = new java.util.Random(seed);

        while (!q.isEmpty()) {
            // zbierz całą warstwę (bieżący fron­t)
            int levelSize = q.size();
            List<Node> nextFrontier = new ArrayList<>();

            for (int i = 0; i < levelSize; i++) {
                Node u = q.removeFirst();

                // sąsiedzi w losowej kolejności
                List<Node> nbrs = new ArrayList<>(u.neighborNodes().toList());
                java.util.Collections.shuffle(nbrs, rng);

                for (Node v : nbrs) {
                    if (!v.hasAttribute("visited")) {
                        v.setAttribute("visited", true);
                        v.setAttribute("pred", u);
                        treeEdges++;
                        nextFrontier.add(v); // dodaj do następnej warstwy
                    }
                }
            }

            // losowa permutacja całej następnej warstwy → „ziarnisty” wygląd
            java.util.Collections.shuffle(nextFrontier, rng);
            for (Node v : nextFrontier) q.addLast(v);
        }
        return treeEdges;
    }

    public static int dfsTree(Graph g, Node source) {
        for (Node n : g) { n.removeAttribute("visited"); n.removeAttribute("pred"); }
        Deque<Node> stack = new ArrayDeque<>();
        source.setAttribute("visited", true);
        stack.push(source);
        int treeEdges = 0;
        long seed = 12345L;
        while (!stack.isEmpty()) {
            Node u = stack.peek();
            Node next = null;
            for (Node v : shuffledNeighbors(u, seed)) {
                if (!v.hasAttribute("visited")) { next = v; break; }
            }
            if (next != null) {
                next.setAttribute("visited", true);
                next.setAttribute("pred", u);
                treeEdges++;
                stack.push(next);
            } else {
                stack.pop();
            }
        }
        return treeEdges;
    }

    private static List<Node> shuffledNeighbors(Node u, long seed) {
        List<Node> list = new ArrayList<>(u.neighborNodes().toList());
        Collections.shuffle(list, new java.util.Random(seed ^ u.getIndex()));
        return list;
    }








}
