package master;

import ilog.concert.IloColumn;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import model.Route;

import java.util.ArrayList;
import java.util.List;

public class RestrictedMasterProblem {
    private IloCplex cplex;
    private IloObjective objective;
    private IloRange[] customerConstraints;
    private IloRange vehicleConstraint;
    private List<IloNumVar> routeVars;
    private int numCustomers;
    private double maxVehicles;

    public RestrictedMasterProblem(int numCustomers, double maxVehicles) throws IloException {
        this.numCustomers = numCustomers;
        this.maxVehicles = maxVehicles;
        this.cplex = new IloCplex();
        this.cplex.setOut(null); // disable output for cplex solver
        this.routeVars = new ArrayList<>();

        buildModel();
    }

    private void buildModel() throws IloException {
        // Objective: Minimize total cost
        objective = cplex.addMinimize();

        // Constraints: Each customer visited exactly once
        customerConstraints = new IloRange[numCustomers];
        for (int i = 0; i < numCustomers; i++) {
            customerConstraints[i] = cplex.addRange(1.0, 1.0, "Customer_" + (i + 1));
        }

        // Constraint: Maximum number of vehicles
        if (maxVehicles > 0) {
            vehicleConstraint = cplex.addRange(-Double.MAX_VALUE, maxVehicles, "Max_Vehicles");
        }
    }

    public void addRoute(Route route) throws IloException {
        IloColumn column = cplex.column(objective, route.cost);

        for (int customerId : route.customers) {
            if (customerId > 0 && customerId <= numCustomers) {
                column = column.and(cplex.column(customerConstraints[customerId - 1], 1.0));
            }
        }

        if (maxVehicles > 0 && vehicleConstraint != null) {
            column = column.and(cplex.column(vehicleConstraint, 1.0));
        }

        IloNumVar var = cplex.numVar(column, 0.0, Double.MAX_VALUE, "R_" + routeVars.size());
        routeVars.add(var);
    }

    public boolean solve() throws IloException {
        return cplex.solve();
    }

    public double getObjectiveValue() throws IloException {
        return cplex.getObjValue();
    }

    public double[] getDualPrices() throws IloException {
        double[] duals = new double[numCustomers];
        for (int i = 0; i < numCustomers; i++) {
            duals[i] = cplex.getDual(customerConstraints[i]);
        }
        return duals;
    }

    public double getVehicleDual() throws IloException {
        if (maxVehicles > 0 && vehicleConstraint != null) {
            return cplex.getDual(vehicleConstraint);
        }
        return 0.0;
    }

    public void convertToIntegerAndSolve() throws IloException {
        for (IloNumVar var : routeVars) {
            cplex.add(cplex.conversion(var, ilog.concert.IloNumVarType.Int));
        }
        cplex.setOut(System.out); // Show final solve output
        cplex.solve();
    }

    public List<Integer> getSelectedRoutesIndices() throws IloException {
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < routeVars.size(); i++) {
            if (cplex.getValue(routeVars.get(i)) > 0.5) {
                selected.add(i);
            }
        }
        return selected;
    }

    public void end() {
        if (cplex != null) {
            cplex.end();
        }
    }
}
