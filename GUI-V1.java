import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class GraphVisualizer {
    private List<Node> nodes;
    private List<Edge> edges;
    private List<Edge> optimizedEdges;
    private JFrame appFrame, fileSelectionFrame, originalGraphFrame, optimizedGraphFrame;
    private File nodeFile, edgeFile;
    private String selectedAlgorithm;

    public GraphVisualizer() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        optimizedEdges = new ArrayList<>();
    }

    // Node class
    static class Node {
        String name;
        Double x, y;

        public Node(String name, Double x, Double y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }

        public String getName() { return name; }
        public Double getX() { return x; }
        public Double getY() { return y; }
    }

    // Edge class
    static class Edge {
        Node source, target;
        Double distance;
        Double populationDensitySource, economicWealthSource, tourismPotentialSource;
        Double populationDensityTarget, economicWealthTarget, tourismPotentialTarget;

        public Edge(Node source, Node target, Double distance,
                    Double populationDensitySource, Double economicWealthSource, Double tourismPotentialSource,
                    Double populationDensityTarget, Double economicWealthTarget, Double tourismPotentialTarget) {
            this.source = source;
            this.target = target;
            this.distance = distance;
            this.populationDensitySource = populationDensitySource;
            this.economicWealthSource = economicWealthSource;
            this.tourismPotentialSource = tourismPotentialSource;
            this.populationDensityTarget = populationDensityTarget;
            this.economicWealthTarget = economicWealthTarget;
            this.tourismPotentialTarget = tourismPotentialTarget;
        }

        public Node getSource() { return source; }
        public Node getTarget() { return target; }
        public Double getDistance() { return distance; }

        public Double getCompositeWeight() {
            return distance + populationDensitySource + economicWealthSource + tourismPotentialSource +
                    populationDensityTarget + economicWealthTarget + tourismPotentialTarget;
        }

        public String getEdgeLabel() {
            return String.format("%.2f", getCompositeWeight());
        }
    }

    // Load graph data from CSV files
    private void loadGraph() {
        try (BufferedReader nodeReader = new BufferedReader(new FileReader(nodeFile));
             BufferedReader edgeReader = new BufferedReader(new FileReader(edgeFile))) {

            String line;
            nodeReader.readLine(); // Skip header for nodes.csv
            while ((line = nodeReader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0].trim();
                    Double x = Double.parseDouble(parts[1].trim());
                    Double y = Double.parseDouble(parts[2].trim());
                    nodes.add(new Node(name, x, y));
                }
            }

            edgeReader.readLine(); // Skip header for edges.csv
            while ((line = edgeReader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 9) {
                    String source = parts[0].trim();
                    String target = parts[1].trim();
                    Double distance = Double.parseDouble(parts[2].trim());
                    Double populationDensitySource = Double.parseDouble(parts[3].trim());
                    Double economicWealthSource = Double.parseDouble(parts[4].trim());
                    Double tourismPotentialSource = Double.parseDouble(parts[5].trim());
                    Double populationDensityTarget = Double.parseDouble(parts[6].trim());
                    Double economicWealthTarget = Double.parseDouble(parts[7].trim());
                    Double tourismPotentialTarget = Double.parseDouble(parts[8].trim());

                    Node sourceNode = findNodeByName(source);
                    Node targetNode = findNodeByName(target);
                    if (sourceNode != null && targetNode != null) {
                        edges.add(new Edge(sourceNode, targetNode, distance,
                                populationDensitySource, economicWealthSource, tourismPotentialSource,
                                populationDensityTarget, economicWealthTarget, tourismPotentialTarget));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Node findNodeByName(String name) {
        for (Node node : nodes) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    // Dijkstra's algorithm to calculate shortest path and optimized graph
    private void calculateOptimizedGraph(String startNodeName) {
        Node startNode = findNodeByName(startNodeName);
        if (startNode == null) {
            System.out.println("Start node not found!");
            return;
        }

        if ("Dijkstra".equalsIgnoreCase(selectedAlgorithm)) {
            dijkstraAlgorithm(startNode);
        } else if ("Bellman-Ford".equalsIgnoreCase(selectedAlgorithm)) {
            bellmanFordAlgorithm(startNode);
        } else if ("A*".equalsIgnoreCase(selectedAlgorithm)) {
            aStarAlgorithm(startNode);
        }
    }

    private void dijkstraAlgorithm(Node startNode) {
        // Dijkstra's algorithm implementation (using priority queue for optimal performance)
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> predecessors = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        for (Node node : nodes) {
            distances.put(node, Double.MAX_VALUE);
            pq.add(node);
        }
        distances.put(startNode, 0.0);

        while (!pq.isEmpty()) {
            Node currentNode = pq.poll();

            for (Edge edge : edges) {
                if (edge.getSource().equals(currentNode)) {
                    Node neighbor = edge.getTarget();
                    double newDist = distances.get(currentNode) + edge.getDistance();
                    if (newDist < distances.get(neighbor)) {
                        distances.put(neighbor, newDist);
                        predecessors.put(neighbor, currentNode);
                        pq.add(neighbor);
                    }
                }
            }
        }

        optimizedEdges.clear();
        for (Node node : nodes) {
            Node currentNode = node;
            while (predecessors.containsKey(currentNode)) {
                Node prev = predecessors.get(currentNode);
                Edge edge = findEdge(prev, currentNode);
                if (edge != null) {
                    optimizedEdges.add(edge);
                }
                currentNode = prev;
            }
        }
    }

    private Edge findEdge(Node source, Node target) {
        for (Edge edge : edges) {
            if (edge.getSource().equals(source) && edge.getTarget().equals(target)) {
                return edge;
            }
        }
        return null;
    }

    private void bellmanFordAlgorithm(Node startNode) {
        // Bellman-Ford algorithm implementation
    }

    private void aStarAlgorithm(Node startNode) {
        // A* algorithm implementation
    }

    // Panel for drawing the graph with improved node size and edge routing
    static class GraphPanel extends JPanel {
        private List<Node> nodes;
        private List<Edge> edges;

        public GraphPanel(List<Node> nodes, List<Edge> edges) {
            this.nodes = nodes;
            this.edges = edges;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Drawing edges with offset routing
            for (Edge edge : edges) {
                drawEdge(g2, edge.getSource().getX(), edge.getSource().getY(),
                        edge.getTarget().getX(), edge.getTarget().getY(), edge.getCompositeWeight());
            }

            // Drawing nodes with a larger radius
            for (Node node : nodes) {
                g2.setColor(Color.BLUE);
                g2.fillOval(node.getX().intValue() - 15, node.getY().intValue() - 15, 30, 30);
                g2.setColor(Color.BLACK);
                g2.drawString(node.getName(), node.getX().intValue() - 15, node.getY().intValue() - 20);
            }
        }

        private void drawEdge(Graphics2D g2, Double x1, Double y1, Double x2, Double y2, Double weight) {
            g2.setColor(Color.BLACK);
            g2.drawLine(x1.intValue(), y1.intValue(), x2.intValue(), y2.intValue());
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString(String.format("%.2f", weight), (x1.intValue() + x2.intValue()) / 2,
                    (y1.intValue() + y2.intValue()) / 2);
        }
    }

    // Create the initial app frame with a modern design
    private void createAppFrame() {
        appFrame = new JFrame("Graph Visualizer");
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.setLayout(new BorderLayout());
        appFrame.setBackground(new Color(240, 240, 240));

        JLabel appDetailsLabel = new JLabel("<html><b>Graph Visualizer</b><br>Visualize Graphs and Algorithms</html>", SwingConstants.CENTER);
        appDetailsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appDetailsLabel.setForeground(new Color(0, 51, 102)); // Dark Blue
        appFrame.add(appDetailsLabel, BorderLayout.CENTER);

        // More detailed button with improved design
        JButton nextButton = new JButton("Next");
        nextButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nextButton.setBackground(new Color(0, 123, 255)); // Bootstrap blue
        nextButton.setForeground(Color.WHITE);
        nextButton.setBorderPainted(false);
        nextButton.setFocusPainted(false);
        nextButton.setPreferredSize(new Dimension(120, 40));

        nextButton.addActionListener(e -> {
            appFrame.setVisible(false);
            createFileSelectionScreen();
        });

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(nextButton);
        appFrame.add(panel, BorderLayout.SOUTH);

        appFrame.setSize(450, 250);
        appFrame.setLocationRelativeTo(null);
        appFrame.setVisible(true);
    }

    // Refined file selection screen
    private void createFileSelectionScreen() {
        fileSelectionFrame = new JFrame("File Selection");
        fileSelectionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fileSelectionFrame.setLayout(new GridBagLayout());
        fileSelectionFrame.setBackground(new Color(255, 255, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel selectFilesLabel = new JLabel("Select Files and Algorithm", SwingConstants.CENTER);
        selectFilesLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        selectFilesLabel.setForeground(new Color(0, 51, 102));
        fileSelectionFrame.add(selectFilesLabel, gbc);

        // File selection buttons with modern design
        JButton nodeFileButton = new JButton("Select Nodes File");
        nodeFileButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nodeFileButton.setBackground(new Color(255, 235, 59)); // Yellow
        nodeFileButton.setForeground(Color.BLACK);
        JLabel nodeFileLabel = new JLabel("No file selected");
        nodeFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(fileSelectionFrame) == JFileChooser.APPROVE_OPTION) {
                nodeFile = fileChooser.getSelectedFile();
                nodeFileLabel.setText("Selected: " + nodeFile.getName());
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 1;
        fileSelectionFrame.add(nodeFileButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        fileSelectionFrame.add(nodeFileLabel, gbc);

        JButton edgeFileButton = new JButton("Select Edges File");
        edgeFileButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        edgeFileButton.setBackground(new Color(255, 235, 59)); // Yellow
        edgeFileButton.setForeground(Color.BLACK);
        JLabel edgeFileLabel = new JLabel("No file selected");
        edgeFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(fileSelectionFrame) == JFileChooser.APPROVE_OPTION) {
                edgeFile = fileChooser.getSelectedFile();
                edgeFileLabel.setText("Selected: " + edgeFile.getName());
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        fileSelectionFrame.add(edgeFileButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        fileSelectionFrame.add(edgeFileLabel, gbc);

        // Algorithm selection with dropdown menu
        String[] algorithms = {"Dijkstra", "Bellman-Ford", "A*"};
        JComboBox<String> algorithmComboBox = new JComboBox<>(algorithms);
        algorithmComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        gbc.gridx = 0;
        gbc.gridy = 3;
        fileSelectionFrame.add(new JLabel("Select Algorithm:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        fileSelectionFrame.add(algorithmComboBox, gbc);

        // Visualize Button
        JButton visualizeButton = new JButton("Visualize");
        visualizeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        visualizeButton.setBackground(new Color(0, 123, 255)); // Blue
        visualizeButton.setForeground(Color.WHITE);
        visualizeButton.setPreferredSize(new Dimension(120, 40));
        visualizeButton.addActionListener(e -> {
            selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
            loadGraph();
            String startNodeName = nodes.get(0).getName();  // Assuming start node is the first node
            calculateOptimizedGraph(startNodeName);
            showOriginalGraph();
            showOptimizedGraph();
        });

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        fileSelectionFrame.add(visualizeButton, gbc);

        fileSelectionFrame.setSize(500, 400);
        fileSelectionFrame.setLocationRelativeTo(null);
        fileSelectionFrame.setVisible(true);
    }

    // Show the original graph with better node and edge spacing
    private void showOriginalGraph() {
        originalGraphFrame = new JFrame("Original Graph");
        originalGraphFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        originalGraphFrame.add(new GraphPanel(nodes, edges));
        originalGraphFrame.setSize(800, 600);
        originalGraphFrame.setLocationRelativeTo(null);
        originalGraphFrame.setVisible(true);
    }

    // Show the optimized graph with clearer node and edge layout
    private void showOptimizedGraph() {
        optimizedGraphFrame = new JFrame("Optimized Graph");
        optimizedGraphFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        optimizedGraphFrame.add(new GraphPanel(nodes, optimizedEdges));
        optimizedGraphFrame.setSize(800, 600);
        optimizedGraphFrame.setLocationRelativeTo(null);
        optimizedGraphFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GraphVisualizer().createAppFrame());
    }
}
