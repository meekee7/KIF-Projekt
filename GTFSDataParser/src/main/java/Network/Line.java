package Network;

import Network.Utils.Identifiable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by micha on 29.11.2016.
 */
public class Line implements Identifiable {
    private int id;
    protected List<Node> stops = new LinkedList<>();
    private List<Integer> stopIDs = new LinkedList<>();
    protected Set<Line> neighbourlines = null;
    private int colour;

    private static int createColour(Random random) {
        return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)).getRGB();
    }

    public Line() {
    }

    public Line(int id, Random colourrand, Node firststop) {
        this.id = id;
        this.integrateIntoNode(firststop);
        this.stops.add(firststop);
        this.colour = createColour(colourrand);
    }

    public Line(int id, Random colourrand, List<Node> stops) {
        this.id = id;
        this.stops = new ArrayList<>(stops);
        this.stops.forEach(this::integrateIntoNode);
        this.colour = createColour(colourrand);
    }

    public Node getStart() {
        return this.stops.get(0);
    }

    public Node getEnd() {
        return this.stops.get(this.stops.size() - 1);
    }

    public List<Node> getStartAndEnd() {
        List<Node> result = new ArrayList<>(2);
        result.add(this.getStart());
        result.add(this.getEnd());
        return result;
    }

    public Set<Line> getNeighbourLines() {
        if (this.neighbourlines == null)
            throw new IllegalStateException("Neighbour lines were not calculated yet");
        return this.neighbourlines;
    }

    public void calcNeighbourLines() {
        Set<Line> result = new HashSet<>();
        this.stops.forEach(x -> result.addAll(x.getLines()));
        result.remove(this);
        this.neighbourlines = result;
    }

    public boolean canAdd(Node node) {
        return this.getStartAndEnd().stream().anyMatch(x -> x.getNeighbours().contains(node))
                && !this.stops.contains(node);
    }

    private void integrateIntoNode(Node node) {
        node.getLines().add(this);
    }

    public void addToStart(Node node) {
        if (this.stops.contains(node) || !this.getStart().getNeighbours().contains(node))
            throw new IllegalArgumentException("Cannot add node to start");
        integrateIntoNode(node);
        this.stops.add(0, node);
    }

    public void addToEnd(Node node) {
        if (this.stops.contains(node) || !this.getEnd().getNeighbours().contains(node)) {
            System.err.println(node);
            System.err.println(this.getStops());
            throw new IllegalArgumentException("Cannot add node to end");
        }
        integrateIntoNode(node);
        this.stops.add(node);
    }

    public void addToPlace(Node node, Node place) {
        if (this.getStart() == place)
            this.addToStart(node);
        else if (this.getEnd() == place)
            this.addToEnd(node);
        else
            throw new IllegalArgumentException("Place neither start nor end");
    }

    public void addFitting(Node node) {
        if (this.getStart().getNeighbours().contains(node))
            this.addToPlace(node, this.getStart());
        else if (this.getEnd().getNeighbours().contains(node))
            this.addToPlace(node, this.getEnd());
        else
            throw new IllegalArgumentException("Node does not fit at start or end");
    }

    public boolean canAbsorb(Line other, Node mergenode) {
        return this.getStops().stream().allMatch(x -> mergenode == x || !other.getStops().contains(x));
    }

    public void verify() {
        if (new HashSet<>(this.stops).size() != this.stops.size())
            throw new IllegalStateException("Duplicate stops");
        Graph.verifyPath(this.stops);
    }

    public void absorbLine(Line other, Node mergenode) {
        if (!this.canAbsorb(other, mergenode))
            throw new IllegalArgumentException("Cannot absorb other line");
        List<Node> othernodes = new LinkedList<>(other.getStops());
        othernodes.remove(mergenode);
        if (this.getStart() == mergenode && other.getStart() == mergenode) {
            Collections.reverse(othernodes);
            othernodes.addAll(this.stops);
            this.stops = othernodes;
        } else if (this.getStart() == mergenode && other.getEnd() == mergenode) {
            othernodes.addAll(this.stops);
            this.stops = othernodes;
        } else if (this.getEnd() == mergenode && other.getStart() == mergenode)
            this.stops.addAll(othernodes);
        else if (this.getEnd() == mergenode && other.getEnd() == mergenode) {
            Collections.reverse(othernodes);
            this.stops.addAll(othernodes);
        } else {
            System.out.println(this.getStartAndEnd());
            System.out.println(other.getStartAndEnd());
            System.out.println(mergenode);
            throw new IllegalArgumentException("Merge node not part of both lines");
        }
        other.getStops().forEach(x -> {
            x.getLines().remove(other);
            x.getLines().add(this);
        });
    }

    @XmlAttribute(name = "id")
    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Color getColourAWT() {
        return new Color(this.colour);
    }

    @XmlAttribute(name = "colour")
    public int getColour() {
        return this.colour;
    }

    public void setColour(int colour) {
        this.colour = colour;
    }

    public void setColour(Color colour) {
        this.colour = colour.getRGB();
    }

    public List<Node> getStops() {
        return stops;
    }

    public Node endNodeDirected(Node start, Node end) {
        if (this.stops.indexOf(start) < this.stops.indexOf(end))
            return this.getEnd();
        else
            return this.getStart();
    }

    @XmlElementWrapper(name = "stops")
    @XmlElement(name = "s")
    public List<Integer> getStopIDs() {
        this.stopIDs = this.stops.stream().map(Node::getId).collect(Collectors.toList());
        return this.stopIDs;
    }

    /**
     * /**
     * This method is a helper for GraphIO. Because JAXB will use the getter above
     * and just use addAll on the collection GraphIO needs to integrate the edge
     * data into the nodes.
     *
     * @param nodemap A map from NodeIDs to actual nodes
     */
    public void postIOIntegration(Map<Integer, Node> nodemap) {
        this.stops = this.stopIDs.stream().map(nodemap::get).collect(Collectors.toCollection(ArrayList::new));
        if (this.stops.size() != this.stopIDs.size())
            throw new IllegalStateException("Stops unequal to StopIDs");
        this.stops = Collections.unmodifiableList(this.stops);
    }
}
