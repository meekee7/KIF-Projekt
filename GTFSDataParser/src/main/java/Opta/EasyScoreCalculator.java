package Opta;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

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
            int travelledDistance = 0;
            boolean used = false;

            // Calculate usage
            for (OptaPassenger passenger : assignment.getPassengerList()) {
                if (taxi.equals(passenger.getTaxi())) {
                    peopleInTaxi++;

                    //method for path construction goes here.
                    travelledDistance =+ passenger.getDistance();
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

