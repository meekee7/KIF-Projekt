package Simulation.Factory;

import Network.Graph;
import Network.Node;
import Simulation.Entity.Taxi;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by micha on 08.01.2017.
 */
public class EmptyFaultFactory extends FaultFactory {
    public EmptyFaultFactory(Graph graph) {
        super(graph);
    }

    @Override
    public Collection<Node> createFaultyNodes() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Node> createRepairedNodes() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Taxi> createFaultyTaxis() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Taxi> createRepairedTaxis() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Taxi> createCommFaultTaxis() {
        return Collections.emptyList();
    }
}
