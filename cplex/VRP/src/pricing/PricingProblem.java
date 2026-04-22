package pricing;

import model.Customer;
import model.Route;

import java.util.*;

public class PricingProblem
{
    private int numNodes; // including depot (0)
    private Map<Integer, Customer> customers;
    private double[][] distances;
    private double capacity;
    private double maxDuration;
    private double costPerDist;
    private double fixedCost;
    private double overtimePenalty;
    private double bestReducedCost; // Track best reduced cost from last solve

    public PricingProblem(int numNodes, Map<Integer, Customer> customers, double[][] distances, double capacity, double maxDuration, double costPerDist, double fixedCost, double overtimePenalty)
    {
        this.numNodes = numNodes;
        this.customers = customers;
        this.distances = distances;
        this.capacity = capacity;
        this.maxDuration = maxDuration;
        this.costPerDist = costPerDist;
        this.fixedCost = fixedCost;
        this.overtimePenalty = overtimePenalty;
    }

    // A simple label correcting / Depth First Search approach for ESPPRC
    public List<Route> findNegativeReducedCostRoutes(double[] duals, double vehicleDual)
    {
        List<Route> routes = new ArrayList<>();

        // DP Label: (currentNode, visited, load, duration, cost, reducedCost, path)
        class Label
        {
            int node;
            Set<Integer> visited;
            double load;
            double duration;
            double cost;
            double reducedCost;
            List<Integer> path;

            Label(int node, Set<Integer> visited, double load, double duration, double cost, double reducedCost, List<Integer> path)
            {
                this.node = node;
                this.visited = new HashSet<>(visited);
                this.load = load;
                this.duration = duration;
                this.cost = cost;
                this.reducedCost = reducedCost;
                this.path = new ArrayList<>(path);
            }
        }

        Queue<Label> queue = new LinkedList<>();
        queue.add(new Label(0, new HashSet<>(), 0.0, 0.0, fixedCost, fixedCost - vehicleDual, Collections.singletonList(0)));

        bestReducedCost = -1e-6; // Only interested in negative reduced costs

        while (!queue.isEmpty())
        {
            Label curr = queue.poll();

            for (int next = 0; next < numNodes; next++)
            {
                if (next == curr.node)
                    continue;
                if (next == 0)
                {
                    // Return to depot
                    double addedCost = distances[curr.node][0] * costPerDist;
                    double finalCost = curr.cost + addedCost;
                    double finalDuration = curr.duration + distances[curr.node][0];
                    if (maxDuration > 0 && finalDuration > maxDuration)
                    {
                        finalCost += (finalDuration - maxDuration) * overtimePenalty;
                    }
                    double finalRc = curr.reducedCost + addedCost; // Duals only apply to customers

                    if (finalRc < bestReducedCost)
                    {
                        List<Integer> finalPath = new ArrayList<>(curr.path);
                        finalPath.add(0);

                        // Exclude depots from route customer list when creating Route object if
                        // desired,
                        // but keeping it simple: just list customers.
                        List<Integer> custPath = new ArrayList<>();
                        for (int p : finalPath)
                            if (p != 0)
                                custPath.add(p);

                        routes.add(new Route(custPath, finalCost, curr.load, finalDuration));
                        bestReducedCost = finalRc; // keep searching for even better
                    }
                }
                else
                {
                    // Visit next customer
                    if (curr.visited.contains(next))
                        continue;
                    double nextLoad = curr.load + customers.get(next).demand;
                    if (nextLoad > capacity)
                        continue;

                    double addedCost = distances[curr.node][next] * costPerDist;
                    double nextCost = curr.cost + addedCost;
                    double nextDuration = curr.duration + distances[curr.node][next];

                    // Reduced cost: Cost - \sum dual_i
                    double nextRc = curr.reducedCost + addedCost - duals[next - 1];

                    Set<Integer> nextVisited = new HashSet<>(curr.visited);
                    nextVisited.add(next);
                    List<Integer> nextPath = new ArrayList<>(curr.path);
                    nextPath.add(next);

                    queue.add(new Label(next, nextVisited, nextLoad, nextDuration, nextCost, nextRc, nextPath));
                }
            }
        }

        // Sort to return the most negative reduced cost routes first
        routes.sort(Comparator.comparingDouble(r -> r.cost));
        return routes;
    }

    public double getBestReducedCost()
    {
        return bestReducedCost;
    }
}
