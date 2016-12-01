package Network.IO.OptaPlannerExport;

import javax.xml.bind.Marshaller;
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
    private static int innerIDgiver = 0;

    public Location(Point2D point){
        this(point.getX(), point.getY());
    }

    public Location(double lat, double lon) {
        this.innerid = innerIDgiver++;
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
