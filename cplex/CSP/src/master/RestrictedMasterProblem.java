package master;

import ilog.concert.*;
import ilog.cplex.*;
import model.Flight;
import model.Pairing;
import util.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestrictedMasterProblem {

    private IloCplex cplex;
    private List<Flight> flights;
    private List<Pairing> columns;
    private Map<Flight, IloRange> constraints;
    private Map<Pairing, IloNumVar> variables;

    public RestrictedMasterProblem(List<Flight> flights) throws IloException 
    {
        this.flights = flights;
        this.columns = new ArrayList<>();
        this.constraints = new HashMap<>();
        this.variables = new HashMap<>();
        this.cplex = new IloCplex();

        cplex.setOut(null);

        buildModel();
    }

    private void buildModel() throws IloException 
    {
        // minimize Total Cost
        cplex.addMinimize();

        // add constraints: Each flight covered exactly once

        for (Flight f : flights) 
        {
            IloLinearNumExpr expr = cplex.linearNumExpr();
            IloRange constraint = cplex.addEq(expr, 1.0, "Cover_" + f.getFlightId());
            constraints.put(f, constraint);
        }
    }

    public void addColumn(Pairing pairing) throws IloException 
    {
        columns.add(pairing);
        IloColumn col = cplex.column(cplex.getObjective(), pairing.getCost());

        for (Flight f : pairing.getFlights()) 
        {
            if (constraints.containsKey(f)) 
                {
                col = col.and(cplex.column(constraints.get(f), 1.0));
            }
        }

        IloNumVar var = cplex.numVar(col, 0.0, Double.MAX_VALUE, "x_" + columns.size());
        variables.put(pairing, var);
    }

    public void solve() throws IloException 
    {
        cplex.solve();
    }

    public double[] getDuals() throws IloException 
    {
        double[] duals = new double[flights.size()];

        for (int i = 0; i < flights.size(); i++) 
        {
            duals[i] = cplex.getDual(constraints.get(flights.get(i)));
        }
        return duals;
    }

    public double getObjectiveValue() throws IloException 
    {
        return cplex.getObjValue();
    }

    public void close() 
    {
        cplex.end();
    }

    public void generateInitialSolution() throws IloException 
    {
        for (Flight f : flights) 
        {
            Pairing p = new Pairing();
            p.addFlight(f);
            p.setCost(utils.UN_REALISTIC_VALUE); //unrealistic column
            addColumn(p);
        }
    }

    public List<Pairing> getSolution() throws IloException 
    {
        List<Pairing> selected = new ArrayList<>();
        
        for (Pairing p : columns) 
        {
            double val = cplex.getValue(variables.get(p));
            if (val > 0.0001) 
            {
                selected.add(p);
            }
        }
        return selected;
    }
}
