package Simulation.PlannedSimulation;

import Network.Graph;
import Network.Node;
import Simulation.Entity.Taxi;
import Simulation.Factory.TaxiFactory;
import Simulation.Simulator;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Created by micha on 20.01.2017.
 */
public class PlannedTaxiFactory extends TaxiFactory {
    private int capacity = 8;

    public PlannedTaxiFactory(Graph graph, Simulator simulator) {
        super(graph, simulator);
    }

    protected PlannedTaxi createTaxiAt(Node node) {
        return new PlannedTaxi(this.simulator, this.idfactory.createID(), this.capacity, Collections.singletonList(node));
    }

    protected Collection<Taxi> distributeAcrossNodes(Collection<Node> nodes, boolean northsouth, int number) {
        if (nodes.size() < number)
            throw new IllegalArgumentException("Num nodes (" + nodes.size() + ") < number (" + number + ")");
        if (nodes.size() == number)
            return nodes.stream().map(this::createTaxiAt).collect(Collectors.toList());
        if (number == 0)
            return Collections.emptyList();
        if (number == 1)
            return Collections.singletonList(this.createTaxiAt(nodes.stream().findAny().get()));
        ToDoubleFunction<Node> getdim = northsouth ? Node::getLat : Node::getLon;
//        double center = nodes.stream().mapToDouble(getdim).average().getAsDouble();
        List<Node> sorted = nodes.stream().sorted(Comparator.comparingDouble(getdim)).collect(Collectors.toList());
        Collection<Taxi> left = this.distributeAcrossNodes(sorted.subList(0, sorted.size() / 2), !northsouth, number / 2 + number % 2);
        Collection<Taxi> right = this.distributeAcrossNodes(sorted.subList(sorted.size() / 2, sorted.size()), !northsouth, number / 2);

        ArrayList<Taxi> result = new ArrayList<>(number);
        result.addAll(left);
        result.addAll(right);
        return result;
    }

    @Override
    public Collection<Taxi> createTaxis() {
        return new HashSet<>(this.distributeAcrossNodes(this.graph.getNodes(), true, (int) Math.max(this.graph.getNodes().size() * this.simulator.getConfig().getTaxirate(), 1)));
    }
}
