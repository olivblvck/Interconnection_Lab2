package pl.uni.graphs;

import org.graphstream.ui.view.Viewer;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;

/**
 * Main application class executing all Lab 2 exercises sequentially.
 *
 * Exercises:
 * 1. Average degree and node highlighting by neighbor cost sum
 * 2. Breadth-First Search (BFS) traversal visualization
 * 3. Depth-First Search (DFS) traversal visualization
 * 4. Dijkstra shortest paths
 * 5. Diameter and radius computation with eccentricity heatmap
 * 6. BFS and DFS spanning trees
 */

public class App {

    public static void main(String[] args) {
        // Enable graphical mode (GraphStream uses AWT/Swing)
        System.setProperty("java.awt.headless", "false");
        System.setProperty("org.graphstream.ui", "swing");


        //  Exercise 1
        System.out.println("\n--- Exercise 1 on firstgraphlab2.dgs ---");
        Graph g0 = Tools.readGraph("dgs/firstgraphlab2.dgs");
        g0.setAttribute("ui.title", "Exercise 1");
        Viewer v0 = g0.display(true);
        v0.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);

        // Compute and print the average degree of the graph
        double avgDeg = Tools.averageDegree(g0);
        System.out.printf("[g0] Average degree = %.2f%n", avgDeg);

        // Apply visual style and highlight nodes with large neighbor cost sums
        g0.setAttribute("ui.stylesheet", """
            node { size: 8px; fill-color: #aab; text-size: 10; }
            edge { size: 1px; fill-color: #bbb; }
        """);
        int THRESHOLD = 30;
        int marked = Tools.styleByNeighborCostSum(g0, THRESHOLD);
        System.out.println("[g0] Nodes marked > threshold = " + marked);

        //wait 4s
        try {
            Thread.sleep(4000); // 4 sekundy
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Exercise 2
        System.out.println("\n--- Exercise 2 ---");
        //using gridvonneumann_30.dgs instead of completegrid_10.gds - couldn't find this file
        Graph g1 = Tools.readGraph("dgs/gridvonneumann_30.dgs");

        g1.setAttribute("ui.title", "Exercise 2");
        Viewer v1 = g1.display();
        v1.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        v1.disableAutoLayout(); // grid graph already contains coordinates

        // Choose center node as BFS starting point
        Node start = Tools.pickCenterNode(g1);
        System.out.println("[g1] start node = " + start.getId() +
                ", xy=" + java.util.Arrays.toString(Tools.getXY(start)));

        start.setAttribute("ui.class", "start");

        // Visual style for traversal evolution
        g1.setAttribute("ui.stylesheet", """
            node { size: 4px; fill-color: #000; }
            node.start { size: 7px; fill-color: #00aa00; stroke-mode: plain; stroke-color: #0a0; stroke-width: 2px; }
            node.queued  { size: 5px; fill-color: #333; }
            node.visited { size: 4px; fill-color: #000; }
            edge { size: 1px; fill-color: #f00; }
            edge.frontier { size: 2px; fill-color: #f90; }
            edge.tree     { size: 2px; fill-color: #f00; }
        """);

        // Perform BFS with visualization delay (15 ms per step)
        TraversalAlgorithms.bfsEvolution(g1, start, 15);

        // Exercise 3
        System.out.println("\n--- Exercise 3 on gridvaluated_30_120.dgs ---");
        Graph g2a = Tools.readGraph("dgs/gridvaluated_30_120.dgs");
        g2a.setAttribute("ui.title", "Exercise 3");

        Viewer v2a = g2a.display();
        for (Node n : g2a) n.removeAttribute("ui.label");

        v2a.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        v2a.disableAutoLayout(); // grid coordinates already stored in DGS
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

        //wait 4s
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*
        System.out.println("\n--- Exercise 3 on randomgnp_50_0.05.dgs ---");
        Graph g2b = Tools.readGraph("dgs/randomgnp_50_0.05.dgs");
        //Graph g2b = Tools.readGraph("dgs/gridvaluated_30_120.dgs"); //for testing only
        g2b.setAttribute("ui.title", "Exercise 3 (random G(n,p))");

        for (Node n : g2b) n.removeAttribute("ui.label");

        Viewer v2b = g2b.display();
        v2b.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);

        // KLUCZOWE: zapewnij współrzędne siatkowe
        Tools.ensureGridLayout(g2b, 1.0);

        // teraz dopiero blokujemy automatyczny layout
        v2b.disableAutoLayout();

        g2b.setAttribute("ui.stylesheet", """
            node { size: 5px; fill-color: #ff4d4d; text-size: 0; }
            node.stack { size: 6px; fill-color: #ffa500; }
            node.visited { size: 5px; fill-color: #000000; }
            node.backtracked { size: 5px; fill-color: #666666; }
            edge { size: 1px; fill-color: #ff0000; }
            edge.stackEdge { size: 2px; fill-color: #ff9900; }
            edge.tree { size: 2px; fill-color: #ff0000; }
        """);

        Node startB = Tools.pickNodeInLargestComponent(g2b);
        TraversalAlgorithms.dfsEvolution(g2b, startB, 5);


