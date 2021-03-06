package Simulation.LineSimulation;

import Network.Graph;
import Network.Journey;
import Network.Node;
import Simulation.Entity.Passenger;
import Simulation.Factory.PassengerFactory;
import Simulation.Simulator;

/**
 * Created by micha on 08.01.2017.
 */
public class LinePassengerFactory extends PassengerFactory {
    public LinePassengerFactory(Graph graph, Simulator simulator) {
        super(graph, simulator);
    }

    @Override
    public Passenger createPassenger(Journey journey, int frame) {
        return new LinePassenger(this.simulator, this.idFactory.createID(), frame, journey);
    }

    @Override
    public Journey createJourney(Node start, Node end) {
        return this.graph.getJourney(start, end, 0.0);
    }
}
