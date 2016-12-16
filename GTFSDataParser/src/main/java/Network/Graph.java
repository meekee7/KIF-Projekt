package Network;

import Network.IO.IOEdge;
import Network.IO.StatJSON;
import Network.LineMaking.NeighbourNode;
import Network.LineMaking.UnitableLines;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by micha on 01.11.2016.
 */
@XmlRootElement
public class Graph {
    protected Collection<Node> nodes = new HashSet<>();
    protected Collection<String> routesIncluded = new ArrayList<>();
    private Collection<IOEdge> ioedges = new HashSet<>();
    private Collection<Line> lines = new HashSet<>();
    protected String name;

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Graph() {
    }

    /**
     * This method makes all one-directional links in the graph bidirectional and removes neighbours not in the official node list
     */
    public void repairEdges() {
        this.nodes.forEach(x -> {
            x.getNeighbours().forEach(y ->
                    y.addNeighbour(x));
            x.getNeighbours().removeIf(y -> !this.nodes.contains(y));
        });
    }

    /**
     * This method removes all edges that point a node to itself.
     * It is now redundant since nodes take care that they do not get any straps.
     */
    @Deprecated
    public void removeStraps() {
        this.nodes.forEach(x -> x.getNeighbours().remove(x));
    }

    public void mergeNodes(Collection<Node> tomerge) {
        if (tomerge.isEmpty())
            return;
        Optional<Node> minopt = tomerge.stream()
                .min(Comparator.comparingInt(x -> x.getName().length()));

        Node min = minopt.get();
        tomerge.remove(min);
        tomerge.forEach(min::absorbNode);
        this.nodes.removeAll(tomerge);
    }

    public boolean isConnected() {
        Set<Node> component = collectComponent(this.nodes.stream().findFirst().get());
        return component.size() == this.nodes.size();
    }

