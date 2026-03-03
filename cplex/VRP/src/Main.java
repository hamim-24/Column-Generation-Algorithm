import cg.ColumnGenerationSolver;
import ilog.concert.IloException;
import model.Customer;
import pricing.PricingProblem;
import util.InputParser;

import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Vehicle Routing using Column Generation Solver: IBM ILOG CPLEX (Java)");
        System.out.println("------------------------------------------------------------------");

        try {
            System.out.println("STEP 1: LOAD DATA FILE");
            System.out.print("Enter path to customer file [default: data/customers.csv]: > ");
            String customerFile = scanner.nextLine();
            if (customerFile.isEmpty())
                customerFile = "data/customers.csv";

            System.out.print("Enter path to distance matrix file [default: data/distances.csv]: > ");
            String distanceFile = scanner.nextLine();
            if (distanceFile.isEmpty())
                distanceFile = "data/distances.csv";

            Map<Integer, Customer> customers = InputParser.parseCustomers(customerFile);
            int numNodes = customers.size() + 1; // including depot (0)
            double[][] distances = InputParser.parseDistances(distanceFile, numNodes);

            System.out.println("\nSTEP 2: OPERATIONAL CONSTRAINTS");
            System.out.print("Vehicle capacity [default: 50.0]: > ");
            String capStr = scanner.nextLine();
            double capacity = capStr.isEmpty() ? 50.0 : Double.parseDouble(capStr);

            System.out.print("Maximum route duration (hours, optional): > ");
            String durStr = scanner.nextLine();
            double maxDuration = durStr.isEmpty() ? -1 : Double.parseDouble(durStr);

            System.out.print("Maximum number of vehicles: > ");
            String vehStr = scanner.nextLine();
            double maxVehICLES = vehStr.isEmpty() ? -1 : Double.parseDouble(vehStr);

            System.out.println("\nSTEP 3: COST PARAMETERS");
            System.out.print("Cost per distance unit [default: 1.0]: > ");
            String cpdStr = scanner.nextLine();
            double costPerDist = cpdStr.isEmpty() ? 1.0 : Double.parseDouble(cpdStr);

            System.out.print("Fixed cost per vehicle [default: 100.0]: > ");
            String fcStr = scanner.nextLine();
            double fixedCost = fcStr.isEmpty() ? 100.0 : Double.parseDouble(fcStr);

            System.out.print("Penalty for overtime per hour [default: 10.0]: > ");
            String pfoStr = scanner.nextLine();
            double overtimePenalty = pfoStr.isEmpty() ? 10.0 : Double.parseDouble(pfoStr);

            System.out.println("\nSTEP 4: COLUMN GENERATION EXECUTION");
            long startTime = System.currentTimeMillis();

            PricingProblem pricing = new PricingProblem(numNodes, customers, distances,
                    capacity, maxDuration, costPerDist,
                    fixedCost, overtimePenalty);

            ColumnGenerationSolver solver = new ColumnGenerationSolver(customers.size(), maxVehICLES, customers,
                    pricing);
            solver.solve();

            long endTime = System.currentTimeMillis();
            System.out.println("Execution time: " + (endTime - startTime) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
