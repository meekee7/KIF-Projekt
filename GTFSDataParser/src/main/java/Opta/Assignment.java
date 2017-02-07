package Opta;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.examples.common.domain.AbstractPersistable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sabine on 07.02.2017.
 */
@PlanningSolution
@XStreamAlias("Assignment")
public class Assignment extends AbstractPersistable implements Solution<HardSoftScore>{

    private List<OptaPassenger> passengerList = new LinkedList<>();
    private List<OptaTaxi> taxiList = new LinkedList<>();
    private long Id = 0L;

    private HardSoftScore score;

    @ValueRangeProvider(id = "taxiRange")
    public List<OptaTaxi> getTaxiList() {
        return taxiList;
    }

    public void setPassengerList(List<OptaPassenger> passengerList) {
        this.passengerList = passengerList;
    }

    @PlanningEntityCollectionProperty
    public List<OptaPassenger> getPassengerList() {
        return passengerList;
    }

    public void setTaxiList(List<OptaTaxi> taxiList) {
        this.taxiList = taxiList;
    }

    public void setId(long i){
        Id = i;
    }

    public Long getId() {
        return Id;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public Collection<? extends Object> getProblemFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(taxiList);
        // Do not add the planning entity's (processList) because that will be done automatically
        return facts;
    }
}

