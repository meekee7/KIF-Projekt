package Simulation.LineSimulation;

import Network.Graph;
import Network.Line;
import Network.Node;
import Simulation.Entity.EdgeLocation;
import Simulation.Entity.NodeLocation;
import Simulation.Entity.Passenger;
import Simulation.Factory.EmptyFaultFactory;
import Simulation.SimulationConfig;
import Simulation.Simulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by micha on 01.01.2017.
 */
public class LineSimulator extends Simulator {
    public Map<Line, AtomicInteger> denialmap;

    public LineSimulator(Graph graph, SimulationConfig config) {
        super(config);
        this.graph = graph;
        this.createLocmap();
        this.faultFactory = new EmptyFaultFactory(this.graph);
        this.passengerFactory = new LinePassengerFactory(this.graph, this);
        this.taxis = new LineTaxiFactory(this.graph, this).createTaxis();
        this.passengers = new HashSet<>();
        this.denialmap = new HashMap<>();
        this.graph.getLines().forEach(x -> denialmap.put(x, new AtomicInteger(0)));

        for (int i = 0; i < 5; i++)
            this.passengers.addAll(this.passengerFactory.createNewPassengers(0));

        System.out.println("Taxis: " + this.taxis.size());
        System.out.println("Passengers: " + this.passengers.size());
        System.out.println("Init complete");
       /*
        System.out.println(taxis.stream().filter(x -> {
            Optional<Taxi> t =
            taxis.stream().filter(y ->
                y.getLocation().equals(x.getLocation())).findAny();
            boolean ismatch = t.isPresent() && t.get() != x && ((LineTaxi) t.get()).getLine() == ((LineTaxi) x).getLine();
            if (ismatch)
                System.out.println(t.get().getLocation().getPointAWT() + " " + ((LineTaxi) t.get()).getLine().getId() + " VS " + x.getLocation().getPointAWT() + " " + ((LineTaxi) x).getLine().getId());
            return  ismatch;
            }).count());
            */
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
                    .filter(Passenger::needsPickup)
                    .filter(x -> x.getNextLine() == taxi.getLine() && x.getNextLineEnd() == taxi.getLineEndStop())
                    .collect(Collectors.toList());
            while (!taxi.isFull() && !waiting.isEmpty())
                waiting.remove(0).enterTaxi(taxi);
            waiting.forEach(Passenger::incDenied);
            waiting.forEach(x->denialmap.get(taxi.getLine()).incrementAndGet());
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
