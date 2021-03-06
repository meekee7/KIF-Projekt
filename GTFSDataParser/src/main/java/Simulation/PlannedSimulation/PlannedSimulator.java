package Simulation.PlannedSimulation;

import Network.Graph;
import Network.Node;
import Simulation.Entity.EdgeLocation;
import Simulation.Entity.NodeLocation;
import Simulation.Entity.Passenger;
import Simulation.Factory.EmptyFaultFactory;
import Simulation.SimulationConfig;
import Simulation.Simulator;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

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
    SolverFactory<Opta.Assignment> solverFactory = null;

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

        String solverconfig =
                "taxiAssignmentSolverConfig.xml";
        this.solverFactory = SolverFactory.createFromXmlResource(
                solverconfig);




        System.out.println("Taxis: " + this.taxis.size());
        System.out.println("Passengers: " + this.passengers.size());
        System.out.println("Init complete");
    }

    @Override
    protected void advanceOneTurn() {
        if (this.turn % this.config.getCalcstep() == 0)
            this.planNextPathsOpta();

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

    protected void planNextPathsOpta() {
        Solver<Opta.Assignment> solver = this.solverFactory.buildSolver();
        Opta.Assignment assignment = new Opta.Assignment();
        assignment.setId(0L);
        assignment.setPassengerList(this.passengers.stream()
                .map(x -> (PlannedPassenger) x)
                .filter(Passenger::needsPickup)
                .filter(x -> !x.isAssigned())
                .map(PlannedPassenger::toOpta)
                .collect(Collectors.toList())
        );
        assignment.setTaxiList(this.taxis.stream()
                .map(x -> (PlannedTaxi) x)
                .filter(x -> !x.isFull())
                .map(PlannedTaxi::toOpta).collect(Collectors.toList())
        );
        if (assignment.getTaxiList().isEmpty()){
            System.out.println("Turn: " + this.turn + " | no new assignments");
            return;
        }
        Opta.Assignment solution = solver.solve(assignment);

        Map<Integer, PlannedTaxi> taximap = new HashMap<>();
        this.taxis.stream().map(x -> (PlannedTaxi) x).forEach(x -> taximap.put(x.getId(), x));

        Map<Integer, PlannedPassenger> passmap = new HashMap<>();
        this.passengers.stream().map(x -> (PlannedPassenger) x).forEach(x -> passmap.put(x.getId(), x));

        solution.getPassengerList().stream().filter(x -> x.getTaxi() != null).forEach(op -> {
            PlannedPassenger p = passmap.get(op.getId());
            p.markAssigned();
            PlannedTaxi taxi = taximap.get((int) op.getTaxi().getId());
            taxi.addToAssigned(p);
            taxi.setCorepath(this.graph.integrateIntoCorePath(taxi.getCorepath(), p.getStart(), p.getEnd()));
        });

        System.out.println("Turn: " + this.turn
//                + "Allassignments: " + allassignments.size() + " | "
//                        + " | Asgn " + allassociations.stream().mapToDouble(PlannedAssignment::totalIncCost).summaryStatistics().toString().replace("DoubleSummaryStatistics", "")
                        // + " | Unsg " + this.passengers.stream().map(x -> (PlannedPassenger) x).filter(x -> !x.isAssigned()).count()
                        + " | CoPa " + this.taxis.stream().map(x -> (PlannedTaxi) x).mapToInt(x -> x.corepath.size()).summaryStatistics().toString().replace("IntSummaryStatistics", "")
        );
    }


    protected void planNextPathsBrute() {

        Collection<Collection<PlannedAssignment>> allassociations = new ConcurrentLinkedQueue<>();

        if (this.turn % this.config.getClearing() == 0) {
            this.taxis.stream().map(x -> (PlannedTaxi) x).forEach(PlannedTaxi::streamlineAssignments);
            this.passengers.stream().filter(Passenger::needsPickup).map(x -> (PlannedPassenger) x).forEach(PlannedPassenger::markUnassigned);
        }

        List<PlannedPassenger> passtoassign = this.passengers.stream()
                .filter(Passenger::needsPickup)
                .map(p -> (PlannedPassenger) p)
                .filter(p -> !p.isAssigned())
                .collect(Collectors.toList());
        List<PlannedTaxi> freetaxis = this.taxis.stream()
                .filter(t -> !t.isFull())
                .map(p -> (PlannedTaxi) p)
		.filter(t-> t.getAssigned().size() < this.config.getCapacity())
                .collect(Collectors.toList());
//        Collection<PlannedAssignment> allassignments = new ArrayList<>(passtoassign.size() * freetaxis.size());
//        passtoassign.forEach(p ->
//                freetaxis.forEach(t ->
//                        allassignments.add(new PlannedAssignment(p, t, this.graph))));

        Random random = new Random(9354084610997234L);
        List<Thread> threadpool = new ArrayList<>(8);
        AtomicBoolean foundzerosolution = new AtomicBoolean(false);
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            threadpool.add(new Thread(() -> {
                Instant start = Instant.now();
                long calctime = this.config.getMaxcalctime();
                while (!foundzerosolution.get() && Duration.between(start, Instant.now()).minus(Duration.ofMillis(calctime)).isNegative()) {
                    Collection<PlannedAssignment> association = new ArrayList<>(passtoassign.size());
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
//                        PlannedAssignment assignment = allassignments.stream()
//                                .filter(asgnmnt -> asgnmnt.getPassenger() == pass && asgnmnt.getTaxi() == taxi)
//                                .findFirst().get();
                        PlannedAssignment assignment = new PlannedAssignment(pass, taxi, newpath.get(taxi), this.graph);
                        association.add(assignment);
                        newpath.put(taxi, assignment.getNewpath());
                        assignment.delPath();
                        if (restcapacity.get(taxi).decrementAndGet() == 0)
                            taxiset.remove(taxi);
                    }
                    allassociations.add(association);
                    if (PlannedAssignment.totalIncCost(association) == 0.0)
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


        Collection<PlannedAssignment> result = allassociations.stream().min(Comparator.comparingDouble(PlannedAssignment::totalIncCost)).get();
        result.forEach(x -> x.getTaxi().setCorepath(this.getGraph().integrateIntoCorePath(x.getTaxi().getCorepath(), x.getPassenger().getStart(), x.getPassenger().getEnd())));
        result.forEach(x -> x.getTaxi().addToAssigned(x.getPassenger()));
        result.forEach(x -> x.getPassenger().markAssigned());

        if (this.turn > 100) {
            List<PlannedTaxi> stopped = this.taxis.stream().map(x -> (PlannedTaxi) x).filter(x -> x.corepath.size() == 0).collect(Collectors.toList());
            if (!stopped.isEmpty())
                System.out.println("HERE");
        }

        System.out.println("Turn: " + this.turn
//                + "Allassignments: " + allassignments.size() + " | "
                        + " | Asgn " + allassociations.stream().mapToDouble(PlannedAssignment::totalIncCost).summaryStatistics().toString().replace("DoubleSummaryStatistics", "")
                        // + " | Unsg " + this.passengers.stream().map(x -> (PlannedPassenger) x).filter(x -> !x.isAssigned()).count()
                        + " | CoPa " + this.taxis.stream().map(x -> (PlannedTaxi) x).mapToInt(x -> x.corepath.size()).summaryStatistics().toString().replace("IntSummaryStatistics", "")
			+ " | Locs " + this.taxis.stream().map(x -> (PlannedTaxi) x).filter(x->x.corepath.size() == 0).map(PlannedTaxi::getLocation).map(x->x.getClass().getSimpleName()).collect(Collectors.toList())
        );
    }

}
