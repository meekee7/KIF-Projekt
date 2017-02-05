package Simulation.LineSimulation;

import Network.Journey;
import Network.Line;
import Network.Node;
import Simulation.Entity.Passenger;
import Simulation.Simulator;

/**
 * Created by micha on 02.01.2017.
 */
public class LinePassenger extends Passenger {
    protected Journey journey;
    protected Journey.Step currstep;

    public LinePassenger(LinePassenger lp) {
        super(lp); //TODO COPY
    }

    public LinePassenger(Simulator simulator, int id, int frame, Journey journey){
        super(simulator, id, frame);
        this.journey = journey;
        this.start = journey.getStart();
        this.end = journey.getEnd();
        this.currstep = this.journey.getSteps().poll();
//        this.simulator.getLoc(this.start).getPassengers().add(this);
        this.directdist = this.journey.getStart().getDistance(this.journey.getEnd());
    }

    public Line getNextLine() {
        return this.currstep.getLine();
    }

    public Node getNextLineEnd() {
        return this.currstep.getLineEnd();
    }

    public Node getNextDestination() {
        return this.currstep.getEnd();
    }

    @Override
    public void leaveTaxi() {
        super.leaveTaxi();
        this.currstep = this.journey.getSteps().poll();
    }
}
