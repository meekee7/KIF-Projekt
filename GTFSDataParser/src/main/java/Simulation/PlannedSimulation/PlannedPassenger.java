package Simulation.PlannedSimulation;

import Network.Node;
import Simulation.Entity.Passenger;
import Simulation.Simulator;

/**
 * Created by micha on 20.01.2017.
 */
public class PlannedPassenger extends Passenger {
    protected Node start;
    protected Node end;

    public PlannedPassenger(Simulator simulator, int id, int frame, Node start, Node end) {
        super(simulator, id, frame);
        this.start = start;
        this.end = end;
    }

    public PlannedPassenger(PlannedPassenger passenger) {
        super(passenger);
        this.start = passenger.start;
        this.end = passenger.end;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }
}