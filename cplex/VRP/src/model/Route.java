package model;

import java.util.ArrayList;
import java.util.List;

public class Route {
    public List<Integer> customers;
    public double cost;
    public double load;
    public double duration;

    public Route() {
        this.customers = new ArrayList<>();
        this.cost = 0.0;
        this.load = 0.0;
        this.duration = 0.0;
    }

    public Route(List<Integer> customers, double cost, double load, double duration) {
        this.customers = new ArrayList<>(customers);
        this.cost = cost;
        this.load = load;
        this.duration = duration;
    }
    
    @Override
    public String toString() {
        return "Route [cost=" + cost + ", load=" + load + ", path=" + customers + "]";
    }
}
