package Simulation.Entity;

import Network.Node;
import Simulation.Simulator;

import java.util.Collection;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;

/**
 * Created by micha on 11.12.2016.
 */
public class Taxi extends AbstractEntity {
    protected boolean broken;
    protected boolean commfaulty;
    protected Location location;
    protected Collection<Passenger> passengers;
    protected int capacity;
    protected List<Node> futurepath;
    protected int nodeshit = 0;
    protected int passengersloaded = 0;
    protected double totaldistance = 0.0;
    protected int standstill = 0;
    protected IntSummaryStatistics loadoutstats = new IntSummaryStatistics();

    protected Taxi(Simulator sim, int id, int capacity, List<Node> futurepath) {
        super(sim, id);
        this.id = id;
        this.capacity = capacity;
        this.passengers = new HashSet<>();
        this.broken = false;
        this.commfaulty = false;
        this.futurepath = futurepath;
    }

    public Taxi(Taxi taxi) {
        super(taxi.simulator, taxi.id);
        this.colour = taxi.colour;
        this.broken = taxi.broken;
        this.commfaulty = taxi.commfaulty;
        this.location = taxi.location;
        this.passengers = taxi.passengers;
        this.capacity = taxi.capacity;
        this.futurepath = taxi.futurepath;
        //TODO Clone
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        Location oldloc = this.location;
        if (location instanceof NodeLocation) {
            this.totaldistance += location.distanceTo(oldloc);
            this.nodeshit++;
        }
        this.location = location;
    }

    public int getNodeshit() {
        return nodeshit;
    }

    public int getPassengersloaded() {
        return passengersloaded;
    }

    public double getTotaldistance() {
        return totaldistance;
    }

    public IntSummaryStatistics getLoadoutstats() {
        return loadoutstats;
    }

    public void incrementLoadStats(){
        this.loadoutstats.accept(this.passengers.size());
    }

    public Collection<Passenger> getPassengers() {
        return passengers;
    }

    public void loadPassenger(Passenger passenger) {
        if (this.isFull())
            throw new IllegalStateException("Cannot load passenger into full taxi");
        this.passengers.add(passenger);
        this.passengersloaded++;
    }

    public void unloadPassenger(Passenger passenger) {
        this.passengers.remove(passenger);
    }

    public boolean isFull() {
        return this.passengers.size() == this.capacity;
    }

    public boolean isBroken() {
        return broken;
    }

    public boolean isCommfaulty() {
        return commfaulty;
    }

    public Node fetchNextNode() {
        return this.futurepath.remove(0);
    }

    public List<Node> getFuturepath() {
        return futurepath;
    }

    public void setFuturepath(List<Node> path) {
        this.futurepath = path;
    }

    protected void initLocation(){
        this.location = this.simulator.getLoc(this.fetchNextNode());
        this.nodeshit++;
    }

    public int getStandstill() {
        return standstill;
    }

    protected void incStandstill(){
        this.standstill++;
    }
}
