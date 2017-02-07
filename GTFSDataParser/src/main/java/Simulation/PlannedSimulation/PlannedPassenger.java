package Simulation.PlannedSimulation;

import Network.Node;
import Opta.OptaPassenger;
import Simulation.Entity.Passenger;
import Simulation.Simulator;

/**
 * Created by micha on 20.01.2017.
 */
public class PlannedPassenger extends Passenger {
    protected boolean assigned = false;

    public PlannedPassenger(Simulator simulator, int id, int frame, Node start, Node end) {
        super(simulator, id, frame);
        this.start = start;
        this.end = end;
        this.directdist = this.getStart().getDistance(this.getEnd());
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

    public boolean isAssigned() {
        return assigned;
    }

    public void markAssigned(){
        this.assigned = true;
    }

    public void markUnassigned(){this.assigned = false;}

    public OptaPassenger toOpta(){
        return new OptaPassenger(this.getStart(), this.getEnd(), this.id);
    }

    /*
    @Override
    public boolean needsPickup() {
        return super.needsPickup() && !this.isAssigned();
    }
    */
}
