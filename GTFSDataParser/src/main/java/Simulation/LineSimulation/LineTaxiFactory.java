package Simulation.LineSimulation;

import Network.Graph;
import Network.IDFactory;
import Network.Node;
import Simulation.Entity.NodeLocation;
import Simulation.Entity.Taxi;
import Simulation.Factory.TaxiFactory;
import Simulation.Simulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by micha on 08.01.2017.
 */
public class LineTaxiFactory extends TaxiFactory {
    protected IDFactory idFactory = new IDFactory();

    public LineTaxiFactory(Graph graph, Simulator simulator) {
        super(graph, simulator);
    }

    @Override
    public Collection<Taxi> createTaxis() {
        int step = this.simulator.getConfig().getTaxistep();
        int capacity = this.simulator.getConfig().getCapacity();
        List<Taxi> taxis = new ArrayList<>();
        this.graph.getLines().stream()
                .filter(x -> x.getStops().size() <= step)
                .forEach(x -> taxis.add(new LineTaxi(this.simulator, this.idFactory.createID(), capacity, x, new ArrayList<>(x.getStops()))));
        this.graph.getLines().stream()
                .filter(x -> x.getStops().size() > step)
                .forEach(x -> {
                    List<Node> pathforward = new ArrayList<>(x.getStops());
                    List<Node> pathbackward = new ArrayList<>(x.getStops());
                    int size = x.getStops().size();
                    Collections.reverse(pathbackward);
                    for (int i = 0; i < size; i += step) {
                        int ix = i; //Because lambdas
                        if (taxis.stream().map(y -> (LineTaxi) y).noneMatch(y -> y.getLine() == x && ((NodeLocation) y.getLocation()).getNode() == pathforward.get(ix)))
                            taxis.add(new LineTaxi(this.simulator, this.idFactory.createID(), capacity, x, pathforward.stream().skip(i).collect(Collectors.toList())));
                        if (taxis.stream().map(y -> (LineTaxi) y).noneMatch(y -> y.getLine() == x && ((NodeLocation) y.getLocation()).getNode() == pathbackward.get(ix)))
                            taxis.add(new LineTaxi(this.simulator, this.idFactory.createID(), capacity, x, pathbackward.stream().skip(i).collect(Collectors.toList())));
                    }
                });
        return taxis;
    }
}
