package main;

import core.PortListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import ui.Dashboard;
import utils.DBHelper;
import utils.GeoIPService;

public class Honey {

    private static final List<Integer> PORTS = Arrays.asList(21, 22, 8080, 2222);
    private static final String LOG_FILE = "log.txt";
    private static Dashboard dashboard;

    public static void main(String[] args) {
        System.out.println("Starting Port Listener...");
        logEvent("Starting Port Listener...");

        // Initialize Database
        DBHelper.initializeDatabase();

        // Initialize Dashboard UI
        dashboard = new Dashboard();
        GeoIPService.init("src/utils/GeoLite2-City.mmdb");

        for (int port : PORTS) {
            new Thread(new PortListener(port)).start();
        }
    }

    // Get the reference of the dashboard
    public static Dashboard getDashboard() {
        return dashboard;
    }

    // Logging function
    public static void logEvent(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String entry = timestamp + " - " + message;

        // Write to file
        try (FileWriter fw = new FileWriter(LOG_FILE, true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) {
            out.println(entry);
        } catch (IOException e) {
            System.err.println("Logging error: " + e.getMessage());
        }

        // Print to console
        System.out.println(entry);

        // Push into the dashboard UI (if initialized)
        if (dashboard != null) {
            dashboard.addLog(entry);
        }
    }

    public class ExampleAttackHandler {

        public static void simulateAttack() {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String ip = "192.168.1.10";
            int port = 22;
            String type = "Brute Force";
            String message = "Invalid SSH login attempt";
            String location = "Unknown"; // later we can get GeoIP here

            DBHelper.insertLog(timestamp, ip, port, type, message, location);
        }
    }

}
