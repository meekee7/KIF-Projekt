package Simulation.PlannedSimulation;

import Network.Graph;
import Network.Node;
import Simulation.Entity.EdgeLocation;
import Simulation.Entity.NodeLocation;
import Simulation.Entity.Passenger;
import Simulation.Factory.EmptyFaultFactory;
import Simulation.SimulationConfig;
import Simulation.Simulator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Created by micha on 20.01.2017.
 */
public class PlannedSimulator extends Simulator {
    public PlannedSimulator(Graph graph, SimulationConfig config) {
        super(config);
        this.graph = graph;
        this.createLocmap();
        this.faultFactory = new EmptyFaultFactory(this.graph);
        this.passengerFactory = new PlannedPassengerFactory(this.graph, this);
        this.taxis = new PlannedTaxiFactory(this.graph, this).createTaxis();
        this.passengers = new HashSet<>();


        for (int i = 0; i < 5; i++)
            this.passengers.addAll(this.passengerFactory.createNewPassengers(0));

        System.out.println("Taxis: " + this.taxis.size());
        System.out.println("Passengers: " + this.passengers.size());
        System.out.println("Init complete");
    }

    @Override
    protected void advanceOneTurn() {
        this.planNextPaths();
        this.taxis.stream().filter(x -> x.getLocation() instanceof NodeLocation)
                .map(x -> (PlannedTaxi) x).forEach(taxi -> {
            NodeLocation loc = (NodeLocation) taxi.getLocation();
            //Unload passengers
            List<Passenger> toleave = taxi.getPassengers().stream().map(x -> (PlannedPassenger) x)
                    .filter(x -> x.getEnd() == loc.getNode())
                    .collect(Collectors.toList());
            toleave.forEach(Passenger::leaveTaxi);

            //Load passengers
            List<Passenger> waiting = loc.getPassengers().stream()
                    .map(x -> (PlannedPassenger) x)
                    .filter(Passenger::needsPickup)
                    .filter(x -> taxi.getFuturepath().contains(x.getEnd()))
                    .collect(Collectors.toList());
            while (!taxi.isFull() && !waiting.isEmpty())
                waiting.remove(0).enterTaxi(taxi);
            waiting.forEach(Passenger::incDenied);
        });
        this.taxis.stream().filter(x -> x.getLocation() instanceof NodeLocation).forEach(taxi -> {
            Node start = ((NodeLocation) taxi.getLocation()).getNode();
            Node end = taxi.fetchNextNode();
            if (end != null)
                taxi.setLocation(new EdgeLocation(this.getLoc(start), this.getLoc(end), 0.00));
            else
                ((PlannedTaxi) taxi).incStandstill();
        });
        this.taxis.stream().filter(x -> x.getLocation() instanceof EdgeLocation).forEach(taxi ->
                taxi.setLocation(((EdgeLocation) taxi.getLocation()).advance(this.movementspeed)));
    }

    protected void planNextPaths() {
        Collection<Assignment> assignments = new ConcurrentLinkedQueue<>();
        this.passengers.stream()
                .filter(Passenger::needsPickup)
                .map(p -> (PlannedPassenger) p)
                .forEach(p ->
                        this.taxis.stream()
                                .filter(t -> !t.isFull())
                                .map(t -> (PlannedTaxi) t)
                                .forEach(t -> assignments.add(new Assignment(p, t, this.graph)))
                );
        System.out.println(assignments.size());
    }
}
