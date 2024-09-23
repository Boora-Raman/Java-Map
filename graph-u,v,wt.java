import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List; // Explicitly import java.util.List
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator; // Add this import for Comparator

class Node {
    int id;
    String name; // Custom name for the node
    int x, y; // Coordinates for rendering

    public Node(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

class Edge {
    Node u, v;
    double weight;

    public Edge(Node u, Node v, double weight) {
        this.u = u;
        this.v = v;
        this.weight = weight;
    }
}

public class GraphVisualizer extends JPanel {
    private List<Node> nodes; // Use java.util.List
    private List<Edge> edges; // Use java.util.List
    private final int NODE_RADIUS = 30;

    public GraphVisualizer() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        loadGraph("graph.txt");
        positionNodes();
        repaint();
    }

    private void loadGraph(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null && !line.trim().isEmpty()) {
                String[] parts = line.split(",");
                if (parts.length == 3) { // Edge format: u, v, weight
                    int uId = Integer.parseInt(parts[0].trim());
                    int vId = Integer.parseInt(parts[1].trim());
                    double weight = Double.parseDouble(parts[2].trim());
                    edges.add(new Edge(findNodeById(uId), findNodeById(vId), weight));
                } else if (parts.length == 2) { // Node format: id, name
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    nodes.add(new Node(id, name));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Node findNodeById(int id) {
        for (Node node : nodes) {
            if (node.id == id) {
                return node;
            }
        }
        return null;
    }

    private void positionNodes() {
        int startX = 150; // Starting x-coordinate
        int startY = 150; // Starting y-coordinate
        int spacingX = 100; // Horizontal spacing
        int spacingY = 100; // Vertical spacing

        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).x = startX + (i % 3) * spacingX; // Position in a grid
            nodes.get(i).y = startY + (i / 3) * spacingY;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw edges
        for (Edge edge : edges) {
            drawEdge(g2, edge.u.x, edge.u.y, edge.v.x, edge.v.y, edge.weight);
        }

        // Draw nodes
        for (Node node : nodes) {
            g2.setColor(Color.RED);
            g2.fillOval(node.x - NODE_RADIUS / 2, node.y - NODE_RADIUS / 2, NODE_RADIUS, NODE_RADIUS);
            g2.setColor(Color.BLACK);
            g2.drawString(node.name, node.x - NODE_RADIUS / 2, node.y - NODE_RADIUS);
        }
    }

    private void drawEdge(Graphics2D g2, int x1, int y1, int x2, int y2, double weight) {
        g2.setColor(Color.BLACK);
        g2.drawLine(x1, y1, x2, y2);
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;

        // Adjust weight position away from the edge
        int weightOffsetX = 10; // Horizontal offset
        int weightOffsetY = -10; // Vertical offset
        g2.setColor(Color.BLUE);
        g2.drawString(String.format("%.2f", weight), midX + weightOffsetX, midY + weightOffsetY);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List<Edge> calculateOptimizedGraph(int startNodeId) {
        List<Edge> optimizedEdges = new ArrayList<>();
        Map<Node, List<Edge>> adjacencyList = new HashMap<>();

        for (Edge edge : edges) {
            adjacencyList.computeIfAbsent(edge.u, k -> new ArrayList<>()).add(edge);
            adjacencyList.computeIfAbsent(edge.v, k -> new ArrayList<>()).add(new Edge(edge.v, edge.u, edge.weight)); // add reverse edge
        }

        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> previous = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        for (Node node : nodes) {
            distances.put(node, Double.MAX_VALUE);
            previous.put(node, null);
            queue.add(node);
        }

        distances.put(findNodeById(startNodeId), 0.0);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            for (Edge edge : adjacencyList.getOrDefault(current, new ArrayList<>())) {
                double newDist = distances.get(current) + edge.weight;
                if (newDist < distances.get(edge.v)) {
                    distances.put(edge.v, newDist);
                    previous.put(edge.v, current);
                    queue.add(edge.v);
                }
            }
        }

        for (Node node : nodes) {
            if (previous.get(node) != null) {
                optimizedEdges.add(new Edge(previous.get(node), node, distances.get(node)));
            }
        }

        return optimizedEdges;
    }

    public void saveOptimizedGraph(List<Edge> optimizedEdges) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("optimized_graph.txt"))) {
            for (Edge edge : optimizedEdges) {
                bw.write(edge.u.id + "," + edge.v.id + "," + edge.weight);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Graph Visualizer");
        GraphVisualizer graphVisualizer = new GraphVisualizer();
        frame.add(graphVisualizer);
        frame.setSize(800, 600); // Adjusted size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Run Dijkstra's Algorithm and visualize optimized graph
        List<Edge> optimizedEdges = graphVisualizer.calculateOptimizedGraph(1); // Start from node ID 1
        graphVisualizer.saveOptimizedGraph(optimizedEdges);

        OptimizedGraphVisualizer optimizedGraphVisualizer = new OptimizedGraphVisualizer(optimizedEdges, graphVisualizer.getNodes());
        JFrame optimizedFrame = new JFrame("Optimized Graph Visualizer");
        optimizedFrame.add(optimizedGraphVisualizer);
        optimizedFrame.setSize(800, 600); // Adjusted size
        optimizedFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        optimizedFrame.setVisible(true);
    }
}

class OptimizedGraphVisualizer extends JPanel {
    private List<Node> nodes;
    private List<Edge> edges;
    private final int NODE_RADIUS = 30;

    public OptimizedGraphVisualizer(List<Edge> edges, List<Node> nodes) {
        this.edges = edges;
        this.nodes = nodes;
        positionNodes();
        repaint();
    }

    private void positionNodes() {
        int startX = 150; // Starting x-coordinate
        int startY = 150; // Starting y-coordinate
        int spacingX = 100; // Horizontal spacing
        int spacingY = 100; // Vertical spacing

        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).x = startX + (i % 3) * spacingX; // Position in a grid
            nodes.get(i).y = startY + (i / 3) * spacingY;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw edges
        for (Edge edge : edges) {
            drawEdge(g2, edge.u.x, edge.u.y, edge.v.x, edge.v.y, edge.weight);
        }

        // Draw nodes
        for (Node node : nodes) {
            g2.setColor(Color.RED);
            g2.fillOval(node.x - NODE_RADIUS / 2, node.y - NODE_RADIUS / 2, NODE_RADIUS, NODE_RADIUS);
            g2.setColor(Color.BLACK);
            g2.drawString(node.name, node.x - NODE_RADIUS / 2, node.y - NODE_RADIUS);
        }
    }

    private void drawEdge(Graphics2D g2, int x1, int y1, int x2, int y2, double weight) {
        g2.setColor(Color.BLACK);
        g2.drawLine(x1, y1, x2, y2);
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;

        // Adjust weight position away from the edge
        int weightOffsetX = 10; // Horizontal offset
        int weightOffsetY = -10; // Vertical offset
        g2.setColor(Color.BLUE);
        g2.drawString(String.format("%.2f", weight), midX + weightOffsetX, midY + weightOffsetY);
    }
}
