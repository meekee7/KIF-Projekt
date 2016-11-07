package Network;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        this.neighbours = ConcurrentHashMap.newKeySet();
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

    @XmlAttribute
    public Set<Node> getNeighbours() {
        return neighbours;
    }
}
