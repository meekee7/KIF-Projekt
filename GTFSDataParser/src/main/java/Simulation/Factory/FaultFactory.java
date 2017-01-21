package Simulation.Factory;

import Network.Graph;
import Network.Node;
import Simulation.Entity.Taxi;

import java.util.Collection;
import java.util.Random;

/**
 * Created by micha on 12.12.2016.
 */
public abstract class FaultFactory {
    protected Graph graph;
    protected Collection<Node> faultynodes;
    protected Collection<Taxi> faultytaxis;
    protected Collection<Taxi> commfaulttaxis;
    protected Random random = new Random(9876456986L);

    public FaultFactory(Graph graph) {
        this.graph = graph;
    }

    public abstract Collection<Node> createFaultyNodes();

    public abstract Collection<Node> createRepairedNodes();

    public abstract Collection<Taxi> createFaultyTaxis();

    public abstract Collection<Taxi> createRepairedTaxis();

    public abstract Collection<Taxi> createCommFaultTaxis();
}
