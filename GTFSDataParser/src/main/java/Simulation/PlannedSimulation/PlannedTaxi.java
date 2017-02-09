package Simulation.PlannedSimulation;

import Network.Node;
import Opta.OptaTaxi;
import Simulation.Entity.EdgeLocation;
import Simulation.Entity.NodeLocation;
import Simulation.Entity.Passenger;
import Simulation.Entity.Taxi;
import Simulation.Simulator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by micha on 20.01.2017.
 */
public class PlannedTaxi extends Taxi {
    protected Set<Passenger> assigned = new HashSet<>();
    protected final List<Node> corepath = new LinkedList<>();
//    protected List<List<Node>> corepathhist = new ArrayList<>(10000);

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
//        List<Node> prevcorepath = new ArrayList<>(this.corepath);
//        this.corepathhist.add(prevcorepath);
        if (!this.corepath.isEmpty() && this.corepath.get(0) == curnode)
            this.corepath.remove(0);
        if (this.corepath.isEmpty()) {
            if (!this.passengers.isEmpty() || !this.assigned.isEmpty()) {
                System.out.println("WARNING: APPLYING REPAIR ON TAXI " + this.getId());
                this.corepath.add(curnode);
                this.passengers.forEach(p -> this.setCorepath(this.simulator.getGraph().integrateIntoCorePath(this.corepath, p.getEnd())));
                this.assigned.forEach(p -> this.setCorepath(this.simulator.getGraph().integrateIntoCorePath(this.corepath, p.getStart(), p.getEnd())));
                return curnode;
            } //else {
            this.incStandstill();
            return curnode;
            //}
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
        if (corepath.size() < this.corepath.size())
            System.out.println("WARNING: getting assigned less nodes than we had on taxi " + this.getId());
        this.corepath.clear();
        this.corepath.addAll(corepath);
//        this.corepath = corepath;
    }

    public void streamlineAssignments() {
        Set<Node> relevantnodes = this.passengers.stream().map(Passenger::getEnd).collect(Collectors.toSet());
        this.setCorepath(this.corepath.stream().filter(relevantnodes::contains).distinct().collect(Collectors.toList()));
        this.getAssigned().clear();
    }

    public OptaTaxi toOpta() {
        return new OptaTaxi(this.capacity - this.passengers.size(), this.id, this.simulator.getGraph(), this.getCorepath());
    }
}
