package pl.uni.graphs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;
import java.util.Random;

import java.io.InputStream;
import java.util.Locale;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

//Utility helpers used across Lab 2.
public class Tools {
    public static void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    public static void hitakey(String message) {
        System.out.println(message);
    }

    public static int getInt(Node n, String key, int def) {
        if (!n.hasAttribute(key)) return def;
        Object v = n.getAttribute(key);
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return def; }
    }

    public static int styleByNeighborCostSum(Graph g, int threshold) {
        int marked = 0;
        for (Node v : g) {
            int sum = 0;
            for (Node nb : v.neighborNodes().toList())
                sum += getInt(nb, "cost", 0);
            if (sum > threshold) {
                v.setAttribute("ui.style", "size: 30px; fill-color: red;");
                v.setAttribute("ui.label", "sum=" + sum);
                marked++;
            }
        }
        return marked;
    }

    public static double[] getXY(Node v) {
        Object[] arr = v.getArray("ui.xy");
        if (arr == null) arr = v.getArray("xy");
        if (arr != null && arr.length >= 2) {
            return new double[] {
                    ((Number) arr[0]).doubleValue(),
                    ((Number) arr[1]).doubleValue()
            };
        }
        if (v.hasNumber("x") && v.hasNumber("y")) {
            return new double[] { v.getNumber("x"), v.getNumber("y") };
        }
        return null;
    }

    public static Node pickCenterNode(Graph g) {
        double cx = 0, cy = 0; int n = 0;

        for (Node v : g) {
            double[] xy = getXY(v);
            if (xy != null) { cx += xy[0]; cy += xy[1]; n++; }
        }
        if (n == 0) {
            return g.getNode(g.getNodeCount()/2);
        }
        cx /= n; cy /= n;

        Node best = null; double bestD = Double.POSITIVE_INFINITY;
        for (Node v : g) {
            double[] xy = getXY(v);
            if (xy == null) continue;
            double d = Math.hypot(xy[0] - cx, xy[1] - cy);
            if (d < bestD) { bestD = d; best = v; }
        }
        return best != null ? best : g.getNode(g.getNodeCount()/2);
    }

    public static Node pickNodeInLargestComponent(Graph g) {
        Set<Node> globalVisited = new HashSet<>();
        Node bestNode = null; int bestSize = -1;

        for (Node s : g) {
            if (globalVisited.contains(s)) continue;
            ArrayDeque<Node> q = new ArrayDeque<>();
            Set<Node> comp = new HashSet<>();
            q.add(s); comp.add(s);
            while (!q.isEmpty()) {
                Node u = q.removeFirst();
                for (Node v : u.neighborNodes().toList()) {
                    if (!comp.contains(v)) { comp.add(v); q.add(v); }
                }
            }
            globalVisited.addAll(comp);
            if (comp.size() > bestSize) { bestSize = comp.size(); bestNode = s; }
        }
        return bestNode != null ? bestNode : g.getNode(0);
    }

    public static Graph readGraph(String resourcePath) {
        var url = Tools.class.getClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new IllegalArgumentException("Resource not found on classpath: " + resourcePath);
        }

        Graph g = new SingleGraph(resourcePath);
        FileSource fs = new FileSourceDGS();
        fs.addSink(g);
        try {
            fs.readAll(url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read DGS: " + resourcePath, e);
        } finally {
            fs.removeSink(g);
        }
        return g;
    }

    public static double averageDegree(Graph g) {
        double sum = 0.0;
        for (Node n : g) sum += n.getDegree();
        return (g.getNodeCount() == 0) ? 0.0 : sum / g.getNodeCount();
    }

    public static void label(Node n, String top, String bottom) {
        n.setAttribute("ui.label", String.format(Locale.US, "%s\n%s", top, bottom));
    }

    public static double weight(Edge e) {
        String[] keys = { "length", "weight", "w", "cost", "value" };
        for (String k : keys) {
            if (e.hasAttribute(k)) {
                Object v = e.getAttribute(k);
                if (v instanceof Number) {
                    return ((Number) v).doubleValue();
                }
                try {
                    return Double.parseDouble(String.valueOf(v));
                } catch (Exception ignore) {
                }
            }
        }
        return 1.0;
    }

    public static void highlightSPTree(Graph g, String edgeClass) {
        for (Edge e : g.edges().toList()) e.removeAttribute("ui.class");
        for (Node n : g) {
            Object p = n.getAttribute("pred");
            Node parent = null;
            if (p instanceof Node) parent = (Node)p;
            else if (p != null) parent = g.getNode(String.valueOf(p));
            if (parent != null) {
                Edge e = n.getEdgeBetween(parent);
                if (e != null) e.setAttribute("ui.class", edgeClass);
            }
        }
    }
}
