package Simulation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by micha on 10.01.2017.
 */
@XmlRootElement
public class SimulationConfig {
    private double speed;
    private int capacity;
    private int spawnfrequency;
    private double spawnshare;
    private int linefrequency;
    private int taxirate;

    @XmlElement
    public double getSpeed() {
        return speed;
    }

    @XmlElement
    public int getCapacity() {
        return capacity;
    }

    @XmlElement
    public int getSpawnfrequency() {
        return spawnfrequency;
    }

    @XmlElement
    public double getSpawnshare() {
        return spawnshare;
    }

    @XmlElement
    public int getLinefrequency() {
        return linefrequency;
    }

    @XmlElement
    public int getTaxirate() {
        return taxirate;
    }
}
