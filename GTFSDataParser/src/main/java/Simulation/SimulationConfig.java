package Simulation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by micha on 10.01.2017.
 */
@XmlRootElement
public class SimulationConfig {
    public static class Builder {
        private SimulationConfig config = new SimulationConfig();

        public Builder() {
        }

        public Builder(SimulationConfig config){
            this.config = config;
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

        public Builder linefrequency(Map<Integer, Integer> linefrequency) {
            this.config.linefrequency = linefrequency;
            return this;
        }

        public Builder taxirate(double taxirate) {
            this.config.taxirate = taxirate;
            return this;
        }

        public Builder turns(int turns) {
            this.config.turns = turns;
            return this;
        }

        public Builder maxcalctime(long time){
            this.config.maxcalctime = time;
            return this;
        }

        public Builder calcstep(int step){
            this.config.calcstep = step;
            return this;
        }
    }

    private double speed = 1000; //1000
    private int capacity = 8; //8
    private int spawnfrequency = 1; //1
    private double spawnshare = 0.1; //10
    private Map<Integer,Integer> linefrequency = new HashMap<>(); //4
    private double taxirate = 0.05; //?
    private int turns = 10000; //10000
    private long maxcalctime = 100L;
    private int calcstep = 5;

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
    public Map<Integer, Integer> getLinefrequency() {
        return linefrequency;
    }

    @XmlElement
    public double getTaxirate() {
        return taxirate;
    }

    @XmlElement
    public int getTurns() {
        return turns;
    }

    @XmlElement
    public long getMaxcalctime() {
        return maxcalctime;
    }

    @XmlElement
    public int getCalcstep() {
        return calcstep;
    }
}
