package Simulation.PlannedSimulation;

import Network.Node;
import Simulation.Entity.Passenger;
import Simulation.Entity.Taxi;
import Simulation.Simulator;

import java.util.*;

/**
 * Created by micha on 20.01.2017.
 */
public class PlannedTaxi extends Taxi {
    protected Set<Passenger> assigned = new HashSet<>();

    public PlannedTaxi(Simulator sim, int id, int capacity, List<Node> futurepath) {
        super(sim, id, capacity, new LinkedList<>(futurepath));
        this.initLocation();
    }

    public PlannedTaxi(Taxi taxi) {
        super(taxi);
    }

    @Override
    public Node fetchNextNode() {
        Node node = super.fetchNextNode();
        if (this.futurepath.isEmpty()) {
            this.incStandstill();
            this.futurepath = new ArrayList<>(Arrays.asList(node));
        }
        return node;
    }

    @Override
    public void loadPassenger(Passenger passenger) {
        super.loadPassenger(passenger);
        this.assigned.remove(passenger);
    }

    public boolean isAssignedTo(Passenger passenger){
        return this.assigned.contains(passenger);
    }

    public void addToAssigned(Passenger passenger) {
        this.assigned.add(passenger);
    }
}
