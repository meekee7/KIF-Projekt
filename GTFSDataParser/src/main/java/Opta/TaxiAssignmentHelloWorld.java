package Opta;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

/**
 * Created by Sabine on 07.02.2017.
 */
public class TaxiAssignmentHelloWorld {
    public static void main(String[] args) {
        // Build the Solver
        String solverconfig =
                "taxiAssignmentSolverConfig.xml";
        SolverFactory<Assignment> solverFactory = SolverFactory.createFromXmlResource(
                solverconfig);

        Solver<Assignment> solver = solverFactory.buildSolver();

        // Load a problem with 10 Taxis and 100 passengers
        Assignment unsolvedAssignment = new TaxiAssignmentGenerator().createAssignment("a", 20, 100);

        // Solve the problem
        Assignment solvedAssignment = solver.solve(unsolvedAssignment);

        // Display the result
        System.out.println("\nSolved TaxiAssignment with 20 taxis and 100 passengers:\n"
                + toDisplayString(solvedAssignment));
    }

    public static String toDisplayString(Assignment assignment) {
        StringBuilder displayString = new StringBuilder();
        for (OptaPassenger passenger : assignment.getPassengerList()) {
            OptaTaxi taxi = passenger.getTaxi();
            displayString.append("  Passenger: ").append(passenger.getId()).append(" -> Taxi: ")
                    .append(taxi == null ? null : taxi.getId()).append("\n");
        }
        return displayString.toString();
    }
}
