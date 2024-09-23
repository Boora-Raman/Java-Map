import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;

class Node {
    int id;
    int x, y; // Coordinates
    String name; // Custom name for the node

    public Node(int id, int x, int y, String name) {
        this.id = id;
        this.x = x;
        this.y = y;
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
    private java.util.List<Node> nodes;
    private java.util.List<Edge> edges;

    public GraphVisualizer() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        loadGraph("graph.txt");
        repaint();
    }

    private void loadGraph(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null && !line.trim().isEmpty()) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    int id = Integer.parseInt(parts[0].trim());
                    int x = Integer.parseInt(parts[1].trim());
                    int y = Integer.parseInt(parts[2].trim());
                    String name = parts[3].trim();
                    nodes.add(new Node(id, x, y, name));
                } else if (parts.length == 3) {
                    int uId = Integer.parseInt(parts[0].trim());
                    int vId = Integer.parseInt(parts[1].trim());
                    double weight = Double.parseDouble(parts[2].trim());
                    edges.add(new Edge(findNodeById(uId), findNodeById(vId), weight));
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
            g2.fillOval(node.x - 10, node.y - 10, 20, 20);
            g2.setColor(Color.BLACK);
            g2.drawString(node.name, node.x - 10, node.y - 20);
        }
    }

    private void drawEdge(Graphics2D g2, int x1, int y1, int x2, int y2, double weight) {
        g2.setColor(Color.BLACK);
        g2.drawLine(x1, y1, x2, y2);
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        g2.setColor(Color.BLUE);
        g2.drawString(String.format("%.2f", weight), midX, midY);
    }

    public java.util.List<Node> getNodes() {
        return nodes;
    }

    public java.util.List<Edge> getEdges() {
        return edges;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Graph Visualizer");
        GraphVisualizer graphVisualizer = new GraphVisualizer();
        frame.add(graphVisualizer);
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        // Run Dijkstra's Algorithm and visualize optimized graph
        java.util.List<Edge> optimizedEdges = graphVisualizer.calculateOptimizedGraph(1); // Start from node 'a' (id 1)
        graphVisualizer.saveOptimizedGraph(optimizedEdges);
        
        OptimizedGraphVisualizer optimizedGraphVisualizer = new OptimizedGraphVisualizer(optimizedEdges, graphVisualizer.getNodes());
        JFrame optimizedFrame = new JFrame("Optimized Graph Visualizer");
        optimizedFrame.add(optimizedGraphVisualizer);
        optimizedFrame.setSize(800, 800);
        optimizedFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        optimizedFrame.setVisible(true);
    }

    // Calculate optimized graph using Dijkstra's algorithm
    public java.util.List<Edge> calculateOptimizedGraph(int startNodeId) {
        java.util.List<Edge> optimizedEdges = new ArrayList<>();
        Map<Node, java.util.List<Edge>> adjacencyList = new HashMap<>();

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

        // Create optimized edges based on shortest path tree
        for (Node node : nodes) {
            if (previous.get(node) != null) {
                optimizedEdges.add(new Edge(previous.get(node), node, distances.get(node)));
            }
        }

        return optimizedEdges;
    }

    // Save optimized graph to a file
    public void saveOptimizedGraph(java.util.List<Edge> optimizedEdges) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("optimized_graph.txt"))) {
            for (Edge edge : optimizedEdges) {
                bw.write(edge.u.id + "," + edge.v.id + "," + edge.weight);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class OptimizedGraphVisualizer extends JPanel {
    private java.util.List<Node> nodes;
    private java.util.List<Edge> edges;

    public OptimizedGraphVisualizer(java.util.List<Edge> edges, java.util.List<Node> nodes) {
        this.edges = edges;
        this.nodes = nodes;
        repaint();
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
            g2.fillOval(node.x - 10, node.y - 10, 20, 20);
            g2.setColor(Color.BLACK);
            g2.drawString(node.name, node.x - 10, node.y - 20);
        }
    }

    private void drawEdge(Graphics2D g2, int x1, int y1, int x2, int y2, double weight) {
        g2.setColor(Color.BLACK);
        g2.drawLine(x1, y1, x2, y2);
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        g2.setColor(Color.BLUE);
        g2.drawString(String.format("%.2f", weight), midX, midY);
    }
}

