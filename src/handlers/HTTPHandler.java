package handlers;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import main.Honey;
import utils.DBHelper;

public class HTTPHandler implements Runnable {

    private static final Map<String, Integer> ipLoginAttempts = new HashMap<>();
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    private Socket socket;

    public HTTPHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Get client IP address and clean up loopback address
            String clientIP = socket.getInetAddress().getHostAddress();
            if (clientIP.equals("0:0:0:0:0:0:0:1") || clientIP.equals("::1")) {
                clientIP = "127.0.0.1";  // Convert to standard loopback address
            }

            System.out.println("Connection from: " + clientIP);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            String requestLine = reader.readLine();
            if (requestLine != null && requestLine.startsWith("POST /report")) {
                // read headers
                String line;
                int contentLength = 0;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    if (line.toLowerCase().startsWith("content-length:")) {
                        contentLength = Integer.parseInt(line.split(":")[1].trim());
                    }
                }
                // read JSON body
                char[] buf = new char[contentLength];
                reader.read(buf, 0, contentLength);
                String json = new String(buf);

                // log it
                Honey.logEvent("📣 [Django Alert] " + json);

                try {
                    // Parse JSON
                    org.json.JSONObject obj = new org.json.JSONObject(json);

                    String ip = obj.optString("ip", "unknown");
                    int port = obj.optInt("port", 8080);  // default to 8080
                    String type = obj.optString("type", "Unknown Attack");
                    String path = obj.optString("path", "");
                    String body = obj.optString("body", "");
                    String timestamp = obj.optString("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

                    // Optional: get GeoIP
                    String location = utils.GeoIPService.lookup(ip);

                    // Save to DB
                    DBHelper.insertLog(timestamp, ip, port, type, path + " " + body, location);
                } catch (Exception e) {
                    Honey.logEvent("❌ Error parsing report JSON: " + e.getMessage());
                }

                // respond
                writer.println("HTTP/1.1 200 OK\r\nContent-Length:0\r\n\r\n");
                writer.flush();
                socket.close();
                return;  // skip the rest of fake-login logic
            }
            System.out.println("Request: " + requestLine);

            // Detect admin panel scans
            if (requestLine != null && requestLine.contains("/admin")) {
                Honey.logEvent("[ALERT] Admin panel access attempt by " + clientIP + " — Request: " + requestLine);
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                DBHelper.insertLog(timestamp, clientIP, 8080, "SQL Injection", requestLine, "Unknown");
            }

            // Detect SQL Injection & XSS attempts in URL
            if (requestLine != null && containsInjection(requestLine)) {
                Honey.logEvent("[ALERT] Possible injection/XSS attack by " + clientIP + " — Request: " + requestLine);
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                DBHelper.insertLog(timestamp, clientIP, 8080, "SQL Injection", requestLine, "Unknown");
            }

            // Read headers and body if POST
            boolean isPost = requestLine != null && requestLine.startsWith("POST");
            StringBuilder body = new StringBuilder();
            String line;
            int contentLength = 0;

            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            if (isPost && contentLength > 0) {
                char[] buffer = new char[contentLength];
                reader.read(buffer, 0, contentLength);
                body.append(buffer);

                String bodyData = body.toString();
                if (containsInjection(bodyData)) {
                    Honey.logEvent("[ALERT] Injection detected in POST data by " + clientIP + " — Data: " + bodyData);
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    DBHelper.insertLog(timestamp, clientIP, 8080, "SQL Injection", requestLine, "Unknown");
                }

                // Brute-force simulation: track login attempts
                ipLoginAttempts.put(clientIP, ipLoginAttempts.getOrDefault(clientIP, 0) + 1);
                if (ipLoginAttempts.get(clientIP) > MAX_LOGIN_ATTEMPTS) {
                    Honey.logEvent("[ALERT] Brute-force login attempt from " + clientIP);
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    DBHelper.insertLog(timestamp, clientIP, 8080, "SQL Injection", requestLine, "Unknown");

                } else {
                    Honey.logEvent("[INFO] Login attempt " + ipLoginAttempts.get(clientIP) + " from " + clientIP);
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    DBHelper.insertLog(timestamp, clientIP, 8080, "SQL Injection", requestLine, "Unknown");

                }
            }

            // Send fake login page
            String httpResponse = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html; charset=UTF-8\r\n"
                    + "Connection: close\r\n"
                    + "\r\n"
                    + "<html><head><title>Honeypot Login</title></head><body>"
                    + "<h2>Fake Login Page</h2>"
                    + "<form method='POST'>"
                    + "Username: <input type='text' name='username'><br>"
                    + "Password: <input type='password' name='password'><br>"
                    + "<input type='submit' value='Login'>"
                    + "</form>"
                    + "</body></html>";

            writer.println(httpResponse);
            writer.flush();
            socket.close();

        } catch (IOException e) {
            System.err.println("HTTP Handler Error: " + e.getMessage());
        }
    }

    private boolean containsInjection(String input) {
        if (input == null) {
            return false;
        }

        String lower = input.toLowerCase();
        return lower.contains("' or '1'='1")
                || lower.contains("union")
                || lower.contains("select")
                || lower.contains("drop")
                || lower.contains("<script>")
                || lower.contains("onerror")
                || lower.contains("alert(")
                || lower.contains("--")
                || lower.contains("/*")
                || lower.contains("../")
                || lower.contains("admin");
    }
}
