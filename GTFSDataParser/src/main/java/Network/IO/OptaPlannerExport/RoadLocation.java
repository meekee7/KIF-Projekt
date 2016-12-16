package Network.IO.OptaPlannerExport;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Map;

/**
 * Created by micha on 14.12.2016.
 */

@XStreamAlias("VrpRoadLocation")
public class RoadLocation {
    protected Map<RoadLocation, Double> travelDistanceMap;

    protected int id;
    protected double latitude;
    protected double longitude;

    public RoadLocation() {
    }

    public Map<RoadLocation, Double> getTravelDistanceMap() {
        return travelDistanceMap;
    }

    public void setTravelDistanceMap(Map<RoadLocation, Double> travelDistanceMap) {
        this.travelDistanceMap = travelDistanceMap;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