    private static Set<Line> collectLineComponent(Line start) {
        Queue<Line> queue = new LinkedList<>();
        Set<Line> checked = new HashSet<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Line line = queue.remove();
            if (!checked.contains(line)) {
                line.getNeighbourLines().stream()
                        .filter(x -> !checked.contains(x))
                        .forEach(queue::add);
                checked.add(line);
            }
        }
        return checked;
    }

    public Collection<Set<Line>> getLineComponents() {
        this.nodes.removeIf(x -> x.getNeighbours().isEmpty());
        Set<Line> unconnected = new HashSet<>(this.lines);
        LinkedList<Set<Line>> components = new LinkedList<>();
        this.calcNeighbourLines();

        while (!unconnected.isEmpty()) {
            Set<Line> component = collectLineComponent(unconnected.stream().findFirst().get());
            unconnected.removeAll(component);
            components.add(component);
        }
        return components;
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
        LinkedList<Set<Node>> components = new LinkedList<>();

        while (!unconnected.isEmpty()) {
            Set<Node> component = collectComponent(unconnected.stream().findFirst().get());
            unconnected.removeAll(component);
            components.add(component);
        }

        System.out.println("Components: " + components.stream().mapToInt(Collection::size).summaryStatistics());

        this.nodes = components.stream().max(Comparator.comparingInt(Collection::size)).get();

        //System.out.println(components.stream().filter(x -> x != this.nodes).map(x -> x.stream().map(Node::getName).collect(Collectors.toList())).collect(Collectors.toList()));
    }

    public IntSummaryStatistics getEdgeStats() {
        return this.nodes.stream().mapToInt(x -> x.getNeighbours().size()).summaryStatistics();
    }

    public IntSummaryStatistics getLineStats() {
        return this.lines.stream().mapToInt(x -> x.getStops().size()).summaryStatistics();
    }

    @XmlElementWrapper(name = "nodes")
    @XmlElement(name = "n")
    public Collection<Node> getNodes() {
        return nodes;
    }

    @XmlElementWrapper(name = "routesIncluded")
    @XmlElement(name = "r")
    public Collection<String> getRoutesIncluded() {
        return routesIncluded;
    }

    /**
     * This method returns a set of edges in the graph.
     * Note that this edge set is recreated every time this method is called.
     * The set you receive is not maintained.
     * This method exists for IO purposes and is not intended for computational usage.
     *
     * @return A collection of all edges in the graph.
     */
    @XmlElementWrapper(name = "edges")
    @XmlElement(name = "e")
    public Collection<IOEdge> getIOEdges() {
        this.nodes.forEach(x ->
                x.getNeighbours().forEach(y ->
                        this.ioedges.add(new IOEdge(x.getId(), y.getId()))
                )
        );
        return this.ioedges;
    }

    public Collection<Edge> getEdges() {
        Collection<Edge> edges = new HashSet<>(this.nodes.size() * 2);
        this.nodes.forEach(x ->
                x.getNeighbours().forEach(y ->
                        edges.add(new Edge(x, y))
                )
        );
        return edges;
    }

    @XmlElementWrapper(name = "lines")
    @XmlElement(name = "l")
    public Collection<Line> getLines() {
        return lines;
    }

    /**
     * This method is a helper for GraphIO. Because JAXB will use the getter above
     * and just use addAll on the collection GraphIO needs to integrate the edge
     * data into the nodes.
     */
    public void postIOIntegration() {
        System.out.println("Graph post-IO integration");
        this.nodes.forEach(x -> x.getNeighbours().clear());
        Map<Integer, Node> nodemap = new HashMap<>(this.nodes.size());
        this.nodes.forEach(x -> nodemap.put(x.id, x));
        this.ioedges.forEach(edge -> {
            nodemap.get(edge.getA()).addNeighbour(nodemap.get(edge.getB()));
            nodemap.get(edge.getB()).addNeighbour(nodemap.get(edge.getA()));
        });
        this.lines.forEach(x -> x.postIOIntegration(nodemap));
        this.lines.forEach(x -> x.getStops().forEach(y -> y.getLines().add(x)));
    }

    private static void walkSmallNode(Set<Node> target, Node node) {
        if (target.contains(node) || node.getNeighbours().size() > 2)
            return;
        target.add(node);
        node.getNeighbours().forEach(x -> walkSmallNode(target, x));
    }

    private List<Node> orderChain(Set<Node> cluster) {
        List<Node> chain = new ArrayList<>(cluster.size());
        while (!cluster.isEmpty()) {
            Node node = cluster.stream()
                    .filter(x -> x.getNeighbours().size() == 1
                            || !x.getNeighbours().stream()
                            .allMatch(cluster::contains))
                    .findFirst()
                    .get();
            chain.add(node);
            cluster.remove(node);
        }
        return chain;
    }

    public void collapseChains() {
        Collection<Node> smallnodes = this.nodes.stream()
                .filter(x -> x.getNeighbours().size() <= 2
                        && x.getNeighbours().stream()
                        .anyMatch(y -> y.getNeighbours().size() <= 2))
                .collect(Collectors.toSet());
        Collection<Set<Node>> clusters = new LinkedList<>();
        while (!smallnodes.isEmpty()) {
            Set<Node> cluster = new HashSet<>();
            Node start = smallnodes.stream().findFirst().get();
            walkSmallNode(cluster, start);
            smallnodes.removeAll(cluster);
            clusters.add(cluster);
        }
        System.out.println("Chain cluster stats " + clusters.stream().mapToInt(Set::size).summaryStatistics());
        clusters.stream()
                .map(this::orderChain)
                .forEach(this::mergeNodes);
    }

    /**
     * This method uses the Dijkstra algorithm to calculate a path
     * with minimum length between two nodes.
     * The edges are weighted with the air distance.
     *
     * @param start The start node
     * @param end   The end node
     * @return A path between start and end with minimum length
     */
    public List<Node> getShortestPathWeighted(Node start, Node end) {
        Map<Node, Double> distance = new HashMap<>(this.nodes.size());
        Map<Node, Node> prev = new HashMap<>(this.nodes.size());
        this.nodes.forEach(x -> distance.put(x, Double.POSITIVE_INFINITY));
        this.nodes.forEach(x -> prev.put(x, null));
        distance.replace(start, 0.0);
        Set<Node> queue = new HashSet<>(this.nodes);
        while (!queue.isEmpty()) queueloop:{
            Node current = queue.stream().min(Comparator.comparingDouble(distance::get)).get();
            queue.remove(current);
            if (current == end)
                break queueloop;
            current.getNeighbours().forEach(neighbour -> {
                double dist = distance.get(current) + current.getDistance(neighbour);
                if (dist < distance.get(neighbour)) {
                    distance.put(neighbour, dist);
                    prev.put(neighbour, current);
                }
            });
        }

        List<Node> path = new LinkedList<>();
        for (Node node = end; prev.get(node) != null; node = prev.get(node))
            path.add(0, node);
        if (path.get(0) != start)
            path.add(0, start);
        return path;
    }

    /**
     * This method uses a simplified variant to find a route between two lines
     * with minimum line switches.
     *
     * @param start The start line
     * @param end   The end line
     * @return A route between start and end with minimum line switches, null if there is no route
     */
    public List<Line> getSwitchRoute(Line start, Line end) {
        Map<Line, Line> prev = new HashMap<>();
        Queue<Line> queue = new LinkedList<>();
        queue.add(start);
        prev.put(start, null);
        while (!queue.isEmpty()) switchqueueloop:{
            Line current = queue.poll();
            if (current == end)
                break switchqueueloop;
            current.getNeighbourLines().stream()
                    .filter(x -> !prev.containsKey(x))
                    .peek(x -> prev.put(x, current))
                    .forEach(queue::add);
        }
        if (!prev.containsKey(end))
            return null;
        //throw new IllegalStateException("Could not find route");

        List<Line> path = new LinkedList<>();
        for (Line node = end; prev.get(node) != null; node = prev.get(node))
            path.add(0, node);
        path.add(0, start);
        return path;
    }

    public IntSummaryStatistics getSwitchRouteStats() {
        this.calcNeighbourLines();
        Set<UnitableLines> pairs = new HashSet<>(this.lines.size() * this.lines.size() / 2);
        this.lines.forEach(x -> this.lines.forEach(y -> pairs.add(new UnitableLines(x, y, null))));
        AtomicInteger counter = new AtomicInteger(0);
        return pairs.parallelStream()
                .filter(x -> x.getA() != x.getB())
                .map(x -> {
                    if (counter.incrementAndGet() % 100_000 == 0)
                        System.out.println(counter.intValue() + " out of " + (pairs.size() - this.lines.size()));
                    return this.getSwitchRoute(x.getA(), x.getB());
                })
                .mapToInt(List::size)
                .summaryStatistics();
        //.max(Comparator.comparingInt(List::size))
        //.get();
    }

    private void calcNeighbourLines() {
        this.lines.forEach(Line::calcNeighbourLines);
    }

    public void parseGTFS(GtfsDaoImpl data, String name, Predicate<Route> routepredicate, Collection<Set<Stop>> transferclusters) {
        this.name = name;

        System.out.println("Graph building start");

        Collection<Route> routes = data.getAllRoutes().stream()
                .filter(routepredicate)
                .collect(Collectors.toList());

        System.out.println("Routes included: " + routes.size());
        this.routesIncluded = routes.stream()
                .map(Route::getShortName)
                .collect(Collectors.toList());

        Collection<Trip> trips = data.getAllTrips().stream()
                .filter(x -> routes.contains(x.getRoute()))
                .collect(Collectors.toSet());

        System.out.println("Trips collected: " + trips.size());


        Collection<StopTime> stoptimes = data.getAllStopTimes().stream()
                .filter(x -> trips.contains(x.getTrip()))
                .collect(Collectors.toList());

        Collection<Stop> stops = stoptimes.stream()
                .map(StopTime::getStop)
                .distinct() //Removes duplicate elements
                .collect(Collectors.toList());

        System.out.println("Stops collected: " + stops.size());

        Map<Stop, Node> stopmap = new HashMap<>(stops.size());
        IDFactory nodeIDs = new IDFactory();
        stops.forEach(x -> stopmap.put(x,
                new Node(nodeIDs.createID(), x.getName(), x.getLat(), x.getLon())));

        System.out.println("Stopmap built");

        Map<Trip, Collection<StopTime>> tripmap = new HashMap<>();
        trips.forEach(x -> tripmap.put(x, new HashSet<>()));
        stoptimes.forEach(x -> tripmap.get(x.getTrip()).add(x));

        System.out.println("Tripmap built");

        trips.forEach(trip -> tripmap.get(trip).stream()
                .sorted(Comparator.comparingInt(StopTime::getStopSequence))
                .map(StopTime::getStop)
                .reduce(null, (prev, curr) -> {
                    if (prev != null)
                        stopmap.get(curr).addNeighbour(stopmap.get(prev));
                    return curr;
                }));

        System.out.println("Edges first pass");

        this.getNodes().addAll(stopmap.values());
        this.repairEdges();

        System.out.println("Edges second pass");

        transferclusters.stream()
                .map(x -> x.stream()
                        .filter(stops::contains)
                        .map(stopmap::get)
                        .collect(Collectors.toList())
                ).forEach(this::mergeNodes);

        System.out.println("Transfer clusters merged, nodes remaining: " + this.getNodes().size());

        this.repairEdges();
        //graph.removeStraps();
        System.out.println("Edges third pass");

        this.removeDisconnected();
        this.repairEdges();

        System.out.println("Graph removed disconnected components, nodes remaining: "
                + this.getNodes().size());

        //System.out.println("Pre-chain edge stats:");
        //System.out.println(graph.getEdgeStats());

        int curr = this.getNodes().size();
        int prev = Integer.MAX_VALUE;
        while (curr < prev) { //Repeat until fixpoint is reached
            prev = curr;
            this.collapseChains();
            this.repairEdges();
            curr = this.getNodes().size();
        }

        System.out.println("Graph collapsed chains, nodes remaining: " + this.getNodes().size());
    }

    public static Collection<Set<Stop>> buildTransferClusters(GtfsDaoImpl data) {
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
        System.out.println("Transfer cluster stats: " + transferclusters.stream().mapToInt(Set::size).summaryStatistics());
        return transferclusters;
    }

    public void buildLinesEdgeComplete() {
        this.lines.clear();
        this.nodes.forEach(x -> x.getLines().clear());
        System.out.println("Line building start");
        IDFactory lineIDs = new IDFactory();
        Random random = new Random(23424589798L);

        this.getEdges().forEach(edge -> {
            Line line = new Line(lineIDs.createID(), random, edge.getAnode());
            line.addFitting(edge.getBnode());
            this.lines.add(line);
        });

        AtomicInteger counter = new AtomicInteger(0);
        this.mergeLines(() -> {
            if (counter.incrementAndGet() % 100 == 0)
                System.out.println("Merge " + counter.intValue());
        });
        this.lines.forEach(Line::verify);
        System.out.println("Lines verified");
        System.out.println("Built lines stats: " + this.getLines().stream().mapToInt(x -> x.getStops().size()).summaryStatistics());
        System.out.println("Line building disconnected components: " + this.getLineComponents().stream().mapToInt(Set::size).summaryStatistics());

    }

    public void buildLines() {
        this.lines.clear();
        this.nodes.forEach(x -> x.getLines().clear());
        System.out.println("Line building start");

        Random random = new Random(345345234L); //Arbitrary value
        IDFactory lineIDs = new IDFactory();
        final AtomicInteger pic = new AtomicInteger(0);
        List<StatJSON> stats = new LinkedList<>();
        String basename = "./Sequences/" + this.name + "/" + this.name;
        //Runnable imgmaker = pic::incrementAndGet;
        Runnable imgmaker = () -> {
        };
        //Runnable imgmaker = () -> new SVGBuilder(Graph.this, basename + pic.getAndIncrement()).export();
        imgmaker.run();
        while (this.getNodes().stream().anyMatch(x -> x.getLines().isEmpty())) {
            Collection<Node> singles = this.getNodes().stream()
                    .filter(x -> x.getLines().isEmpty())
                    .filter(x -> x.getNeighbours().stream()
                            .filter(y -> y.getLines().isEmpty())
                            .count() <= 1
                    ).collect(Collectors.toList());
            if (!singles.isEmpty())
                singles.forEach(x -> this.lines.add(new Line(lineIDs.createID(), random, x)));
            else {
                List<Node> nearline = this.getNodes().stream().filter(x -> x.getLines().isEmpty())
                        .filter(x -> x.getNeighbours().stream()
                                .anyMatch(y -> !y.getLines().isEmpty())).collect(Collectors.toList());
                java.util.Collections.shuffle(nearline, random);
                nearline = nearline.subList(0, Math.min(nearline.size(), 5));
                nearline.forEach(x -> {
                    Line line = new Line(lineIDs.createID(), random, x.getNeighbours().stream()
                            .filter(y -> !y.getLines().isEmpty())
                            .findFirst().get());
                    this.lines.add(line);
                    line.addToEnd(x);
                });
            }
            this.lines.forEach(line -> line.getStartAndEnd().forEach(x ->
                            x.getNeighbours().stream()
                                    .filter(y -> y.getLines().isEmpty())
                                    .filter(line::canAdd).findFirst()
                                    .ifPresent(node -> line.addToPlace(node, x))
                    //TODO maybe find an adequate heuristic
            ));
            imgmaker.run();
            mergeLines(imgmaker);
        }

        System.out.println("Basic lines built, expanding");
        AtomicInteger changed = new AtomicInteger(Integer.MAX_VALUE); //An object to bypass restrictions of lambdas
        while (changed.intValue() != 0) {
            changed.set(0);
            this.nodes.stream()
                    .filter(x -> x.getLines().size() == 1 && x.getEndLines().size() == 1)
                    .forEach(node -> {
                        Line line = node.getEndLines().get(0);
                        node.getNeighbours().stream().filter(line::canAdd).findFirst().ifPresent(y -> {
                            line.addFitting(y);
                            changed.incrementAndGet();
                        });
                    });
            imgmaker.run();
        }
        System.out.println("Expansion finished, post-merging");

        int prevlines = this.lines.size();
        mergeLines(imgmaker);
        System.out.println("Post-merging removed " + (prevlines - this.lines.size()) + " lines");
        prevlines = this.lines.size();

        this.calcNeighbourLines();
        Collection<Set<Line>> linecomps = this.getLineComponents();
        System.out.println("Line building disconnected components: " + linecomps.stream().mapToInt(Set::size).summaryStatistics());
        for (Collection<Set<Line>> components = linecomps; components.size() != 1; components = this.getLineComponents()) {
            Set<Line> minset = components.stream().min(Comparator.comparingInt(Set::size)).get();
            Set<Line> maxset = components.stream().filter(x -> x != minset).min(Comparator.comparingInt(Set::size)).get();
            Line minline = minset.stream().findFirst().get();
            Line maxline = maxset.stream().findFirst().get();
            List<Node> path = this.getShortestPathWeighted(minline.getStart(), maxline.getEnd());
            Line newline = new Line(lineIDs.createID(), random, path);
            this.lines.add(newline);
            this.calcNeighbourLines();
            imgmaker.run();
        }
        imgmaker.run();
        System.out.println("Component connecting added " + (this.lines.size() - prevlines) + " lines");
        this.lines.forEach(Line::verify);
        System.out.println("Lines verified");
        System.out.println("Built lines stats: " + this.getLines().stream().mapToInt(x -> x.getStops().size()).summaryStatistics());
        System.out.println("PICS: " + pic.get());
    }

    private void mergeLines(Runnable imagemaker) {
        boolean keepmerging = true;
        while (keepmerging) {
            keepmerging = false;
            loopcore:
            {
                for (Node node : this.getNodes()) {
                    List<Line> endlines = node.getEndLines().stream()
                            .sorted(Comparator.comparingInt(x -> x.getStops().size()))
                            .collect(Collectors.toList());
                    if (endlines.size() >= 2) {
                        if (endlines.get(0).canAbsorb(endlines.get(1), node)) {
                            endlines.get(0).absorbLine(endlines.get(1), node);
                            this.lines.remove(endlines.get(1));
                            keepmerging = true;
                            break loopcore;
                        }
                    } else {
                        List<NeighbourNode> neighbourswithends = node.getNeighbours().stream()
                                .map(x -> new NeighbourNode(x, node))
                                .filter(NeighbourNode::isValid)
                                .sorted(Comparator.comparingInt(x -> x.getMinline().getStops().size()))
                                .collect(Collectors.toList());
                        if (neighbourswithends.size() >= 1 && endlines.size() == 1) {
                            Line myline = endlines.get(0);
                            Line otherline = neighbourswithends.get(0).getMinline();
                            otherline.addFitting(node);
                            if (myline.canAbsorb(otherline, node)) {
                                myline.absorbLine(otherline, node);
                                this.lines.remove(otherline);
                                keepmerging = true;
                                break loopcore;
                            }
                        } else if (neighbourswithends.size() >= 2) {
                            Line line1 = neighbourswithends.get(0).getMinline();
                            Line line2 = neighbourswithends.get(1).getMinline();
                            if (line1.canAbsorb(line2, node)) {
                                line1.addFitting(node);
                                line2.addFitting(node);
                                line1.absorbLine(line2, node);
                                this.lines.remove(line2);
                                keepmerging = true;
                                break loopcore;
                            }
                        }
                    }
                }
            }
            imagemaker.run();
        }
    }

    public static double getPathLength(List<Node> path) {
        double length = 0;
        for (int i = 1; i < path.size(); i++)
            length += path.get(i).getDistance(path.get(i - 1));
        return length;
    }
}