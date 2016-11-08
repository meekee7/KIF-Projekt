package Network;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by micha on 01.11.2016.
 */
@XmlRootElement
public class Graph {
    private Collection<Node> nodes = new HashSet<>();
    private Collection<String> routesIncluded = Collections.EMPTY_LIST;

    /**
     * This method makes all one-directional links in the graph bidirectional.
     */
    public void makeEdgesSymmetric() {
        this.nodes.forEach(x ->
                x.getNeighbours().forEach(y ->
                        y.getNeighbours().add(x)));
    }

    public void mergeNodes(Collection<Node> tomerge) {
        Optional<Node> minopt = tomerge.stream()
                .min((x, y) ->
                        x.getName().length() - y.getName().length());

        if (!minopt.isPresent())
            return;
        Node min = minopt.get();

        tomerge.remove(min);
        tomerge.forEach(x -> min.getNeighbours().addAll(x.getNeighbours())); //Absorb all neighbours
        min.getNeighbours().forEach(x -> x.getNeighbours().add(x)); //Become neighbour for all neighbours
        this.nodes.removeAll(tomerge);
        this.nodes.forEach(x -> x.getNeighbours().removeAll(tomerge)); //Forget all nodes that were removed
    }

    public boolean isConnected() {
        Set<Node> component = collectComponent(this.nodes.stream().findFirst().get());
        return component.size() == this.nodes.size();
    }

    private static Set<Node> collectComponent(Node start) {
        Queue<Node> queue = new LinkedList<>();
        Set<Node> checked = new HashSet<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if (!checked.contains(node)) {
                node.getNeighbours().stream()
                        .filter(x -> !checked.contains(x))
                        .forEach(queue::add);
                checked.add(node);
            }
        }
        return checked;
    }

    public void removeDisconnected() {
        this.nodes.removeIf(x -> x.getNeighbours().isEmpty());
        Set<Node> unconnected = new HashSet<>(this.nodes);
        LinkedList<Collection<Node>> components = new LinkedList<>();

        while (!unconnected.isEmpty()) {
            Set<Node> component = collectComponent(unconnected.stream().findFirst().get());
            unconnected.removeAll(component);
            components.add(component);
        }

        this.nodes = components.stream()
                .max((x, y) -> x.size() - y.size())
                .get();
    }

    public IntSummaryStatistics getEdgeStats() {
        return this.nodes.stream().mapToInt(x -> x.getNeighbours().size()).summaryStatistics();
    }

    @XmlElement
    public Collection<Node> getNodes() {
        return nodes;
    }

    @XmlElement
    public Collection<String> getRoutesIncluded() {
        return routesIncluded;
    }

    public static Graph parseGTFS(GtfsDaoImpl data, Collection<String> routenames) {
        Graph graph = new Graph();

        System.out.println("Graph building start");

        Collection<Route> routes = data.getAllRoutes().stream()
                .filter(x -> routenames.contains(x.getShortName()))
                .collect(Collectors.toSet());

        System.out.println("Routes collected: " + routes.size());
        graph.routesIncluded = routes.stream().map(Route::getShortName).collect(Collectors.toList());

        Collection<Trip> trips = data.getAllTrips().stream()
                .filter(x -> routes.contains(x.getRoute()))
                .collect(Collectors.toSet());

        System.out.println("Trips collected: " + trips.size());

        Collection<StopTime> stoptimes = data.getAllStopTimes().stream()
                .filter(x -> trips.contains(x.getTrip()))
                .collect(Collectors.toList());

        Collection<Stop> stops = stoptimes.stream()
                .map(StopTime::getStop)
                .distinct()
                .collect(Collectors.toList());

        System.out.println("Stops collected: " + stops.size());

        Map<Stop, Node> stopmap = new HashMap<>(stops.size());
        stops.forEach(x -> stopmap.put(x, new Node(x.getName(), x.getLat(), x.getLon())));

        System.out.println("Stopmap built");

        Map<Trip, Collection<StopTime>> tripmap = new HashMap<>();
        trips.forEach(x -> tripmap.put(x, new HashSet<>()));
        stoptimes.forEach(x -> tripmap.get(x.getTrip()).add(x));

        System.out.println("Tripmap built");

        trips.forEach(trip -> tripmap.get(trip).stream()
                .sorted((x, y) -> x.getStopSequence() - y.getStopSequence())
                .map(StopTime::getStop)
                .reduce(null, (prev, curr) -> {
                    if (prev != null)
                        stopmap.get(curr).getNeighbours().add(stopmap.get(prev));
                    return curr;
                }));

        System.out.println("Edges first pass");

        graph.getNodes().addAll(stopmap.values());
        graph.makeEdgesSymmetric();

        System.out.println("Edges second pass");

        Collection<Transfer> alltransfers = data.getAllTransfers().stream()
                .filter(x -> x.getFromStop() != x.getToStop()) //Skip self-transfers
                .collect(Collectors.toList());
        Collection<Set<Stop>> transferclusters = new ArrayList<>(alltransfers.size());
        alltransfers.forEach(transfer -> {
            Optional<Set<Stop>> cluster = transferclusters.stream()
                    .filter(x ->
                            x.contains(transfer.getFromStop()) || x.contains(transfer.getToStop()))
                    .findFirst();
            if (cluster.isPresent()) {
                cluster.get().add(transfer.getFromStop());
                cluster.get().add(transfer.getToStop());
            } else {
                Set<Stop> newcluster = new HashSet<>();
                newcluster.add(transfer.getFromStop());
                newcluster.add(transfer.getToStop());
                transferclusters.add(newcluster);
            }
        });

        System.out.println("Transfer clusters built: " + transferclusters.size());

        /*
        transferclusters.stream().map(x ->
                x.stream()
                        .min((y, z) -> y.getName().length() - z.getName().length())
        ).map(x->x.get().getName()).forEach(System.out::println);
        */

        transferclusters.stream()
                .map(x -> x.stream()
                        .filter(stops::contains)
                        .map(stopmap::get)
                        .collect(Collectors.toList())
                ).forEach(graph::mergeNodes);

        System.out.println("Transfer clusters merged, nodes remaining: " + graph.getNodes().size());

        graph.makeEdgesSymmetric();
        System.out.println("Edges third pass");

        graph.removeDisconnected();

        System.out.println("Graph removed disconnected components, nodes remaining: " + graph.getNodes().size());

        return graph;
    }
}
