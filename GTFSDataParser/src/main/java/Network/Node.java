package Network;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by micha on 01.11.2016.
 */
public class Node {
    protected String name;
    protected double lat;
    protected double lon;
    protected Set<Node> neighbours;

    public Node(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.neighbours = new HashSet<>();
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

    @XmlElement //TODO this creates loops in the serialization, do something different
    public Set<Node> getNeighbours() {
        return neighbours;
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
