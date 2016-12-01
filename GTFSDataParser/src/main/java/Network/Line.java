package Network;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by micha on 29.11.2016.
 */
public class Line {
    private int id;
    private List<Node> stops = new LinkedList<>();
    private List<Integer> stopIDs = new LinkedList<>();

    public Line() {
    }

    public Line(int id, Node firststop) {
        this.id = id;
        this.integrateIntoNode(firststop);
        this.stops.add(firststop);
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

    public void absorbLine(Line other, Node mergenode) {
        List<Node> othernodes = new LinkedList<>(other.getStops());
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
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public List<Node> getStops() {
        return stops;
    }

    public void setStops(List<Node> stops) {
        this.stops = stops;
    }

    @XmlElementWrapper(name = "stops")
    @XmlElement(name = "s")
    public List<Integer> getStopIDs() {
        return stopIDs;
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
        this.stops = this.stopIDs.stream().map(nodemap::get).collect(Collectors.toCollection(LinkedList::new));
    }
}
