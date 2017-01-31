package Simulation.PlannedSimulation;

import Network.Graph;
import Network.Node;
import Simulation.Entity.EdgeLocation;
import Simulation.Entity.NodeLocation;
import Simulation.Entity.Passenger;
import Simulation.Factory.EmptyFaultFactory;
import Simulation.SimulationConfig;
import Simulation.Simulator;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
//                    .filter(Passenger::needsPickup)
                    .filter(taxi::isAssignedTo)
                    .filter(x -> taxi.getFuturepath().contains(x.getEnd()))
                    .collect(Collectors.toList());
            while (!taxi.isFull() && !waiting.isEmpty())
                waiting.remove(0).enterTaxi(taxi);
            waiting.forEach(Passenger::incDenied);
        });
        this.taxis.stream().filter(x -> x.getLocation() instanceof NodeLocation).forEach(taxi -> {
            Node start = ((NodeLocation) taxi.getLocation()).getNode();
            Node end = taxi.fetchNextNode();
            if (start != end)
                taxi.setLocation(new EdgeLocation(this.getLoc(start), this.getLoc(end), 0.00));
        });
        this.taxis.stream().filter(x -> x.getLocation() instanceof EdgeLocation).forEach(taxi ->
                taxi.setLocation(((EdgeLocation) taxi.getLocation()).advance(this.movementspeed)));
    }

    protected void planNextPaths() {
        Collection<Collection<Assignment>> allassociations = new ConcurrentLinkedQueue<>();
        List<PlannedPassenger> passtoassign = this.passengers.stream()
                .filter(Passenger::needsPickup)
                .map(p -> (PlannedPassenger) p)
                .collect(Collectors.toList());
        List<PlannedTaxi> freetaxis = this.taxis.stream()
                .filter(t -> !t.isFull())
                .map(p -> (PlannedTaxi) p)
                .collect(Collectors.toList());
        Random random = new Random(9354084610997234L);
        //for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
        List<Thread> threadpool = new ArrayList<>(8);
        AtomicBoolean foundzerosolution = new AtomicBoolean(false);
        for (int i = 0; i < 50; i++) {
            //threadpool.add(new Thread(() -> {
            //Instant start = Instant.now();
//            while (!foundzerosolution.get() && Duration.between(start, Instant.now()).minus(Duration.ofMillis(200L)).isNegative()) {
            Collection<Assignment> association = new ArrayList<>(passtoassign.size());
            Map<PlannedTaxi, AtomicInteger> map = new HashMap<>(freetaxis.size());
            freetaxis.forEach(t -> map.put(t, new AtomicInteger(this.config.getCapacity() - t.getPassengersloaded())));
            Set<PlannedTaxi> taxiset = new HashSet<>(freetaxis);
            Set<PlannedPassenger> passset = new HashSet<>(passtoassign);

            while (!taxiset.isEmpty() && !passset.isEmpty()) {
                PlannedTaxi taxi = taxiset.stream().skip(random.nextInt(taxiset.size())).findFirst().get();
                PlannedPassenger pass = passset.stream().skip(random.nextInt(passset.size())).findFirst().get();
                passset.remove(pass);
                Assignment assignment = new Assignment(pass, taxi, this.graph);
                association.add(assignment);
                if (map.get(taxi).decrementAndGet() == 0)
                    taxiset.remove(taxi);
            }
            allassociations.add(association);
            if (Assignment.totalIncCost(association) == 0.0) {
                foundzerosolution.set(true);
                break;
            }
            //}
            //   }));
        }
        /*
        threadpool.forEach(Thread::start);
        try {
            for (Thread thread: threadpool)
                thread.join();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        */
        System.out.println(allassociations.stream().mapToDouble(Assignment::totalIncCost).summaryStatistics());
        Collection<Assignment> result = allassociations.stream().min(Comparator.comparingDouble(Assignment::totalIncCost)).get();
        result.forEach(x -> x.getTaxi().setFuturepath(this.graph.integrateIntoPath(x.getTaxi().getFuturepath(), x.getPassenger().getStart(), x.getPassenger().getEnd())));// x.getNewpath()));
        result.forEach(x -> x.getTaxi().addToAssigned(x.getPassenger()));
        result.forEach(x -> x.getPassenger().markAssigned());
    }
}
