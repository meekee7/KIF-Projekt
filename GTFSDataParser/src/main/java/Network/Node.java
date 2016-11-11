package Network;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by micha on 01.11.2016.
 */
public class Node {
    protected final int id;
    protected final String name;
    protected final double lat;
    protected final double lon;
    protected final Set<Node> neighbours;

    public Node(int id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.neighbours = new HashSet<>();
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlAttribute
    public double getLat() {
        return lat;
    }

    @XmlAttribute
    public double getLon() {
        return lon;
    }

    public Set<Node> getNeighbours() {
        return neighbours;
    }

    public void addNeighbour(Node neighbour) {
        if (neighbour != this)
            this.neighbours.add(neighbour);
    }

    public void addNeighbours(Collection<Node> newneighbours) {
        this.neighbours.addAll(newneighbours.stream()
                .filter(x -> x != this)
                .collect(Collectors.toList()
                ));
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", links:" + neighbours.size() +
                '}';
    }
}
