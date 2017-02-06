package Simulation.PlannedSimulation;

import Network.Graph;
import Network.Node;
import Simulation.Entity.EdgeLocation;
import Simulation.Entity.NodeLocation;
import Simulation.Entity.Passenger;
import Simulation.Factory.EmptyFaultFactory;
import Simulation.SimulationConfig;
import Simulation.Simulator;

import java.time.Duration;
import java.time.Instant;
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
        if (this.turn % this.config.getCalcstep() == 0)
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
//                    .filter(x -> taxi.getFuturepath().contains(x.getEnd()))
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

//        Collection<Assignment> allassignments = new ArrayList<>(passtoassign.size() * freetaxis.size());
//        passtoassign.forEach(p ->
//                freetaxis.forEach(t ->
//                        allassignments.add(new Assignment(p, t, this.graph))));


        Random random = new Random(9354084610997234L);
        List<Thread> threadpool = new ArrayList<>(8);
        AtomicBoolean foundzerosolution = new AtomicBoolean(false);
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            threadpool.add(new Thread(() -> {
                Instant start = Instant.now();
                long calctime = this.config.getMaxcalctime();
                while (!foundzerosolution.get() && Duration.between(start, Instant.now()).minus(Duration.ofMillis(calctime)).isNegative()) {
                    Collection<Assignment> association = new ArrayList<>(passtoassign.size());
                    Map<PlannedTaxi, AtomicInteger> restcapacity = new HashMap<>(freetaxis.size());
                    Map<PlannedTaxi, List<Node>> newpath = new HashMap<>(freetaxis.size());
//                    freetaxis.forEach(t -> restcapacity.put(t, new AtomicInteger(1)));
                    freetaxis.forEach(t -> restcapacity.put(t, new AtomicInteger(this.config.getCapacity() - t.getPassengersloaded())));
                    freetaxis.forEach(t -> newpath.put(t, t.getCorepath()));
                    Set<PlannedTaxi> taxiset = new HashSet<>(freetaxis);
                    List<PlannedPassenger> passset = new LinkedList<>(passtoassign);

                    while (!taxiset.isEmpty() && !passset.isEmpty()) {
                        PlannedTaxi taxi = taxiset.stream().skip(random.nextInt(taxiset.size())).findFirst().get();
                        PlannedPassenger pass = passset.remove(0); //passset.stream().skip(random.nextInt(passset.size())).findFirst().get();
                        //passset.remove(pass);
//                        Assignment assignment = allassignments.stream()
//                                .filter(asgnmnt -> asgnmnt.getPassenger() == pass && asgnmnt.getTaxi() == taxi)
//                                .findFirst().get();
                        Assignment assignment = new Assignment(pass, taxi, newpath.get(taxi), this.graph);
                        association.add(assignment);
                        newpath.put(taxi, assignment.getNewpath());
			assignment.delPath();
                        if (restcapacity.get(taxi).decrementAndGet() == 0)
                            taxiset.remove(taxi);
                    }
                    allassociations.add(association);
                    if (Assignment.totalIncCost(association) == 0.0)
                        foundzerosolution.set(true);

                }
            }));
        }
        threadpool.forEach(Thread::start);
        try {
            for (Thread thread : threadpool)
                thread.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }


        Collection<Assignment> result = allassociations.stream().min(Comparator.comparingDouble(Assignment::totalIncCost)).get();
//        result.forEach(x -> x.getTaxi().setCorepath(x.getNewpath()));// setFuturepath(this.graph.integrateIntoPath(x.getTaxi().getFuturepath(), x.getPassenger().getStart(), x.getPassenger().getEnd())));// x.getNewpath()));
        result.forEach(x -> x.getTaxi().setCorepath(this.getGraph().integrateIntoCorePath(x.getTaxi().getCorepath(), x.getPassenger().getStart(), x.getPassenger().getEnd())));
        result.forEach(x -> x.getTaxi().addToAssigned(x.getPassenger()));
        result.forEach(x -> x.getPassenger().markAssigned());
      /*
        this.taxis.stream().map(t -> (PlannedTaxi) t).forEach(t -> {
            Set<Node> corenodes = new HashSet<>();
            t.getAssigned().forEach(p -> corenodes.add(p.getStart()));
            t.getAssigned().forEach(p -> corenodes.add(p.getEnd()));
            t.getPassengers().forEach(p -> corenodes.add(p.getStart()));
            t.getPassengers().forEach(p -> corenodes.add(p.getEnd()));
            t.setCorepath(t.getFuturepath().stream().filter(corenodes::contains).collect(Collectors.toList()));
            if (t.getCorepath().get(0) != t.getFuturepath().get(0))
                t.getCorepath().add(0, t.getFuturepath().get(0));
        });
        */

        System.out.println("Turn: " + this.turn + " | "
//                + "Allassignments: " + allassignments.size() + " | "
                        + allassociations.stream().mapToDouble(Assignment::totalIncCost).summaryStatistics()
                        + " | Unassigned " + this.passengers.stream().map(x->(PlannedPassenger)x).filter(x->!x.isAssigned()).count()
        );
    }
}
