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

public class VRPMain 
{
    public static void main(String[] args) 
    {
        Scanner scanner = new Scanner(System.in);

        utils.header("Vehicle Routing Problem");

        while (true)
        {
            try {
                utils.subHeader("STEP 1: LOAD DATA FILE");
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
                        System.out.println(customerFile + " doesn't exist. Starting over...\n");
                        continue;
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
                        System.out.println(distanceFile + " doesn't exist. Starting over...\n");
                        continue;
                    }
                }
                double[][] distances = InputParser.parseDistances(distanceFile, numNodes);
                System.out.println("Loaded " + distances.length + " routes.");

                utils.subHeader("STEP 2: OPERATIONAL CONSTRAINTS");
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

                utils.subHeader("STEP 3: COST PARAMETERS");
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

                utils.subHeader("Step 4: Column Generation Execution");

                long startTime = System.currentTimeMillis();

                PricingProblem pricing = new PricingProblem(numNodes, customers, distances, capacity, maxDuration, costPerDist, fixedCost, overtimePenalty);

                ColumnGenerationSolver solver = new ColumnGenerationSolver(customers.size(), maxVehICLES, customers, pricing);

                solver.solve();
                solver.printSolution();

                long endTime = System.currentTimeMillis();
                System.out.println("Execution time: " + (endTime - startTime) + " ms");

                break;
            } 
            catch (NumberFormatException e)
            {
                System.err.println("Error: " + e.getMessage() + ". Please enter valid numbers. Starting over...\n");
            }
            catch (IOException e) 
            {
                System.err.println("IO Error: " + e.getMessage());
            } 
            catch (IloException e) 
            {
                System.err.println("CPLEX Error: " + e.getMessage());
                e.printStackTrace();
                break;
            } 
            catch (Exception e) {
                System.err.println("Error: " + e.getMessage() + "Starting over...\n");
                // e.printStackTrace();
            }
        }

        scanner.close();;
    }
}
