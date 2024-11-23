public class GraphVisualizer {
    private List<Node> nodes;
    private List<Edge> edges;
    private List<Edge> optimizedEdges;

    public GraphVisualizer() {
        nodes = new ArrayList<>(); // Initialize the nodes list
        edges = new ArrayList<>(); // Initialize the edges list
        optimizedEdges = new ArrayList<>(); // Initialize the optimizedEdges list
    }

    // Node class with Double data types
    static class Node {
        String name;
        Double x, y;

        public Node(String name, Double x, Double y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }

        public String getName() {
            return name;
        }

        public Double getX() {
            return x;
        }

        public Double getY() {
            return y;
        }
    }

    // Edge class with Double data types
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

        public Node getSource() {
            return source;
        }

        public Node getTarget() {
            return target;
        }

        public Double getDistance() {
            return distance;
        }

        public Double getCompositeWeight() {
            return distance + populationDensitySource + economicWealthSource + tourismPotentialSource;
        }
    }

    // Load graph data from CSV files
    private void loadGraph(String nodesFile, String edgesFile) {
        try (BufferedReader nodeReader = new BufferedReader(new FileReader(nodesFile));
             BufferedReader edgeReader = new BufferedReader(new FileReader(edgesFile))) {

            String line;

            // Skip the header row for nodes.csv
            nodeReader.readLine();
            while ((line = nodeReader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0].trim();
                    Double x = Double.parseDouble(parts[1].trim());
                    Double y = Double.parseDouble(parts[2].trim());
                    nodes.add(new Node(name, x, y));
                }
            }

            // Skip the header row for edges.csv
            edgeReader.readLine();
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

    // Find node by its name
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
        for (Node node : nodes) {
            Node current = node;
            while (previousNodes.get(current) != null) {
                Node previous = previousNodes.get(current);
                optimizedEdges.add(new Edge(previous, current, current.getX() - previous.getX(),
                        0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
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
                        edge.getTarget().getX(), edge.getTarget().getY(), edge.getDistance());
            }

            // Draw nodes
            for (Node node : nodes) {
                g2.setColor(Color.BLUE);
                g2.fillOval(node.getX().intValue() - 10, node.getY().intValue() - 10, 20, 20);
                g2.setColor(Color.BLACK);
                g2.drawString(node.getName(), node.getX().intValue() - 10, node.getY().intValue() - 20);
            }
        }

        // Draw an edge with a label indicating distance
        private void drawEdge(Graphics2D g2, Double x1, Double y1, Double x2, Double y2, Double weight) {
            g2.setColor(Color.BLACK);
            g2.drawLine(x1.intValue(), y1.intValue(), x2.intValue(), y2.intValue());
            int midX = (x1.intValue() + x2.intValue()) / 2;
            int midY = (y1.intValue() + y2.intValue()) / 2;
            g2.setColor(Color.BLUE);
            g2.drawString(String.format("%.2f", weight), midX, midY);
        }
    }

    // Main method to run the program with GUI
    public static void main(String[] args) {
        GraphVisualizer graphVisualizer = new GraphVisualizer();
        graphVisualizer.loadGraph("nodes.csv", "edges.csv");

        // Create the main window with a layout
        JFrame frame = new JFrame("Graph Visualizer");
        frame.setLayout(new BorderLayout());

        // Information Display Panel (text area)
        JPanel infoPanel = new JPanel();
        JTextArea infoArea = new JTextArea(5, 20);
        infoArea.setText("Welcome to the Graph Visualizer!");
        infoArea.setEditable(false);
        infoPanel.add(infoArea);

        // Node, Edge, and Algorithm Selection Panels
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new GridLayout(3, 2));

        JComboBox<String> nodeComboBox = new JComboBox<>();
        for (Node node : graphVisualizer.nodes) {
            nodeComboBox.addItem(node.getName());
        }
        selectionPanel.add(new JLabel("Select Start Node:"));
        selectionPanel.add(nodeComboBox);

        JButton visualizeButton = new JButton("Visualize");

        // Action on "Visualize" Button Pressed
        visualizeButton.addActionListener(e -> {
            String selectedNode = (String) nodeComboBox.getSelectedItem();
            graphVisualizer.calculateOptimizedGraph(selectedNode);

            // Create and display both graphs
            JFrame originalFrame = new JFrame("Original Graph");
            originalFrame.add(new GraphPanel(graphVisualizer.nodes, graphVisualizer.edges));
            originalFrame.setSize(800, 800);
            originalFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            originalFrame.setVisible(true);

            JFrame optimizedFrame = new JFrame("Optimized Graph");
            optimizedFrame.add(new GraphPanel(graphVisualizer.nodes, graphVisualizer.optimizedEdges));
            optimizedFrame.setSize(800, 800);
            optimizedFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            optimizedFrame.setVisible(true);
        });

        // Add components to main frame
        frame.add(infoPanel, BorderLayout.NORTH);
        frame.add(selectionPanel, BorderLayout.CENTER);
        frame.add(visualizeButton, BorderLayout.SOUTH);
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
