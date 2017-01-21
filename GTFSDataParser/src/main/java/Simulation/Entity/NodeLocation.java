package Simulation.Entity;

import Network.Node;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by micha on 12.12.2016.
 */
public class NodeLocation extends Location {
    protected final Node node;
    Collection<Passenger> passengers;

    public NodeLocation(Node node, double lat, double lon) {
        this.node = node;
        this.lat = lat;
        this.lon = lon;
        this.passengers = new LinkedList<>();
    }

    public Node getNode() {
        return node;
    }

    public Collection<Passenger> getPassengers() {
        return passengers;
    }

    /**
     * Automatically generated with IntelliJ IDEA
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeLocation that = (NodeLocation) o;

        return node != null ? node.equals(that.node) : that.node == null;
    }

    /**
     * Automatically generated with IntelliJ IDEA
     *
     * @return
     */
    @Override
    public int hashCode() {
        return node != null ? node.hashCode() : 0;
    }
}
