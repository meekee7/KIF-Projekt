package Simulation.Entity;

import java.awt.geom.Point2D;

/**
 * Created by micha on 12.12.2016.
 */
public abstract class Location {
    protected double lat;
    protected double lon;

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double distanceTo(Location location) {
        //http://www.movable-type.co.uk/scripts/latlong.html
        double R = 6371e3; // metres
        double φ1 = Math.toRadians(this.getLat());
        double φ2 = Math.toRadians(location.getLat());
        double Δφ = Math.toRadians(this.getLat() - location.getLat());
        double Δλ = Math.toRadians(this.getLon() - location.getLon());

        double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                Math.cos(φ1) * Math.cos(φ2) *
                        Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double d = R * c;
        return d;
        //return this.getPointAWT().distance(location.getPointAWT());
    }

    public Point2D getPointAWT() {
        return new Point2D.Double(this.lat, this.lon);
    }
}
