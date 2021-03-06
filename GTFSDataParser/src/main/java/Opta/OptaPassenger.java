package Opta;

import Network.Node;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

/**
 * Created by Sabine on 07.02.2017.
 */
@XStreamAlias("OptaPassenger")
@PlanningEntity(difficultyComparatorClass = DifficultyComp.class)
public class OptaPassenger {
    //private Location start;
    //private Location end;
    private OptaTaxi taxi;
    private Node start;
    private Node end;
    private int distance;
    private int Id;
    private double airdist;

    public OptaPassenger() {
    }

    public OptaPassenger(Node start, Node end, int id, double airdist) {
        this.start = start;
        this.end = end;
        this.Id = id;
        this.taxi = null;
        this.airdist = airdist;
    }

    @PlanningVariable(valueRangeProviderRefs = {"taxiRange"})
    public OptaTaxi getTaxi() {
        return taxi;
    }

    public void setTaxi(OptaTaxi taxi) {
        this.taxi = taxi;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

        public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getId() {
        return Id;
    }

    public double getAirdist() {
        return airdist;
    }

    public void setAirdist(double airdist) {
        this.airdist = airdist;
    }
}
