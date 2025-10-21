package pl.uni.graphs;

import org.graphstream.ui.view.Viewer;
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

        // ================================
        // === Part 0 : Exercise 1 ========
        // ================================
        System.out.println("\n--- Exercise 1 on firstgraphlab2.dgs ---");
        Graph g0 = Tools.readGraph("dgs/firstgraphlab2.dgs");
        g0.setAttribute("ui.title", "Exercise 1 – Neighbor Cost Threshold"); // title
        Viewer v0 = g0.display(true);                                         // new window for g0
        v0.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);            // X closes window only


        double avgDeg = Tools.averageDegree(g0);
        System.out.printf("[g0] Average degree = %.2f%n", avgDeg);  // (Ex.1 part 1) :contentReference[oaicite:3]{index=3}
        // App.java (right after printing average degree)
        g0.setAttribute("ui.stylesheet", """
  node { size: 8px; fill-color: #aab; text-size: 10; }
  edge { size: 1px; fill-color: #bbb; }
""");
        int THRESHOLD = 30;
        int marked = Tools.styleByNeighborCostSum(g0, THRESHOLD);
        System.out.println("[g0] Nodes marked > threshold = " + marked);

        // ================================
// === Exercise 2 : BFS Evolution ===
// ================================
        System.out.println("\n--- Exercise 2 ---");
        Graph g1 = Tools.readGraph("dgs/gridvonneumann_30.dgs"); //using gridvonneumann_30.dgs instead of completegrid_10.gds - couldn't find this file on moodle

// Title + own window
        g1.setAttribute("ui.title", "Exercise 2 – BFS Traversal (Evolution)");
        Viewer v1 = g1.display();
        v1.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        v1.disableAutoLayout();

        Node start = Tools.pickCenterNode(g1);
        System.out.println("[g1] start node = " + start.getId() +
                ", xy=" + java.util.Arrays.toString(Tools.getXY(start)));

// (opcjonalnie podświetl start, żebyś widziała gdzie zaczyna)
        start.setAttribute("ui.class", "start");

// CSS (dodaj klasę start)
        g1.setAttribute("ui.stylesheet", """
  node { size: 4px; fill-color: #000; }
  node.start { size: 7px; fill-color: #00aa00; stroke-mode: plain; stroke-color: #0a0; stroke-width: 2px; }
  node.queued  { size: 5px; fill-color: #333; }
  node.visited { size: 4px; fill-color: #000; }
  edge { size: 1px; fill-color: #f00; }
  edge.frontier { size: 2px; fill-color: #f90; }
  edge.tree     { size: 2px; fill-color: #f00; }
""");

        TraversalAlgorithms.bfsEvolution(g1, start, 15);

// ================================
// === Exercise 3 : DFS Evolution ===
// ================================
        System.out.println("\n--- Exercise 3 on gridvaluated_30_120.dgs ---");
        Graph g2a = Tools.readGraph("dgs/gridvaluated_30_120.dgs");
        g2a.setAttribute("ui.title", "Exercise 3 – DFS on complete grid (30x30)");

        Viewer v2a = g2a.display();
        for (Node n : g2a) n.removeAttribute("ui.label");

        v2a.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        v2a.disableAutoLayout();
        g2a.setAttribute("ui.stylesheet", """
  node { size: 5px; fill-color: #ff4d4d; text-size: 0; }
  node.stack { size: 6px; fill-color: #ffa500; }
  node.visited { size: 5px; fill-color: #000000; }
  node.backtracked { size: 5px; fill-color: #666666; }
  edge { size: 1px; fill-color: #ff0000; }
  edge.stackEdge { size: 2px; fill-color: #ff9900; }
  edge.tree { size: 2px; fill-color: #ff0000; }
""");
        Node startA = Tools.pickCenterNode(g2a);
        TraversalAlgorithms.dfsEvolution(g2a, startA, 5);

        System.out.println("\n--- Exercise 3 on randomgnp_50_0.05.dgs ---");
        Graph g2b = Tools.readGraph("dgs/randomgnp_50_0.05.dgs");
        g2b.setAttribute("ui.title", "Exercise 3 – DFS on random graph (p=0.05)");
        Viewer v2b = g2b.display();
        for (Node n : g2a) n.removeAttribute("ui.label");

        v2b.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
// random graph nie ma koordynatów → nie wyłączaj layoutu
        g2b.setAttribute("ui.stylesheet", """
  node { size: 5px; fill-color: #ff4d4d; text-size: 0; }
  node.stack { size: 6px; fill-color: #ffa500; }
  node.visited { size: 5px; fill-color: #000000; }
  node.backtracked { size: 5px; fill-color: #666666; }
  edge { size: 1px; fill-color: #ff0000; }
  edge.stackEdge { size: 2px; fill-color: #ff9900; }
  edge.tree { size: 2px; fill-color: #ff0000; }
""");
        // start w największej składowej:
        Node startB = Tools.pickNodeInLargestComponent(g2b);
        TraversalAlgorithms.dfsEvolution(g2b, startB, 5);


        /*
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

         */
    }


}