        //wait 4s
        try {
            Thread.sleep(4000); // 4 sekundy
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        // === Exercise 4
        System.out.println("\n--- Exercise 4  ---");

        // Common CSS for Dijkstra visualization
        String dijkstraCss = """
            graph { padding: 30px; }
            node {
            size: 6px;
            shape: box;
            fill-color: #000;
            text-size: 14;
            text-alignment: at-right;
            text-offset: 6px, 0px;           
        }
        node.source {
            size: 10px;
            shape: box;
            fill-color: #2ecc71;       
            stroke-mode: plain;
            stroke-color: #2ecc71;
            stroke-width: 2px;
            text-size: 16;
        }
        edge {
            size: 2px;
            fill-color: #ff0000;           
            text-size: 12;                  
            text-alignment: along;
        }
        """;

        // ---------- (A) gridvaluated_10_220.dgs  ----------
        Graph g4a = Tools.readGraph("dgs/gridvaluated_10_220.dgs");
        g4a.setAttribute("ui.title", "Exercise 4 – Dijkstra (2:20) on 10x10");
        var v4a = g4a.display(false);
        v4a.setCloseFramePolicy(org.graphstream.ui.view.Viewer.CloseFramePolicy.HIDE_ONLY);
        g4a.setAttribute("ui.stylesheet", dijkstraCss);

        for (var n : g4a) n.removeAttribute("ui.label");

        // Display edge weights as labels
        for (var e : g4a.edges().toList())
            e.setAttribute("ui.label", String.format("%.0f", Tools.weight(e)));

        Node sA = Tools.pickCenterNode(g4a);
        sA.setAttribute("ui.class", "source");
        sA.setAttribute("ui.label", "Source");

        // Run custom Dijkstra algorithm and label each node with its distance
        TraversalAlgorithms.dijkstra(g4a, sA);
        for (var n : g4a) {
            double d = n.getNumber("dist");
            if (Double.isFinite(d))
                n.setAttribute("ui.label", "D:" + (int)Math.round(d));
        }

        //wait 4s
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // ---------- (B) gridvaluated_10_12.dgs : ”(1:2)” ----------
        Graph g4b = Tools.readGraph("dgs/gridvaluated_10_12.dgs");
        g4b.setAttribute("ui.title", "Exercise 4 – Dijkstra (1:2) on 10x10");
        var v4b = g4b.display(false);
        v4b.setCloseFramePolicy(org.graphstream.ui.view.Viewer.CloseFramePolicy.HIDE_ONLY);
        g4b.setAttribute("ui.stylesheet", dijkstraCss);

        for (var n : g4b) n.removeAttribute("ui.label");
        for (var e : g4b.edges().toList())
            e.setAttribute("ui.label", String.format("%.0f", Tools.weight(e)));

        Node sB = Tools.pickCenterNode(g4b);
        sB.setAttribute("ui.class", "source");
        sB.setAttribute("ui.label", "Source");

        TraversalAlgorithms.dijkstra(g4b, sB);
        for (var n : g4b) {
            double d = n.getNumber("dist");
            if (Double.isFinite(d))
                n.setAttribute("ui.label", "D:" + (int)Math.round(d));
        }

        //wait 4s
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // --- Exercise 5 : Diameter & Radius
        Graph g5 = Tools.readGraph("dgs/gridvaluated_30_120.dgs");
        var v5 = g5.display(false);
        v5.setCloseFramePolicy(org.graphstream.ui.view.Viewer.CloseFramePolicy.HIDE_ONLY);

        // Compute eccentricity for all nodes and derive diameter & radius
        TraversalAlgorithms.DR dr = TraversalAlgorithms.computeEccentricities(g5);
        System.out.printf("[Ex5] diameter=%.2f, radius=%.2f%n", dr.diameter, dr.radius);

        // Prepare heatmap visualization
        for (var n : g5) { n.removeAttribute("ui.label"); n.removeAttribute("ui.style"); n.removeAttribute("ui.class"); }

        g5.setAttribute("ui.stylesheet", """
            graph { padding: 20px; }
            node {
                shape: box;
                size: 20px;
                stroke-mode: none;
                text-size: 0;
            }
            edge { size: 0px; }
        """);

        TraversalAlgorithms.applyEccentricityHeatmap(g5);

        //wait 4s
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Exercise 6
        System.out.println("\n--- Exercise 6 : Spanning Trees (BFS vs DFS) ---");
        String treeCss = """
            graph { padding: 30px; }
            node { size: 4px; fill-color: #333; }
            edge { size: 1px; fill-color: #ff6666; }
            edge.tree { size: 4px; fill-color: #000000; }
        """;

        // (A) BFS tree
        Graph g6b = Tools.readGraph("dgs/gridvaluated_30_120.dgs");
        var v6b = g6b.display(false);
        v6b.setCloseFramePolicy(org.graphstream.ui.view.Viewer.CloseFramePolicy.HIDE_ONLY);
        g6b.setAttribute("ui.title", "Exercise 6 — BFS spanning tree");
        g6b.setAttribute("ui.stylesheet", treeCss);
        for (var n : g6b) n.removeAttribute("ui.label");
        Node sBfs = Tools.pickCenterNode(g6b);
        TraversalAlgorithms.bfsTree(g6b, sBfs);
        Tools.highlightSPTree(g6b, "tree");

        //wait 4s
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // (B) DFS tree
        Graph g6d = Tools.readGraph("dgs/gridvaluated_30_120.dgs");
        var v6d = g6d.display(false);
        v6d.setCloseFramePolicy(org.graphstream.ui.view.Viewer.CloseFramePolicy.HIDE_ONLY);
        g6d.setAttribute("ui.title", "Exercise 6 — DFS spanning tree");
        g6d.setAttribute("ui.stylesheet", treeCss);
        for (var n : g6d) n.removeAttribute("ui.label");
        Node sDfs = Tools.pickCenterNode(g6d);
        TraversalAlgorithms.dfsTree(g6d, sDfs);
        Tools.highlightSPTree(g6d, "tree");

        //wait 4s
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
