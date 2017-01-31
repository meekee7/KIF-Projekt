package Simulation.Factory;

import Network.Graph;
import Network.IDFactory;
import Network.Journey;
import Network.Node;
import Simulation.Entity.Passenger;
import Simulation.Simulator;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

/**
 * Created by micha on 11.12.2016.
 */
public abstract class PassengerFactory {
    protected Graph graph;
    protected Simulator simulator;
    protected IDFactory idFactory;
    protected Random random = new Random(347589345L);

    public PassengerFactory(Graph graph, Simulator simulator) {
        this.graph = graph;
        this.simulator = simulator;
        this.idFactory = new IDFactory();
    }

    public abstract Passenger createPassenger(Journey journey, int frame);

    public abstract Journey createJourney(Node start, Node end);

    public Collection<Passenger> createNewPassengers(int frame) {
        if (frame % this.simulator.getConfig().getSpawnfrequency() != 0)
            return Collections.emptyList();
        int count = (int) Math.max(this.graph.getNodes().size() * this.simulator.getConfig().getSpawnshare(), 1);

        Collection<Passenger> newpass = new ArrayList<>();

        Collection<Journey> journeys = new ConcurrentLinkedQueue<>();
        IntStream.range(0, count).parallel().forEach(x -> {
            List<Node> nodes = new ArrayList<>(this.graph.getNodes());
            Collections.shuffle(nodes, this.random);
            Node start = nodes.get(0);
            Node end = nodes.get(1);
//            int firstpoint = this.random.nextInt(this.graph.getNodes().size());
//            int secondpoint = this.random.nextInt(this.graph.getNodes().size());
//            if (firstpoint == secondpoint)
//                secondpoint = (firstpoint + 1) % this.graph.getNodes().size();
//            Node start = this.graph.getNodes().stream().skip(firstpoint).findFirst().get();
//            Node end = this.graph.getNodes().stream().skip(secondpoint).findFirst().get();
            Journey journey = this.createJourney(start, end);
            journeys.add(journey);
        });

        journeys.forEach(journey -> {
            Passenger pass = this.createPassenger(journey, frame);
            this.simulator.getLoc(journey.getStart()).getPassengers().add(pass);
            newpass.add(pass);
        });
        return newpass;
    }
}
