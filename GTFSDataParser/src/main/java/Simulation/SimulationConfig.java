package Simulation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by micha on 10.01.2017.
 */
@XmlRootElement
public class SimulationConfig {
    public static class Builder {
        private SimulationConfig config = new SimulationConfig();

        public Builder() {
        }

        public SimulationConfig assemble() {
            return this.config;
        }

        public Builder speed(double speed) {
            this.config.speed = speed;
            return this;
        }

        public Builder capacity(int capacity) {
            this.config.capacity = capacity;
            return this;
        }

        public Builder spawnfrequency(int spawnfrequency) {
            this.config.spawnfrequency = spawnfrequency;
            return this;
        }

        public Builder spawnshare(double spawnshare) {
            this.config.spawnshare = spawnshare;
            return this;
        }

        public Builder linefrequency(int linefrequency) {
            this.config.linefrequency = linefrequency;
            return this;
        }

        public Builder taxirate(double taxirate) {
            this.config.taxirate = taxirate;
            return this;
        }

        public Builder taxistep(int taxistep) {
            this.config.taxistep = taxistep;
            return this;
        }

        public Builder turns(int turns) {
            this.config.turns = turns;
            return this;
        }
    }

    private double speed = 1000; //1000
    private int capacity = 8; //8
    private int spawnfrequency = 1; //1
    private double spawnshare = 0.1; //10
    private int linefrequency = 4; //4
    private double taxirate = 0.05; //?
    private int taxistep = 4; //?
    private int turns = 10000; //10000

    public SimulationConfig() {
    }

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
    public double getTaxirate() {
        return taxirate;
    }

    @XmlElement
    public int getTaxistep() {
        return taxistep;
    }

    @XmlElement
    public int getTurns() {
        return turns;
    }
}
