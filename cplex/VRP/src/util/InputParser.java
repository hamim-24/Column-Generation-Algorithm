package util;

import model.Customer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class InputParser {

    public static Map<Integer, Customer> parseCustomers(String filepath) throws Exception {
        Map<Integer, Customer> customers = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    int id = parseInt(parts[0].trim());
                    double demand = Double.parseDouble(parts[1].trim());
                    customers.put(id, new Customer(id, demand));
                }
            }
        }
        return customers;
    }

    public static double[][] parseDistances(String filepath, int maxNodes) throws Exception {
        double[][] dist = new double[maxNodes][maxNodes];
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    int from = parseInt(parts[0].trim());
                    int to = parseInt(parts[1].trim());
                    double distance = Double.parseDouble(parts[2].trim());
                    dist[from][to] = distance;
                    // assuming directed or undirected; if undirected:
                    // dist[to][from] = distance;
                }
            }
        }
        return dist;
    }

    private static int parseInt(String s) {
        if (s.equalsIgnoreCase("Depot"))
            return 0;
        return Integer.parseInt(s);
    }
}
