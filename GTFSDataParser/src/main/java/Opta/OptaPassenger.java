package Opta;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

/**
 * Created by Sabine on 07.02.2017.
 */
@XStreamAlias("OptaPassenger")
@PlanningEntity
public class OptaPassenger {
    //private Location start;
    //private Location end;
    private OptaTaxi taxi;
    private int distance;
    private int Id;

    @PlanningVariable(valueRangeProviderRefs = {"taxiRange"})
    public OptaTaxi getTaxi() {
        return taxi;
    }

    public void setTaxi(OptaTaxi taxi) {
        this.taxi = taxi;
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
}
