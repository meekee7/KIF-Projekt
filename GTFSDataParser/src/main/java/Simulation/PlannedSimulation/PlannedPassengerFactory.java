package Simulation.PlannedSimulation;

import Network.Graph;
import Network.Journey;
import Network.Node;
import Simulation.Entity.Passenger;
import Simulation.Factory.PassengerFactory;
import Simulation.Simulator;

/**
 * Created by micha on 20.01.2017.
 */
public class PlannedPassengerFactory extends PassengerFactory {
    public PlannedPassengerFactory(Graph graph, Simulator simulator) {
        super(graph, simulator);
    }

    @Override
    public Passenger createPassenger(Journey journey, int frame) {
        return new PlannedPassenger(this.simulator, this.idFactory.createID(), frame, journey.getStart(), journey.getEnd());
    }

    @Override
    public Journey createJourney(Node start, Node end) {
        return new Journey(start, end, null);
    }
}
