import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class Graph {
    private Map<Long, List<Edge>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
    }

    public void addNode(long nodeId) {
        adjacencyList.putIfAbsent(nodeId, new ArrayList<>());
    }

    public void addEdge(long source, long target, double weight) {
        adjacencyList.get(source).add(new Edge(target, weight));
        adjacencyList.get(target).add(new Edge(source, weight)); // Assuming an undirected graph
    }

    public List<Long> dijkstra(long start, long end) {
        Map<Long, Double> distances = new HashMap<>();
        Map<Long, Long> previous = new HashMap<>();
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(node -> node.distance));

        for (long node : adjacencyList.keySet()) {
            distances.put(node, Double.MAX_VALUE);
            previous.put(node, -1L);
        }
        distances.put(start, 0.0);
        priorityQueue.add(new Node(start, 0.0));

        while (!priorityQueue.isEmpty()) {
            Node current = priorityQueue.poll();
            if (current.id == end) break;

            for (Edge edge : adjacencyList.get(current.id)) {
                double newDist = distances.get(current.id) + edge.weight;
                if (newDist < distances.get(edge.target)) {
                    distances.put(edge.target, newDist);
                    previous.put(edge.target, current.id);
                    priorityQueue.add(new Node(edge.target, newDist));
                }
            }
        }

        List<Long> path = new ArrayList<>();
        for (Long at = end; at != -1; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path.size() == 1 && !path.get(0).equals(start) ? new ArrayList<>() : path;
    }

    public double getPathDistance(List<Long> path) {
        double totalDistance = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            for (Edge edge : adjacencyList.get(path.get(i))) {
                if (edge.target.equals(path.get(i + 1))) {
                    totalDistance += edge.weight;
                    break;
                }
            }
        }
        return totalDistance;
    }
}

class Edge {
    Long target;
    double weight;

    public Edge(Long target, double weight) {
        this.target = target;
        this.weight = weight;
    }
}

class Node {
    Long id;
    double distance;

    public Node(Long id, double distance) {
        this.id = id;
        this.distance = distance;
    }
}

public class GraphFromCSV {
    public static void main(String[] args) {
        String nodesFile = "nodes.csv";
        String edgesFile = "edges.csv";
        Graph graph = new Graph();

        try (BufferedReader nodesReader = new BufferedReader(new FileReader(nodesFile));
             BufferedReader edgesReader = new BufferedReader(new FileReader(edgesFile))) {

            // Skip header
            nodesReader.readLine();
            edgesReader.readLine();

            // Load nodes
            String line;
            while ((line = nodesReader.readLine()) != null) {
                String[] parts = line.split(",");
                long nodeId = Long.parseLong(parts[0]);
                graph.addNode(nodeId);
            }

            // Load edges
            while ((line = edgesReader.readLine()) != null) {
                String[] parts = line.split(",");
                long source = Long.parseLong(parts[0]);
                long target = Long.parseLong(parts[1]);
                double weight = Double.parseDouble(parts[2]);
                graph.addEdge(source, target, weight);
            }

            // User input
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the start node ID: ");
            long startNode = scanner.nextLong();
            System.out.print("Enter the end node ID: ");
            long endNode = scanner.nextLong();

            // Dijkstra's algorithm
            List<Long> shortestPath = graph.dijkstra(startNode, endNode);
            if (shortestPath.isEmpty()) {
                System.out.println("No path found between nodes " + startNode + " and " + endNode);
            } else {
                System.out.println("Shortest path: " + shortestPath);
                System.out.println("Shortest distance: " + graph.getPathDistance(shortestPath) + " km");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
