package handlers;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import utils.DBHelper;

public class FTPHandler implements Runnable {

    private Socket socket;

    public FTPHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                
            // Extract IPv4 address correctly
            String clientIP = socket.getInetAddress().getHostAddress(); // Get only the IPv4 address
            System.out.println("FTP Connection from: " + clientIP);
            logEvent("FTP Connection from: " + clientIP);

            writer.println("220 Fake FTP Server Ready");

            // Prompt for Username
            writer.println("331 Please enter username:");
            String username = reader.readLine();
            logEvent("Username entered: " + username);

            // Prompt for Password
            writer.println("331 Please enter password:");
            String password = reader.readLine();
            logEvent("Password entered: " + password);

            // Fake authentication failure
            writer.println("530 Login incorrect.");
            logEvent("Login failed for user: " + username);

            socket.close();
        } catch (IOException e) {
            System.err.println("Error handling FTP client: " + e.getMessage());
            logEvent("Error handling FTP client: " + e.getMessage());
        }
    }

    // Function to log FTP credentials
    private void logEvent(String message) {
        try (FileWriter fw = new FileWriter("ftp_credentials.log", true); 
             BufferedWriter bw = new BufferedWriter(fw); 
             PrintWriter out = new PrintWriter(bw)) {

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            out.println(timestamp + " - " + message);

            // Insert into database
            DBHelper.insertLog(timestamp, socket.getInetAddress().getHostAddress(), 21, "FTP", message, "Unknown");
        } catch (IOException e) {
            System.err.println("Logging error: " + e.getMessage());
        }
    }
}
