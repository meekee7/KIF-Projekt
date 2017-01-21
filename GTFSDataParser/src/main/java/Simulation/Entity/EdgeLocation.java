package Simulation.Entity;

import Network.Utils.Edge;

/**
 * Created by micha on 12.12.2016.
 */
public class EdgeLocation extends Location {
    protected NodeLocation start;
    protected NodeLocation end;
    protected double progress;

    public EdgeLocation(NodeLocation start, NodeLocation end, double progress) {
        this.start = start;
        this.end = end;
        this.progress = progress;

        double xdiff = -(this.start.getLat() - this.end.getLat());
        double ydiff = -(this.start.getLon() - this.end.getLon());
        this.lat = this.start.getLat() + xdiff * progress;
        this.lon = this.start.getLon() + ydiff * progress;
    }

    public NodeLocation getStart() {
        return start;
    }

    public NodeLocation getEnd() {
        return end;
    }

    public double getProgress() {
        return progress;
    }

    public Edge getEdge() {
        return new Edge(this.start.getNode(), this.end.getNode());
    }

    public Location advance(double distance) {
        double olddist = this.start.distanceTo(this);
        double newdist = olddist + distance;
        double totaldist = this.start.distanceTo(this.end);
        double oldprog = this.getProgress();
        double newprog = newdist / totaldist;
        //double newprog = (oldprog * newdist) / olddist;
        EdgeLocation newloc = new EdgeLocation(this.start, this.end, newprog);
        if (newloc.distanceTo(this.end) < distance)
            return this.end;
        else
            return newloc;
    }
}
