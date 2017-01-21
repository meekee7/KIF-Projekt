package Simulation.PlannedSimulation;

import Network.Node;
import Simulation.Entity.Taxi;
import Simulation.Simulator;

import java.util.List;

/**
 * Created by micha on 20.01.2017.
 */
public class PlannedTaxi extends Taxi {
    protected int standstill = 0;

    public PlannedTaxi(Simulator sim, int id, int capacity, List<Node> futurepath) {
        super(sim, id, capacity, futurepath);
    }

    public PlannedTaxi(Taxi taxi) {
        super(taxi);
    }

    public int getStandstill() {
        return standstill;
    }

    public void incStandstill() {
        this.standstill++;
    }
}
