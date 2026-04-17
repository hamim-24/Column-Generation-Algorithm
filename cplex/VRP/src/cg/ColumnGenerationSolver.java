package cg;

import ilog.concert.IloException;
import master.RestrictedMasterProblem;
import model.Customer;
import model.Route;
import pricing.PricingProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ColumnGenerationSolver {
    private RestrictedMasterProblem rmp;
    private PricingProblem pricing;
    private Map<Integer, Customer> customers;
    private int numCustomers;
    private List<Route> allRoutes;

    public ColumnGenerationSolver(int numCustomers, double maxVehicles, Map<Integer, Customer> customers,
            PricingProblem pricing) throws IloException {
        this.numCustomers = numCustomers;
        this.customers = customers;
        this.pricing = pricing;
        this.rmp = new RestrictedMasterProblem(numCustomers, maxVehicles);
        this.allRoutes = new ArrayList<>();
    }

    public void solve() throws IloException {

        System.out.println("\nStep 4: Column Generation Execution");
        System.out.println("-----------------------------------");

        // 1. Generate initial feasible routes (single-customer routes)
        for (int i = 1; i <= numCustomers; i++) {
            List<Integer> path = new ArrayList<>();
            path.add(i);

            // Assume 1 unit distance to depot for simplicity of initialization
            Route dummyRoute = new Route(path, 10000.0, customers.get(i).demand, 0.0);
            rmp.addRoute(dummyRoute);
            allRoutes.add(dummyRoute);
        }

        int iteration = 1;
        while (true) {
            // 2. Solve RMP
            if (!rmp.solve()) {
                // System.out.println("RMP Infeasible!");
                break;
            }

            double rmpObj = rmp.getObjectiveValue();

            // 3. Extract duals
            double[] dualPrices = rmp.getDualPrices();
            double vehicleDual = rmp.getVehicleDual();

            // 4. Solve pricing problem
            List<Route> newRoutes = pricing.findNegativeReducedCostRoutes(dualPrices, vehicleDual);

            System.out.println(
                    "Iteration " + iteration + " | Objective: " + rmpObj + " | New columns: " + newRoutes.size());

            // 5. Add columns
            if (newRoutes.isEmpty()) {
                // System.out.println("No more improving columns found. Optimal LP solution reached.");
                break;
            }

            for (Route r : newRoutes) {
                rmp.addRoute(r);
                allRoutes.add(r);
            }

            iteration++;
        }

        // 6. Solve final Integer Master Problem
        // System.out.println("Solving Final Integer Formulation...");
        rmp.convertToIntegerAndSolve();

        System.out.println("\n----------------FINAL SOLUTION----------------");
        System.out.println("Optimal Total Cost: " + rmp.getObjectiveValue());

        List<Integer> selectedIndices = rmp.getSelectedRoutesIndices();
        System.out.println("Selected Routes:");
        for (int idx : selectedIndices) {
            System.out.println(allRoutes.get(idx));
        }

        rmp.end();
    }
}
