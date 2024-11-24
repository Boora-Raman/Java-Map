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
        
        // Composite weight used for algorithms
        public Double getCompositeWeight() {
            return distance + populationDensitySource + economicWealthSource + tourismPotentialSource +
                    populationDensityTarget + economicWealthTarget + tourismPotentialTarget;
        }

        // To show the weight of the edge in the visualization
        public String getEdgeLabel() {
            return String.format("%.2f", getCompositeWeight());
        }
    }

    // Load graph data from CSV files
    private void loadGraph(String nodesFile, String edgesFile) {
        try (BufferedReader nodeReader = new BufferedReader(new FileReader(nodesFile));
             BufferedReader edgeReader = new BufferedReader(new FileReader(edgesFile))) {

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
    private void calculateOptimizedGraph(String startNodeName, String algorithm) {
        Node startNode = findNodeByName("Delhi");
        if (startNode == null) {
            System.out.println("Start node not found!");
            return;
        }

        // You can implement other algorithms like Bellman-Ford or A* here
        // For now, Dijkstra's is implemented
        if ("Dijkstra".equalsIgnoreCase(algorithm)) {
            dijkstraAlgorithm(startNode);
        }
    }

    private void dijkstraAlgorithm(Node startNode) {
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> previousNodes = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparing(distances::get));

        for (Node node : nodes) {
            distances.put(node, Double.MAX_VALUE); // Set initial distance to infinity
            previousNodes.put(node, null);
        }

        distances.put(startNode, 0.0);
        pq.add(startNode);

        while (!pq.isEmpty()) {
            Node currentNode = pq.poll();

            for (Edge edge : edges) {
                if (edge.getSource().equals(currentNode)) {
                    Node neighbor = edge.getTarget();
                    double newDist = distances.get(currentNode) + edge.getCompositeWeight();

                    if (newDist < distances.get(neighbor)) {
                        distances.put(neighbor, newDist);
                        previousNodes.put(neighbor, currentNode);
                        pq.add(neighbor);
                    }
                }
            }
        }

        // Reconstruct the optimized graph based on shortest paths
        optimizedEdges.clear(); // Clear the list before adding new edges

        // Go through the previousNodes map to add the shortest path edges to optimizedEdges
        for (Node node : nodes) {
            Node current = node;
            while (previousNodes.get(current) != null) {
                Node previous = previousNodes.get(current);
                // Create an optimized edge with correct composite weight
                optimizedEdges.add(new Edge(previous, current, current.getX() - previous.getX(),
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0)); // Using dummy distances for now
                current = previous;
            }
        }
    }

    // Panel for drawing the original graph
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

            // Draw original graph edges
            for (Edge edge : edges) {
                drawEdge(g2, edge.getSource().getX(), edge.getSource().getY(),
                        edge.getTarget().getX(), edge.getTarget().getY(), edge.getCompositeWeight());
            }

            // Draw nodes
            for (Node node : nodes) {
                g2.setColor(Color.BLUE);
                g2.fillOval(node.getX().intValue() - 10, node.getY().intValue() - 10, 20, 20);
                g2.setColor(Color.BLACK);
                g2.drawString(node.getName(), node.getX().intValue() - 10, node.getY().intValue() - 20);
            }
        }

        // Draw an edge with a label indicating composite weight
        private void drawEdge(Graphics2D g2, Double x1, Double y1, Double x2, Double y2, Double weight) {
            g2.setColor(Color.BLACK);
            g2.drawLine(x1.intValue(), y1.intValue(), x2.intValue(), y2.intValue());
            int midX = (x1.intValue() + x2.intValue()) / 2;
            int midY = (y1.intValue() + y2.intValue()) / 2;
            g2.drawString(String.format("%.2f", weight), midX, midY);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GraphVisualizer graphVisualizer = new GraphVisualizer();
            new WelcomeFrame(graphVisualizer);
        });
    }

    static class WelcomeFrame {
        public WelcomeFrame(GraphVisualizer graphVisualizer) {
            JFrame welcomeFrame = new JFrame("Welcome to Graph Visualizer");
            welcomeFrame.setLayout(new BorderLayout());
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(2, 1));

            JLabel titleLabel = new JLabel("Graph Optimizer and Visualizer", JLabel.CENTER);
            panel.add(titleLabel);

            JButton nextButton = new JButton("Next");
            nextButton.addActionListener(e -> new FileSelectionFrame(graphVisualizer));
            panel.add(nextButton);

            welcomeFrame.add(panel, BorderLayout.CENTER);
            welcomeFrame.setSize(400, 200);
            welcomeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            welcomeFrame.setVisible(true);
        }
    }

    static class FileSelectionFrame {
        public FileSelectionFrame(GraphVisualizer graphVisualizer) {
            JFrame fileFrame = new JFrame("Select Files & Algorithm");
            fileFrame.setLayout(new BorderLayout());

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(4, 1));

            // File selection for nodes
            JLabel label1 = new JLabel("Select Nodes CSV File");
            panel.add(label1);
            JButton loadNodesButton = new JButton("Load Nodes");
            JTextField nodesFilePath = new JTextField();
            loadNodesButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    nodesFilePath.setText(selectedFile.getAbsolutePath());
                }
            });
            panel.add(loadNodesButton);
            panel.add(nodesFilePath);

            // File selection for edges
            JLabel label2 = new JLabel("Select Edges CSV File");
            panel.add(label2);
            JButton loadEdgesButton = new JButton("Load Edges");
            JTextField edgesFilePath = new JTextField();
            loadEdgesButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    edgesFilePath.setText(selectedFile.getAbsolutePath());
                }
            });
            panel.add(loadEdgesButton);
            panel.add(edgesFilePath);

            // Algorithm selection
            JLabel algorithmLabel = new JLabel("Select Algorithm:");
            panel.add(algorithmLabel);
            JComboBox<String> algorithmComboBox = new JComboBox<>(new String[]{"Dijkstra", "Bellman-Ford", "A*"});
            panel.add(algorithmComboBox);

            // Visualize button
            JButton visualizeButton = new JButton("Visualize");
            visualizeButton.addActionListener(e -> {
                String nodesFile = nodesFilePath.getText();
                String edgesFile = edgesFilePath.getText();
                String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();

                graphVisualizer.loadGraph(nodesFile, edgesFile);
                graphVisualizer.calculateOptimizedGraph(nodesFile, selectedAlgorithm);

                // Step 3: Visualization
                JFrame originalFrame = new JFrame("Original Graph");
                originalFrame.add(new GraphPanel(graphVisualizer.nodes, graphVisualizer.edges));
                originalFrame.setSize(600, 600);
                originalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                originalFrame.setVisible(true);

                JFrame optimizedFrame = new JFrame("Optimized Graph");
                optimizedFrame.add(new GraphPanel(graphVisualizer.nodes, graphVisualizer.optimizedEdges));
                optimizedFrame.setSize(600, 600);
                optimizedFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                optimizedFrame.setVisible(true);
            });
            panel.add(visualizeButton);

            fileFrame.add(panel, BorderLayout.CENTER);
            fileFrame.setSize(400, 300);
            fileFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            fileFrame.setVisible(true);
        }
    }
}

