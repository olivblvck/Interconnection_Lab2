package pl.uni.graphs;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;


/**
 * Lab 2 main runner.
 *
 * Part A  -> Dijkstra on a weighted grid (show node distances as labels)
 * Part B  -> Eccentricities and heatmap (blue = center, red = periphery)
 * Optional -> Random G(n,p) graph for connectivity test
 *
 * DGS files are stored in: src/main/resources/dgs/
 */
public class App {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        System.setProperty("org.graphstream.ui", "swing");

        String baseCss = """
    node { size: 5px; text-size: 10; text-alignment: at-right; text-offset: 2px, 0px; }
    edge { size: 1px; fill-color: #999; }
""";



        // ================================================================
        // === Part A : Dijkstra on weighted grid (10x10) ================
        // ================================================================
        System.out.println("\n--- Running Dijkstra on gridvaluated_10_12.dgs ---");
        Graph g1 = Tools.readGraph("dgs/gridvaluated_10_12.dgs");
        g1.display(false); // don't use auto layout (DGS has coordinates)

        // Choose a random source node
        Node src = g1.getNode((int) (Math.random() * g1.getNodeCount()));

        System.out.println("Random source node: " + src.getId());

        // Run Dijkstra
        TraversalAlgorithms.dijkstra(g1, src);

        // Display distances as node labels
        for (Node v : g1) {
            double d = v.getNumber("dist");
            Tools.label(v, v.getId(), String.format("%.2f", d));
        }

        System.out.printf("[g1] Average degree = %.2f%n", Tools.averageDegree(g1));

        // ================================================================
        // === Part B : Eccentricities + Heatmap (30x30) ================
        // ================================================================
        System.out.println("\n--- Computing eccentricities on gridvaluated_30_120.dgs ---");
        Graph g2 = Tools.readGraph("dgs/gridvaluated_30_120.dgs");
        g2.display(false);

        // Compute eccentricities (and diameter / radius)
        TraversalAlgorithms.DR dr = TraversalAlgorithms.computeEccentricities(g2);
        System.out.printf("[g2] Diameter = %.2f, Radius = %.2f%n", dr.diameter, dr.radius);

        // Apply color heatmap based on eccentricity
        TraversalAlgorithms.applyEccentricityHeatmap(g2);

        // ================================================================
        // === Optional : Random G(n,p) connectivity test ================
        // ================================================================
        System.out.println("\n--- Testing random graph randomgnp_50_0.05.dgs ---");
        Graph g3 = Tools.readGraph("dgs/randomgnp_50_0.05.dgs");
        g3.display();

        System.out.println("[g3] Nodes: " + g3.getNodeCount() +
                " | Edges: " + g3.getEdgeCount());

        // If your TraversalAlgorithms still includes the isConnected() method
        // from the professor’s BFS/DFS example, you can uncomment this:
        // boolean connected = TraversalAlgorithms.isConnected(g3);
        // System.out.println("[g3] Connected? " + connected);
        Graph gST = Tools.readGraph("dgs/gridvonneumann_30.dgs");
        gST.setAttribute("ui.stylesheet", baseCss);
        String stCss = """
    edge { size: 1px; fill-color: red; }   
    node { size: 3px; }
""";
        gST.setAttribute("ui.stylesheet", stCss);

        gST.display(false);

// choose BFS or DFS

        Node start = gST.getNode("15-15");
        if (start == null) start = gST.getNode(gST.getNodeCount() / 2);

// uruchom BFS lub DFS
        TraversalAlgorithms.bfsSpanningTree(gST, start);
// TraversalAlgorithms.dfsSpanningTree(gST, start);

// TraversalAlgorithms.dfsSpanningTree(gST, gST.getNode(0));

        System.out.println("\n--- All tasks executed successfully ---");
    }
}
