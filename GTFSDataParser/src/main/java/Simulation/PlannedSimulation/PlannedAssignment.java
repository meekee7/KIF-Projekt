package Simulation.PlannedSimulation;

import Network.Graph;
import Network.Node;

import java.util.Collection;
import java.util.List;

/**
 * Created by micha on 27.01.2017.
 */
public class PlannedAssignment {
    private final PlannedPassenger passenger;
    private final PlannedTaxi taxi;
    private double inccost;
    private List<Node> newpath;


    public PlannedAssignment(PlannedPassenger passenger, PlannedTaxi taxi, List<Node> oldpath, Graph graph) {
        this.passenger = passenger;
        this.taxi = taxi;
        this.newpath = graph.integrateIntoCorePath(oldpath, passenger.getStart(), passenger.getEnd());
        this.inccost = graph.corePathLength(this.newpath);// - graph.corePathLength(oldpath);
    }

    public PlannedPassenger getPassenger() {
        return passenger;
    }

    public PlannedTaxi getTaxi() {
        return taxi;
    }

    public List<Node> getNewpath() {
        return newpath;
    }

    public double getInccost() {
        return inccost;
    }

    public void delPath(){
	this.newpath = null;
    }

    public static double totalIncCost(Collection<PlannedAssignment> assignments) {
        return assignments.stream().mapToDouble(PlannedAssignment::getInccost).sum();
    }
}
