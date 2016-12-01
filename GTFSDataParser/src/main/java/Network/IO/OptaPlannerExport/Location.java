package Network.IO.OptaPlannerExport;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.awt.geom.Point2D;

/**
 * Created by micha on 30.11.2016.
 */
public class Location {
    private int innerid;
    private int globalid;
    private double lat;
    private double lon;

    public Location(Point2D point, int innerid){
        this(point.getX(), point.getY(), innerid);
    }

    public Location(double lat, double lon, int innerid) {
        this.innerid = innerid;
        this.globalid = this.innerid + 3;
        this.lat = lat;
        this.lon = lon;
    }

    @XmlAttribute(name = "id")
    public int getInnerid() {
        return innerid;
    }

    @XmlElement(name = "id")
    public int getGlobalid() {
        return globalid;
    }

    @XmlElement(name = "latitude")
    public double getLat() {
        return lat;
    }

    @XmlElement(name = "longitude")
    public double getLon() {
        return lon;
    }
}
