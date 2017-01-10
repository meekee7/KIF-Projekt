package Network.Utils;

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

    public void getShortestDistWeighted(Node start) {
        Map<Node, Double> distance = new HashMap<>(this.graph.getNodes().size());
        Map<Node, Node> prev = new HashMap<>(this.graph.getNodes().size());
        this.graph.getNodes().forEach(x -> distance.put(x, Double.POSITIVE_INFINITY));
        this.graph.getNodes().forEach(x -> prev.put(x, null));
        distance.replace(start, 0.0);
        NavigableSet<Node> queue = new TreeSet<>(Comparator.comparingDouble(distance::get));
        queue.add(start);

        //Set<Node> queue = new HashSet<>(this.nodes);

        while (!queue.isEmpty()) queueloop:{
            Node current = queue.pollFirst();
            //Node current = queue.stream().min(Comparator.comparingDouble(distance::get)).get();
            queue.remove(current);
            current.getNeighbours().forEach(neighbour -> {
                double dist = distance.get(current) + current.getDistance(neighbour);
                Double global = null;// distances.get(new Edge(start, neighbour));
//                Double global = distances.get(new Edge(start, neighbour));
                if (dist < distance.get(neighbour) && (global == null || dist < global)) {
                    distance.put(neighbour, dist);
                    prev.put(neighbour, current);
                    queue.add(neighbour);
                }
            });
        }

        distance.forEach((node, dist) -> this.distances.put(new Edge(start, node), dist));
    }

    public void calc() {
        AtomicInteger count = new AtomicInteger(0);
        int nodes = this.graph.getNodes().size();
        int size = (nodes + 1) * nodes / 2;// nodes * nodes / 2.0 + (nodes / 2.0);
        System.out.println("Calculating distance matrix of size " + size);
        this.graph.getNodes().parallelStream().forEach(x -> {
            //if (count.incrementAndGet() % 10 == 0)
//                System.out.println( this.distances.size() + " out of " + size + " | " + count.incrementAndGet());
            this.getShortestDistWeighted(x);
//            if (count.get() % 1000 == 0)
//            Runtime.getRuntime().gc();
        });
        System.out.println("Calculated " + this.distances.size());
        /*this.graph.getNodes().parallelStream().forEach(x -> this.graph.getNodes().forEach(y -> {
            if (x.getId() < y.getId()) {
                if (count.incrementAndGet() % 1000 == 0)
                    System.out.println(count.intValue() + " out of " + size);
                this.distances.put(new Edge(x, y),
                        Graph.getPathLength(this.graph.getShortestPathWeighted(x, y)));
            }
        }));*/
    }

    public double getDistance(Node a, Node b) {
        return this.distances.get(new Edge(a, b));
    }

    private void setDistance(Node a, Node b, double distance) {
        this.distances.put(new Edge(a, b), distance);
    }
}
