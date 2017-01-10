package Simulation.LineSimulation;

import Network.Graph;
import Network.Node;
import Simulation.Entity.EdgeLocation;
import Simulation.Entity.Location;
import Simulation.Entity.NodeLocation;
import Simulation.Entity.Passenger;
import Simulation.Factory.EmptyFaultFactory;
import Simulation.Simulator;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by micha on 01.01.2017.
 */
public class LineSimulator extends Simulator {
    public LineSimulator(Graph graph) {
        this.graph = graph;
        this.createLocmap();
        this.faultFactory = new EmptyFaultFactory(this.graph);
        this.passengerFactory = new LinePassengerFactory(this.graph, this);
        this.taxis = new LineTaxiFactory(this.graph, this).createTaxis(0);
        this.passengers = new HashSet<>();


        for (int i = 0; i < 5; i++)
            this.passengers.addAll(this.passengerFactory.createNewPassengers(0));

        System.out.println("Taxis: " + this.taxis.size());
        System.out.println("Passengers: " + this.passengers.size());
        System.out.println("Init complete");
    }

    @Override
    protected void advanceOneTurn() {
        this.taxis.stream().filter(x -> x.getLocation() instanceof NodeLocation)
                .map(x -> (LineTaxi) x).forEach(taxi -> {
            NodeLocation loc = (NodeLocation) taxi.getLocation();
            //Unload passengers
            List<Passenger> toleave = taxi.getPassengers().stream().map(x -> (LinePassenger) x)
                    .filter(x -> x.getNextDestination() == loc.getNode())
                    .collect(Collectors.toList());
            toleave.forEach(Passenger::leaveTaxi);

            //Load passengers
            List<Passenger> waiting = loc.getPassengers().stream()
                    .map(x -> (LinePassenger) x)
                    .filter(x -> x.getNextLine() == taxi.getLine() && x.getNextLineEnd() == taxi.getLineEndStop())
                    .collect(Collectors.toList());
            while (!taxi.isFull() && !waiting.isEmpty())
                waiting.remove(0).enterTaxi(taxi);
        });
        this.taxis.stream().filter(x -> x.getLocation() instanceof NodeLocation).forEach(taxi -> {
            Node start = ((NodeLocation) taxi.getLocation()).getNode();
            Node end = taxi.fetchNextNode();
            taxi.setLocation(new EdgeLocation(this.getLoc(start), this.getLoc(end), 0.00));
        });
        this.taxis.stream().filter(x -> x.getLocation() instanceof EdgeLocation).forEach(taxi ->
                taxi.setLocation(((EdgeLocation) taxi.getLocation()).advance(this.movementspeed)));

        //this.taxis.stream().filter(x -> x.getLocation() instanceof EdgeLocation).forEach(taxi ->
        //        System.out.println("Taxi " + taxi.getId() + " has prog " + ((EdgeLocation)taxi.getLocation()).getProgress()));
        //this.taxis.stream().filter(x -> x.getLocation() instanceof NodeLocation).forEach(x ->
        //        System.out.println("Taxi " + x.getId() + " at " + ((NodeLocation) x.getLocation()).getNode().getName()));
    }
}
