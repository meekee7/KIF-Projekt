package Network;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by micha on 01.11.2016.
 */
public class Node {
    protected int id;
    protected String name;
    protected double lat;
    protected double lon;
    protected Set<Node> neighbours = new HashSet<>();
    protected Set<Line> lines = new HashSet<>();

    public Node() {
    }

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

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public Set<Node> getNeighbours() {
        return neighbours;
    }

    public void addNeighbour(Node neighbour) {
        if (neighbour != this)
            this.neighbours.add(neighbour);
    }

    public void addNeighbours(Collection<Node> newneighbours) {
        this.neighbours.addAll(newneighbours);
        this.neighbours.remove(this);
    }

    public void absorbNode(Node other) {
        if (other.name.length() < this.name.length())
            this.name = other.name;
        this.addNeighbours(other.neighbours);
        this.neighbours.forEach(x -> x.addNeighbour(this));
        other.neighbours.forEach(x -> x.neighbours.remove(other));
    }

    public Point2D getPoint() {
        return new Point2D.Double(this.lat, this.lon);
    }

    public double getDistance(Node other){
        return this.getPoint().distance(other.getPoint());
    }

    public Set<Line> getLines() {
        return lines;
    }

    public List<Line> getEndLines() {
        return this.lines.stream().filter(x -> x.getStartAndEnd().contains(this)).collect(Collectors.toList());
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
