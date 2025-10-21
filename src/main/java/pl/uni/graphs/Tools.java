package pl.uni.graphs;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;

import java.io.InputStream;
import java.util.Locale;

/**
 * Utility helpers used across Lab 2.
 */
public class Tools {

    /** Small sleep helper (milliseconds). */
    public static void pause(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    /** Compatibility stub for examples. */
    public static void hitakey(String message) {
        System.out.println(message);
        // no-op; we don't pause for a keypress in this project
    }




    /**
     * Load a DGS graph from the classpath.
     * Example usage: readGraph("dgs/gridvaluated_10_12.dgs")
     *
     * Put your .dgs files under src/main/resources/dgs/
     */
    public static Graph readGraph(String resourcePath) {
        // Load the DGS file from the classpath as a URL
        var url = Tools.class.getClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new IllegalArgumentException("Resource not found on classpath: " + resourcePath);
        }

        Graph g = new SingleGraph(resourcePath);
        FileSource fs = new FileSourceDGS();
        fs.addSink(g);
        try {
            // GS 2.x: one-shot parse, no begin()/end(), no loops
            fs.readAll(url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read DGS: " + resourcePath, e);
        } finally {
            fs.removeSink(g);
        }
        return g;
    }



    /** Average degree (undirected). */
    public static double averageDegree(Graph g) {
        double sum = 0.0;
        for (Node n : g) sum += n.getDegree();
        return (g.getNodeCount() == 0) ? 0.0 : sum / g.getNodeCount();
    }

    /** Convenience two-line node label: top line (usually ID) + bottom line (e.g., distance). */
    public static void label(Node n, String top, String bottom) {
        n.setAttribute("ui.label", String.format(Locale.US, "%s\n%s", top, bottom));
    }

    // --- Edge weight helper used by Dijkstra ------------------------------------

    /**
     * Returns the numeric weight of an edge. If no known attribute is present,
     * it defaults to 1.0 (unweighted).
     *
     * Recognized attribute keys (first match wins):
     *   "length", "weight", "w", "cost", "value"
     *
     * Robust to both numeric attributes and stringified numbers.
     */
    public static double weight(Edge e) {
        String[] keys = { "length", "weight", "w", "cost", "value" };
        for (String k : keys) {
            if (e.hasAttribute(k)) {
                Object v = e.getAttribute(k);
                if (v instanceof Number) {
                    return ((Number) v).doubleValue();
                }
                // Sometimes attributes are stored as strings; try parsing.
                try {
                    return Double.parseDouble(String.valueOf(v));
                } catch (Exception ignore) {
                    // fall through
                }
            }
        }
        // Fallback for unweighted graphs
        return 1.0;
    }
}
