import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

// Class to represent a Node
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

// Class to represent an Edge with a weight
class Edge {
    Node u, v;
    double weight;
    int index; // Index of the edge for drawing offset when multiple edges exist between nodes

    public Edge(Node u, Node v, double weight, int index) {
        this.u = u;
        this.v = v;
        this.weight = weight;
        this.index = index;
    }
}

// Main class to read nodes and edges, and visualize the graph
public class GraphVisualizer extends JPanel {
    private List<Node> nodes;
    private List<Edge> edges;

    public GraphVisualizer() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        getUserInput();  // Get input from the user
    }

    // Get input from the user for the graph details
    private void getUserInput() {
        int numNodes = Integer.parseInt(JOptionPane.showInputDialog("Enter the number of nodes:"));
        int numEdges = Integer.parseInt(JOptionPane.showInputDialog("Enter the number of edges:"));

        // Take input for each node (id, x, y, name)
        for (int i = 0; i < numNodes; i++) {
            int x = Integer.parseInt(JOptionPane.showInputDialog("Enter x-coordinate for Node " + (i + 1) + ":"));
            int y = Integer.parseInt(JOptionPane.showInputDialog("Enter y-coordinate for Node " + (i + 1) + ":"));
            String name = JOptionPane.showInputDialog("Enter name for Node " + (i + 1) + ":");
            nodes.add(new Node(i + 1, x, y, name));
        }

        // Dictionary to count multiple edges between the same nodes
        int[][] edgeCount = new int[numNodes + 1][numNodes + 1];

        // Take input for each edge (uId, vId, weight)
        for (int i = 0; i < numEdges; i++) {
            int uId = Integer.parseInt(JOptionPane.showInputDialog("Enter start node ID for Edge " + (i + 1) + ":"));
            int vId = Integer.parseInt(JOptionPane.showInputDialog("Enter end node ID for Edge " + (i + 1) + ":"));
            double weight = Double.parseDouble(JOptionPane.showInputDialog("Enter weight for Edge " + (i + 1) + ":"));
            
            Node u = findNodeById(uId);
            Node v = findNodeById(vId);
            if (u != null && v != null) {
                edgeCount[uId][vId]++;
                int index = edgeCount[uId][vId]; // Track the index of the edge between the same nodes
                edges.add(new Edge(u, v, weight, index));
            }
        }

        // Repaint the panel to visualize the new graph
        repaint();
    }

    // Find a node by its ID
    private Node findNodeById(int id) {
        for (Node node : nodes) {
            if (node.id == id) {
                return node;
            }
        }
        return null;
    }

    // Override paintComponent to draw the graph
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw edges
        for (Edge edge : edges) {
            // If multiple edges between same nodes, apply offset (curvature)
            int offset = 10 * edge.index;
            drawCurvedEdge(g2, edge.u.x, edge.u.y, edge.v.x, edge.v.y, offset, edge.weight);
        }

        // Draw nodes
        for (Node node : nodes) {
            g2.setColor(Color.RED);
            g2.fillOval(node.x - 10, node.y - 10, 20, 20); // Draw circle for node

            // Draw node name above the node
            g2.setColor(Color.BLACK);
            g2.drawString(node.name, node.x - 10, node.y - 20);
        }
    }

    // Draw a curved edge with an optional offset for multiple edges
    private void drawCurvedEdge(Graphics2D g2, int x1, int y1, int x2, int y2, int offset, double weight) {
        int midX = (x1 + x2) / 2 + offset;
        int midY = (y1 + y2) / 2 - offset;

        // Draw a quadratic curve
        g2.setColor(Color.BLACK);
        g2.draw(new java.awt.geom.QuadCurve2D.Float(x1, y1, midX, midY, x2, y2));

        // Draw weight near the curve's midpoint
        g2.setColor(Color.BLUE);
        g2.drawString(String.format("%.2f", weight), midX, midY);
    }

    // Main method to set up the UI
    public static void main(String[] args) {
        JFrame frame = new JFrame("Graph Visualizer");
        GraphVisualizer graphVisualizer = new GraphVisualizer();
        frame.add(graphVisualizer);
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
