package model;

import util.utils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Pairing 
{
    private List<Flight> flights;
    private double cost;

    public Pairing() 
    {
        this.flights = new ArrayList<>();
        this.cost = 0.0;
    }

    public Pairing(List<Flight> flights, double cost) 
    {
        this.flights = new ArrayList<>(flights);
        this.cost = cost;
    }

    public void addFlight(Flight flight) 
    {
        this.flights.add(flight);
    }

    public List<Flight> getFlights() 
    {
        return flights;
    }

    public double getCost() 
    {
        return cost;
    }

    public void setCost(double cost) 
    {
        this.cost = cost;
    }

    public Flight getLastFlight() 
    {
        if (flights.isEmpty())
        {
            return null;
        }
        return flights.get(flights.size() - 1);
    }

    @Override
    public String toString() 
    {
        if (cost == utils.UN_REALISTIC_VALUE) 
        {
            return flights.stream().map(Flight::getFlightId).collect(Collectors.joining("->"))
                + " ($" + cost + ")" + "(Unrealistic value)";
        }

        return flights.stream().map(Flight::getFlightId).collect(Collectors.joining("->"))
                + " ($" + cost + ")";
    }
    
}
