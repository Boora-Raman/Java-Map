import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class GraphApp {
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GraphApp().createIntroScreen());
    }

    private void createIntroScreen() {
        frame = new JFrame("Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel introPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("<html><center>Welcome to Graph Visualization!<br>Select files to proceed.</center></html>", SwingConstants.CENTER);
        JButton nextButton = new JButton("Next");

        nextButton.addActionListener(e -> initializeFileSelectionScreen());

        introPanel.add(label, BorderLayout.CENTER);
        introPanel.add(nextButton, BorderLayout.SOUTH);

        frame.add(introPanel);
        frame.setVisible(true);
    }

    private void initializeFileSelectionScreen() {
        frame.getContentPane().removeAll();
        frame.repaint();

        JPanel fileSelectionPanel = new JPanel();
        fileSelectionPanel.setLayout(new GridLayout(4, 2, 10, 10));

        JLabel nodesLabel = new JLabel("Select Nodes File:");
        JTextField nodesField = new JTextField();
        JButton nodesButton = new JButton("Browse");

        JLabel edgesLabel = new JLabel("Select Edges File:");
        JTextField edgesField = new JTextField();
        JButton edgesButton = new JButton("Browse");

        JButton visualizeButton = new JButton("Visualize Graph");

        nodesButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                nodesField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        edgesButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                edgesField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        visualizeButton.addActionListener(e -> {
            String nodesFile = nodesField.getText();
            String edgesFile = edgesField.getText();

            if (nodesFile.isEmpty() || edgesFile.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please select both files!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                loadGraphData(nodesFile, edgesFile);
                createGraphWindow("Graph Visualization", edges, Color.BLUE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error loading data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        fileSelectionPanel.add(nodesLabel);
        fileSelectionPanel.add(nodesField);
        fileSelectionPanel.add(nodesButton);
        fileSelectionPanel.add(edgesLabel);
        fileSelectionPanel.add(edgesField);
        fileSelectionPanel.add(edgesButton);
        fileSelectionPanel.add(new JLabel()); // Placeholder
        fileSelectionPanel.add(visualizeButton);

        frame.add(fileSelectionPanel);
        frame.setVisible(true);
    }

    private void loadGraphData(String nodesFile, String edgesFile) throws Exception {
        nodes.clear();
        edges.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(nodesFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String id = parts[0].trim(); // Node ID as name
                int x = Integer.parseInt(parts[1].trim());
                int y = Integer.parseInt(parts[2].trim());
                nodes.add(new Node(id, x, y));
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(edgesFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String sourceId = parts[0].trim();
                String destinationId = parts[1].trim();
                double weight = Double.parseDouble(parts[2].trim());
                Node source = getNodeById(sourceId);
                Node destination = getNodeById(destinationId);
                edges.add(new Edge(source, destination, weight));
            }
        }
    }

    private Node getNodeById(String id) {
        return nodes.stream().filter(node -> node.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Node ID: " + id));
    }

    private void createGraphWindow(String title, List<Edge> edges, Color edgeColor) {
        JFrame graphFrame = new JFrame(title);
        graphFrame.setSize(800, 600);
        graphFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(new BasicStroke(2));

                for (Edge edge : edges) {
                    int x1 = edge.getSource().getX();
                    int y1 = edge.getSource().getY();
                    int x2 = edge.getDestination().getX();
                    int y2 = edge.getDestination().getY();

                    g2d.setColor(edgeColor);
                    g2d.drawLine(x1, y1, x2, y2);
                    g2d.drawString(String.valueOf(edge.getWeight()), (x1 + x2) / 2, (y1 + y2) / 2);
                }

                for (Node node : nodes) {
                    int x = node.getX();
                    int y = node.getY();

                    g2d.setColor(Color.RED);
                    g2d.fillOval(x - 5, y - 5, 10, 10);
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(node.getId(), x + 5, y - 5);
                }
            }
        };

        graphFrame.add(graphPanel);
        graphFrame.setVisible(true);
    }
}

class Node {
    private String id;
    private int x;
    private int y;

    public Node(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

class Edge {
    private Node source;
    private Node destination;
    private double weight;

    public Edge(Node source, Node destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }
}
