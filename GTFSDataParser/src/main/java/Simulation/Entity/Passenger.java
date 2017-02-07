package Simulation.Entity;

import Network.Node;
import Simulation.Simulator;

/**
 * Created by micha on 11.12.2016.
 */
public class Passenger extends AbstractEntity {
    protected int id;
    protected Node start;
    protected Node end;
    protected int createdAt;
    protected int tripBegin = -1;
    protected int tripEnd = -1;
    protected int switches = 0;
    protected double directdist = -1.0;
    protected Taxi taxi = null;
    protected int waiting = 0;
    protected int denied = 0;

    protected Passenger(Simulator simulator, int id, int frame) {
        super(simulator, id);
        this.id = id;
        this.createdAt = frame;
    }

    public Passenger(Passenger passenger) {
        super(passenger.simulator, passenger.id);
        this.id = passenger.id;
        this.colour = passenger.colour;
        this.start = passenger.start;
        this.end = passenger.end;
        this.createdAt = passenger.createdAt;
        this.tripBegin = passenger.tripBegin;
        this.tripEnd = passenger.tripEnd;
        this.switches = passenger.switches;
        this.taxi = passenger.taxi;
        this.denied = passenger.denied;
    }

    public double getHappinessIndex() {
        if (this.tripEnd == -1)
            return -1.0;
        return 1.5 * (this.tripBegin - this.createdAt)
                + (this.tripEnd - this.tripBegin) / (this.directdist * 100.0)
                + Math.pow(this.switches, 1.5);
    }

    public boolean isDelivered() {
        return this.tripEnd != -1;
    }

    public void incwaiting() {
        this.waiting++;
    }

    public boolean needsPickup() {
        return !this.isDelivered() && this.taxi == null;
    }

    public int getWaitingTime() {
        return waiting;
    }

    public int getInitPickupTime() {
        return this.tripBegin - this.createdAt;
    }

    public int getTripTime() {
        return this.tripEnd - this.tripBegin;
    }

    public void enterTaxi(Taxi taxi) {
        if (this.tripBegin == -1)
            this.tripBegin = this.simulator.getTurn();
        else
            this.switches++;
        this.taxi = taxi;
        taxi.loadPassenger(this);
        ((NodeLocation) taxi.getLocation()).getPassengers().remove(this);
    }

    public void leaveTaxi() {
        this.taxi.unloadPassenger(this);
        NodeLocation loc = (NodeLocation) this.taxi.getLocation();
        if (loc.getNode() == this.end) {
            this.tripEnd = this.simulator.getTurn();
            //if (this.switches > 0)
            //    System.out.println("Passenger " + this.getId() + " delivered with " + this.switches + " switches and happiness " + this.getHappinessIndex());
        } else
            loc.getPassengers().add(this);
        this.taxi = null;
    }

    public int getSwitches() {
        return switches;
    }

    public int getCreatedAt() {
        return createdAt;
    }

    public int getDenied() {
        return denied;
    }

    public void incDenied(){
        this.denied++;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public Taxi getTaxi() {
        return taxi;
    }
}
