package handlers;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import utils.DBHelper;

public class SSHHandler implements Runnable {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String clientIP;
    private static final String LOG_FILE = "credentials_log.txt";
    private static final Map<String, Integer> failedAttempts = new HashMap<>();

    public SSHHandler(Socket socket) {
        this.socket = socket;
        this.clientIP = socket.getInetAddress().getHostAddress(); // Fixing IP format
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Fake SSH Banner
            writer.println("SSH-2.0-OpenSSH_8.2p1 Ubuntu-4ubuntu0.5");

            // Fake SSH Login
            writer.print("login: ");
            writer.flush();
            String username = reader.readLine();

            writer.print("password: ");
            writer.flush();
            String password = reader.readLine();

            if (username != null && password != null) {
                System.out.println("Captured SSH login -> Username: " + username + ", Password: " + password);
                logEvent("Captured SSH login from " + clientIP + " -> Username: " + username + ", Password: " + password);

                // Brute-force detection
                failedAttempts.put(clientIP, failedAttempts.getOrDefault(clientIP, 0) + 1);
                if (failedAttempts.get(clientIP) >= 3) {
                    System.out.println("ALERT: Possible brute-force attack from " + clientIP);
                    logEvent("ALERT: Possible brute-force attack from " + clientIP);
                }
            }

            writer.println("Access denied. Connection closed.");
            socket.close();

        } catch (IOException e) {
            System.err.println("Error handling SSH login: " + e.getMessage());
            logEvent("Error handling SSH login: " + e.getMessage());
        }
    }

    private void logEvent(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            out.println(timestamp + " - " + message);

            // Insert into database
            DBHelper.insertLog(timestamp, clientIP, 22, "SSH", message, "Unknown");
        } catch (IOException e) {
            System.err.println("Logging error: " + e.getMessage());
        }
    }
}
