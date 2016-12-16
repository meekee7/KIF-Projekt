package Network.IO;

import Network.Edge;
import Network.Graph;
import Network.Node;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by micha on 14.12.2016.
 */
public class DistanceMatrix {
    private Map<Edge, Double> distances;
    private Graph graph;

    public DistanceMatrix(Graph graph) {
        this.graph = graph;
        int nodes = this.graph.getNodes().size();
        this.distances = new ConcurrentHashMap<>(nodes * nodes / 2 - nodes);
    }

    public void calc() {
        AtomicInteger count = new AtomicInteger(0);
        int nodes = this.graph.getNodes().size();
        int size = nodes * nodes / 2 - nodes;
        System.out.println("Calculating distance matrix of size " + size);
        this.graph.getNodes().parallelStream().forEach(x -> this.graph.getNodes().forEach(y -> {
            if (x.getId() < y.getId()) {
                if (count.incrementAndGet() % 100 == 0)
                    System.out.println(count.intValue() + " out of " + size);
                this.distances.put(new Edge(x, y),
                        Graph.getPathLength(this.graph.getShortestPathWeighted(x, y)));
            }
        }));
    }

    public double getDistance(Node a, Node b) {
        return this.distances.get(new Edge(a, b));
    }

    private void setDistance(Node a, Node b, double distance) {
        this.distances.put(new Edge(a, b), distance);
    }
}
