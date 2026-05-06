package pricing;

import model.Flight;
import model.Pairing;
import util.TimeUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.LocalTime;
import java.util.Map;

public class PricingProblem 
{
    private List<Flight> allFlights;
    private String base;

    // constraints
    private double maxDutyHours;
    private double maxFlyingHours;
    private long minTurnaroundMin;
    private boolean allowOvernight;

    // cost parameters
    private double fixedCost;
    private double hourlyCost;
    private double nightPenalty;
    private double overtimePenaltyPerHour;

    public PricingProblem(List<Flight> allFlights, String base, double maxDutyHours, double maxFlyingHours,
                          long minTurnaroundMin, boolean allowOvernight, double fixedCost, double hourlyCost, 
                          double nightPenalty, double overtimePenaltyPerHour) 
    {    
        this.allFlights = new ArrayList<>(allFlights);
        // sort flights by departure time
        this.allFlights.sort(Comparator.comparing(Flight::getDepTime));

        this.base = base;
        this.maxDutyHours = maxDutyHours;
        this.maxFlyingHours = maxFlyingHours;
        this.minTurnaroundMin = minTurnaroundMin;
        this.allowOvernight = allowOvernight;

        this.fixedCost = fixedCost;
        this.hourlyCost = hourlyCost;
        this.nightPenalty = nightPenalty;
        this.overtimePenaltyPerHour = overtimePenaltyPerHour;
    }

    public List<Pairing> solve(Map<String, Double> dualMap) 
    {
        List<Pairing> newColumns = new ArrayList<>();

        for (Flight f : allFlights) 
        {
            if (f.getFrom().equals(base)) 
            {
                List<Flight> path = new ArrayList<>();
                path.add(f);
                dfs(f, path, f.getDurationHours(), dualMap, newColumns);
            }
        }

        return newColumns;
    }

    private void dfs(Flight current, List<Flight> currentPath, double currentFlyingTime,
            Map<String, Double> duals, List<Pairing> solutions) 
    {
        // check if we can close the pairing to Base
        if (current.getTo().equals(base)) 
        {
            // crheck full duty validity & Cost
            double dutyTime = calculateDutyTime(currentPath);

            if (dutyTime <= maxDutyHours) 
            {
                Pairing p = createPairing(currentPath);
                double redCost = calculateReducedCost(p, duals);
                // negative reduced cost
                if (redCost < -0.0001) 
                { 
                    solutions.add(p);
                }
            }
        }

        // try to extend
        for (Flight next : allFlights) 
        {
            if (isValidConnection(current, next)) 
            {
                // check flying time
                if (currentFlyingTime + next.getDurationHours() <= maxFlyingHours) 
                {
                    currentPath.add(next);
                    dfs(next, currentPath, currentFlyingTime + next.getDurationHours(), duals, solutions);
                    currentPath.remove(currentPath.size() - 1);
                }
            }
        }
    }

    //check location and turnarround time
    private boolean isValidConnection(Flight f1, Flight f2) 
    {
        // location connection f1.getTo() == f2.getFrom()
        if (!f1.getTo().equals(f2.getFrom())) 
        {
            return false;
        }

        // time connection
        long turn = TimeUtils.minutesBetween(f1.getArrTime(), f2.getDepTime());
        if (turn < minTurnaroundMin) 
        {
            return false;
        }

        if (f2.getDepTime().isBefore(f1.getArrTime())) 
        {
            if (!allowOvernight) 
            {
                return false;
            }
        }
        return true;
    }

    private double calculateDutyTime(List<Flight> path) 
    {

        if (path.isEmpty()) 
        {
            return 0;
        }

        LocalTime start = path.get(0).getDepTime();
        LocalTime end = path.get(path.size() - 1).getArrTime();

        long mins = TimeUtils.minutesBetween(start, end);
        // Handle overnight (if end < start, add 24h)
        if (end.isBefore(start)) 
        {
            mins += 24 * 60;
        }
        return mins / 60.0;
    }

    private Pairing createPairing(List<Flight> path) 
    {
        double cost = 0;
        double flyingTime = 0;
        boolean hasNight = false;

        for (Flight f : path) 
        {
            cost += f.getFlightCost();
            flyingTime += f.getDurationHours();
            
            if (f.isNight())
            {
                hasNight = true;
            }
        }

        cost += fixedCost; // fixed duty cost
        cost += (flyingTime * hourlyCost);

        if (hasNight) 
        {
            cost += nightPenalty;
        }
        
        // overtime
        double duty = calculateDutyTime(path);

        if (duty > 8.0) 
        {
            cost += (duty - 8.0) * overtimePenaltyPerHour;
        }

        return new Pairing(path, cost);
    }

    private double calculateReducedCost(Pairing p, java.util.Map<String, Double> duals) 
    {
        double dualSum = 0;
        
        for (Flight f : p.getFlights()) 
        {
            dualSum += duals.getOrDefault(f.getFlightId(), 0.0); //lib to get dualsum
        }
        return p.getCost() - dualSum;
    }

}
