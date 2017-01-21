package Simulation;

import Network.Graph;
import Simulation.Entity.Passenger;
import Simulation.Entity.Taxi;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by micha on 11.12.2016.
 */
public class Frame {
    protected final int number;
    protected final Graph graph;
    protected final Collection<Taxi> taxis;
    protected final Collection<Passenger> passengers;

    public Frame(int number, Graph graph, Collection<Taxi> taxis, Collection<Passenger> passengers) {
        this.number = number;
        this.graph = graph;
        this.taxis = taxis.stream().map(Taxi::new).collect(Collectors.toList());
        this.passengers = passengers.stream().map(Passenger::new).collect(Collectors.toList());
    }
}
