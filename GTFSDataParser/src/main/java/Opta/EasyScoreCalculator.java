package Opta;

import Network.Node;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

/**
 * Created by Sabine on 07.02.2017.
 */
public class EasyScoreCalculator implements org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator<Assignment> {

    /**
     * A very simple implementation. The double loop can easily be removed by using Maps as shown in
     *
     */
    public HardSoftScore calculateScore(Assignment assignment) {
        int hardScore = 0;
        int softScore = 0;
        for (OptaTaxi taxi : assignment.getTaxiList()) {
            int peopleInTaxi = 0;
            double travelledDistance = 0.0;
            boolean used = false;

            // Calculate usage
            for (OptaPassenger passenger : assignment.getPassengerList()) {
                if (taxi.equals(passenger.getTaxi())) {
                    peopleInTaxi++;

                    //method for path construction goes here.
                    List<Node> newcorepath = taxi.getGraph().integrateIntoCorePath(taxi.getCorepath(), passenger.getStart(), passenger.getEnd());
                    double cplength = taxi.getGraph().corePathLength(newcorepath);
                    travelledDistance =+ cplength;
                    used = true;
                }
            }

            // Hard constraints
            int capacity = taxi.getCapacity() - peopleInTaxi;
            if (capacity < 0) {
                hardScore += capacity;
            }


            // Soft constraints
            if (used) {
                softScore -= travelledDistance;
            }
        }
        return HardSoftScore.valueOf(hardScore, softScore);
    }
}

