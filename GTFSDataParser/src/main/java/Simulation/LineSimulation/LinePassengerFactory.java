package Simulation.LineSimulation;

import Network.Graph;
import Network.Journey;
import Network.Node;
import Simulation.Entity.NodeLocation;
import Simulation.Entity.Passenger;
import Simulation.Factory.PassengerFactory;
import Simulation.Simulator;

import java.util.*;

/**
 * Created by micha on 08.01.2017.
 */
public class LinePassengerFactory extends PassengerFactory {
    protected int end = 10000;

    public LinePassengerFactory(Graph graph, Simulator simulator) {
        super(graph, simulator);
    }

    @Override
    public Collection<Passenger> createNewPassengers(int frame) {
        if (frame > this.end)
            return Collections.emptyList();
        if (frame % 1 != 0)
            return Collections.emptyList();
        int count = Math.max(this.graph.getNodes().size() / 10, 3);
        List<Node> nodes = new ArrayList<>(this.graph.getNodes());
        Collection<Passenger> newpass = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Collections.shuffle(nodes, this.random);
            Node start = nodes.get(0);
            Node end = nodes.get(1);
            Journey journey = this.graph.getJourney(start, end, 0.0);
            Passenger pass = new LinePassenger(this.simulator, this.idFactory.createID(), frame, journey);
            this.simulator.getLoc(start).getPassengers().add(pass);
            newpass.add(pass);
        }
        return newpass;
    }
}
