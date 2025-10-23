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
    //simple sleep helper for visualization delays.
    public static void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    //Prints a message to the console (used as breakpoint).
    public static void hitakey(String message) {
        System.out.println(message);
    }

    //Reads integer attribute from a node with default value if missing.
    public static int getInt(Node n, String key, int def) {
        if (!n.hasAttribute(key)) return def;
        Object v = n.getAttribute(key);
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return def; }
    }

    //Highlights nodes whose neighbors' cost sum exceeds a given threshold
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

    //Returns XY coordinates of a node if available (ui.xy, xy, or x/y).
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

    //Picks a node located near the geometric center of the graph.
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

    //Returns a node from the largest connected component of the graph.
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

    //Reads a DGS file from resources and returns a GraphStream Graph object.
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

    //Computes and returns the average degree of the graph.
    public static double averageDegree(Graph g) {
        double sum = 0.0;
        for (Node n : g) sum += n.getDegree();
        return (g.getNodeCount() == 0) ? 0.0 : sum / g.getNodeCount();
    }

    //Sets a two-line label on a node (top/bottom text)
    public static void label(Node n, String top, String bottom) {
        n.setAttribute("ui.label", String.format(Locale.US, "%s\n%s", top, bottom));
    }

    //Returns edge weight based on possible attribute names.
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

    //Highlights a shortest-path or spanning tree using 'pred' attributes.
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

    //Ensures the graph has xy coordinates (copies or generates grid layout).
    public static void ensureGridLayout(Graph g, double spacing) {
        // 1) Check if graph already has coordinates
        boolean hasXY = true;
        for (Node n : g) {
            Object[] xy = n.getArray("ui.xy");
            Object[] xy2 = n.getArray("xy");
            if (!((xy != null && xy.length >= 2) || (xy2 != null && xy2.length >= 2))) {
                hasXY = false; break;
            }
        }
        if (hasXY) return;

        // 2) Copy x/y → xy if available
        boolean hasXYfromXY = true;
        for (Node n : g) {
            if (n.hasNumber("x") && n.hasNumber("y")) {
                n.setAttribute("xy", n.getNumber("x"), n.getNumber("y"));
            } else {
                hasXYfromXY = false;
            }
        }
        if (hasXYfromXY) return;

        // 3) Generate regular grid layout if no coordinates found
        int V = g.getNodeCount();
        int N = (int) Math.ceil(Math.sqrt(V));
        int i = 0;
        for (Node n : g) {
            int row = i / N;
            int col = i % N;
            double x = col * spacing;
            double y = row * spacing;
            n.setAttribute("xy", x, y);
            i++;
        }
    }

}
