package Opta;

import org.optaplanner.examples.common.persistence.AbstractSolutionImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Sabine on 07.02.2017.
 */
public class TaxiAssignmentGenerator {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public Assignment createAssignment(String inputId, int taxiListSize, int passengerListSize) {
        Random random = new Random(47);
        Assignment assignment = new Assignment();
        assignment.setId(0L);
        createTaxiList(assignment, taxiListSize);
        createPassengerList(assignment, passengerListSize);
        //assureComputerCapacityTotalAtLeastProcessRequiredTotal(cloudBalance);
        BigInteger possibleSolutionSize = BigInteger.valueOf(assignment.getTaxiList().size()).pow(
                assignment.getPassengerList().size());
        logger.info("Assignment {} has {} taxis and {} passengers with a search space of {}.",
                inputId, taxiListSize, passengerListSize,
                AbstractSolutionImporter.getFlooredPossibleSolutionSize(possibleSolutionSize));
        return assignment;
    }

    private void createTaxiList(Assignment assignment, int taxiListSize) {
        List<OptaTaxi> taxiList = new ArrayList<>(taxiListSize);
        for (int i = 0; i < taxiListSize; i++) {
            OptaTaxi taxi = generateTaxi();
            taxiList.add(taxi);
        }
        assignment.setTaxiList(taxiList);
    }

    public OptaTaxi generateTaxi() {
        Random rand  = new Random();
        int randomNum = rand.nextInt(100 + 1);

        OptaTaxi taxi = new OptaTaxi();

        taxi.setCapacity(8);
        taxi.setId((long) randomNum);
        logger.trace("Created taxi with ID ({}).",
                randomNum);
        return taxi;
    }

    private void createPassengerList(Assignment assignment, int passengerListSize) {
        List<OptaPassenger> passengerList = new ArrayList<>(passengerListSize);
        for (int i = 0; i < passengerListSize; i++) {
            OptaPassenger passenger = generatePassenger();
            passengerList.add(passenger);
        }
        assignment.setPassengerList(passengerList);
    }

    public OptaPassenger generatePassenger() {
        Random rand  = new Random();
        int randomNum = rand.nextInt(1000 + 1);
        int randomNum2 = rand.nextInt(1000 +1);
        OptaPassenger passenger = new OptaPassenger();
        int i = randomNum;
        int j = randomNum2;
        passenger.setDistance(i);
        passenger.setId(j);

        // Notice that we leave the PlanningVariable properties on null

        logger.trace("Created taxi with ID ({}) and distance ({}).",
                i,j);
        return passenger;
    }
}
