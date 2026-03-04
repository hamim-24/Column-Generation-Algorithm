import cg.ColumnGenerationSolver;
import ilog.concert.IloException;
import model.Customer;
import pricing.PricingProblem;
import util.InputParser;
import util.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class Main 
{
    public static void main(String[] args) 
    {
        Scanner scanner = new Scanner(System.in);

        utils.header("Vehicle Routing Problem");

        try {
            System.out.println("\n=== STEP 1: LOAD DATA FILE ===");
            System.out.println("Enter path to customer file [default: data/customers.csv]");
            System.out.print(":: ");
            String customerFile = scanner.nextLine().trim();

            if (customerFile.isEmpty())
            {
                customerFile = "data/customers.csv";
            }

            if (!new File(customerFile).exists()) 
            {
                System.err.println("Error: File not found at " + customerFile);
                // try relative to project root if running from bin
                if (new File("../" + customerFile).exists()) 
                {
                    customerFile = "../" + customerFile;
                    System.out.println("Found at " + customerFile);
                } 
                else 
                {
                    System.out.println(customerFile + " doesn't exist. Please fix the file path.");
                    scanner.close();
                    return;
                }
            }
            Map<Integer, Customer> customers = InputParser.parseCustomers(customerFile);
            int numNodes = customers.size() + 1; // including depot (0)
            System.out.println("Loaded " + customers.size() + " customers.");

            System.out.println("Enter path to distance matrix file [default: data/distances.csv]");
            System.out.print(":: ");
            String distanceFile = scanner.nextLine();

            if (distanceFile.isEmpty())
            {
                distanceFile = "data/distances.csv";
            }

            if (!new File(distanceFile).exists()) 
            {
                System.err.println("Error: File not found at " + distanceFile);
                // try relative to project root if running from bin
                if (new File("../" + distanceFile).exists()) 
                {
                    distanceFile = "../" + distanceFile;
                    System.out.println("Found at " + distanceFile);
                } 
                else 
                {
                    System.out.println(distanceFile + " doesn't exist. Please fix the file path.");
                    scanner.close();
                    return;
                }
            }
            double[][] distances = InputParser.parseDistances(distanceFile, numNodes);
            System.out.println("Loaded " + distances.length + " routes.");

            System.out.println("\n=== STEP 2: OPERATIONAL CONSTRAINTS ===");
            System.out.println("Vehicle capacity [default: 50.0]");
            System.out.print(":: ");
            String capStr = scanner.nextLine();
            double capacity = capStr.isEmpty() ? 50.0 : Double.parseDouble(capStr);

            System.out.println("Maximum route duration (hours, optional)");
            System.out.print(":: ");
            String durStr = scanner.nextLine();
            double maxDuration = durStr.isEmpty() ? -1 : Double.parseDouble(durStr);

            System.out.println("Maximum number of vehicles");
            System.out.print(":: ");
            String vehStr = scanner.nextLine();
            double maxVehICLES = vehStr.isEmpty() ? -1 : Double.parseDouble(vehStr);

            System.out.println("\n=== STEP 3: COST PARAMETERS ===");
            System.out.println("Cost per distance unit [default: 1.0]");
            System.out.print(":: ");
            String cpdStr = scanner.nextLine();
            double costPerDist = cpdStr.isEmpty() ? 1.0 : Double.parseDouble(cpdStr);

            System.out.println("Fixed cost per vehicle [default: 100.0]");
            System.out.print(":: ");
            String fcStr = scanner.nextLine();
            double fixedCost = fcStr.isEmpty() ? 100.0 : Double.parseDouble(fcStr);

            System.out.println("Penalty for overtime per hour [default: 10.0]");
            System.out.print(":: ");
            String pfoStr = scanner.nextLine();
            double overtimePenalty = pfoStr.isEmpty() ? 10.0 : Double.parseDouble(pfoStr);

            long startTime = System.currentTimeMillis();

            PricingProblem pricing = new PricingProblem(numNodes, customers, distances,
                    capacity, maxDuration, costPerDist,
                    fixedCost, overtimePenalty);

            ColumnGenerationSolver solver = new ColumnGenerationSolver(customers.size(), maxVehICLES, customers,
                    pricing);
            solver.solve();

            long endTime = System.currentTimeMillis();
            System.out.println("Execution time: " + (endTime - startTime) + " ms");

        } 
        catch (IOException e) 
        {
            System.err.println("IO Error: " + e.getMessage());
        } 
        catch (IloException e) 
        {
            System.err.println("CPLEX Error: " + e.getMessage());
            e.printStackTrace();
        } 
        catch (Exception e) {
            e.printStackTrace();
        } 
        finally {
            scanner.close();
        }
    }
}
