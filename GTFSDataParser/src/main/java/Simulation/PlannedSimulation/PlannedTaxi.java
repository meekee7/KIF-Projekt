package Simulation.PlannedSimulation;

import Network.Node;
import Simulation.Entity.Taxi;
import Simulation.Simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by micha on 20.01.2017.
 */
public class PlannedTaxi extends Taxi {
    protected int standstill = 0;

    public PlannedTaxi(Simulator sim, int id, int capacity, List<Node> futurepath) {
        super(sim, id, capacity, new LinkedList<>(futurepath));
        this.initLocation();
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

    @Override
    public Node fetchNextNode() {
        Node node = super.fetchNextNode();
        if (this.futurepath.isEmpty()){
            this.incStandstill();
            this.futurepath = new ArrayList<>(Arrays.asList(node));
        }
        return node;
    }
}
