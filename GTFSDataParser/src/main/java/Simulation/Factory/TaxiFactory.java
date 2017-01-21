package Simulation.Factory;

import Network.Graph;
import Network.IDFactory;
import Simulation.Entity.Taxi;
import Simulation.Simulator;

import java.util.Collection;
import java.util.Random;

/**
 * Created by micha on 12.12.2016.
 */
public abstract class TaxiFactory {
    protected Random random = new Random(31484763L);
    protected Graph graph;
    protected IDFactory idfactory;
    protected Simulator simulator;

    public TaxiFactory(Graph graph, Simulator simulator) {
        this.graph = graph;
        this.idfactory = new IDFactory();
        this.simulator = simulator;
    }

    public abstract Collection<Taxi> createTaxis();
}
