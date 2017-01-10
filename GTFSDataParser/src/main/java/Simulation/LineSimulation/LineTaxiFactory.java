package Simulation.LineSimulation;

import Network.Graph;
import Network.IDFactory;
import Network.Node;
import Simulation.Entity.NodeLocation;
import Simulation.Entity.Taxi;
import Simulation.Factory.TaxiFactory;
import Simulation.Simulator;

import java.util.*;
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
    public Collection<Taxi> createTaxis(int number) {
        int step = 4;
        int capacity = 8;
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
                        taxis.add(new LineTaxi(this.simulator, this.idFactory.createID(), capacity, x, pathforward.stream().skip(i).collect(Collectors.toList())));
                        taxis.add(new LineTaxi(this.simulator, this.idFactory.createID(), capacity, x, pathbackward.stream().skip(i).collect(Collectors.toList())));
                    }
                });
        return taxis;
    }
}
