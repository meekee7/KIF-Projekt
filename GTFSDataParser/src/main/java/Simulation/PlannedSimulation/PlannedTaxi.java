package Simulation.PlannedSimulation;

import Network.Node;
import Simulation.Entity.EdgeLocation;
import Simulation.Entity.NodeLocation;
import Simulation.Entity.Passenger;
import Simulation.Entity.Taxi;
import Simulation.Simulator;

import java.util.*;

/**
 * Created by micha on 20.01.2017.
 */
public class PlannedTaxi extends Taxi {
    protected Set<Passenger> assigned = new HashSet<>();
    protected List<Node> corepath = new LinkedList<>();

    public PlannedTaxi(Simulator sim, int id, int capacity, List<Node> futurepath) {
        super(sim, id, capacity, new LinkedList<>(futurepath));
        this.initLocation();
    }

    public PlannedTaxi(Taxi taxi) {
        super(taxi);
    }

    @Override
    public Node fetchNextNode() {
        if (this.location == null)
            return this.futurepath.remove(0);
        NodeLocation loc = (NodeLocation) this.getLocation();
        Node curnode = loc.getNode();
        if (!this.corepath.isEmpty() && this.corepath.get(0) == curnode)
            this.corepath.remove(0);
        if (this.corepath.isEmpty()) {
            this.incStandstill();
            return curnode;
        }
        Node nexttarget = this.corepath.get(0);
        return this.simulator.getGraph().getPathFromCache(curnode, nexttarget).get(1);
    }

    @Override
    public void loadPassenger(Passenger passenger) {
        super.loadPassenger(passenger);
        this.assigned.remove(passenger);
    }

    public boolean isAssignedTo(Passenger passenger) {
        return this.assigned.contains(passenger);
    }

    public void addToAssigned(Passenger passenger) {
        this.assigned.add(passenger);
    }

    public Set<Passenger> getAssigned() {
        return assigned;
    }

    public List<Node> getCorepath() {
        List<Node> copy = new ArrayList<>(this.corepath);
        Node currnode;
        if (this.location instanceof NodeLocation)
            currnode = ((NodeLocation) this.location).getNode();
        else
            currnode = ((EdgeLocation) this.location).getEnd().getNode();
        if (copy.isEmpty() || currnode != copy.get(0))
            copy.add(0, currnode);
        return copy;
    }

    public void setCorepath(List<Node> corepath) {
        this.corepath = corepath;
    }
}
