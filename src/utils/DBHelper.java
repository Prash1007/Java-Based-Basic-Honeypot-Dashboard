package utils;

import java.io.File;
import java.sql.*;

public class DBHelper {

    private static final String DB_URL = "jdbc:sqlite:db/honeypot_logs.db";

    static {
        File dbFolder = new File("db");
        if (!dbFolder.exists()) {
            dbFolder.mkdirs();
        }
    }

    // Initialize database and create table if it doesn't exist
    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {

            String sql = """
                CREATE TABLE IF NOT EXISTS attack_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TEXT NOT NULL,
                    ip_address TEXT NOT NULL,
                    port INTEGER NOT NULL,
                    attack_type TEXT NOT NULL,
                    message TEXT,
                    location TEXT
                );
            """;

            stmt.execute(sql);
            System.out.println("[✓] attack_logs table is ready.");
        } catch (SQLException e) {
            System.err.println("[!] Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Insert a log entry into the database
    public static void insertLog(String timestamp, String ipAddress, int port, String attackType, String message, String location) {
        String sql = """
            INSERT INTO attack_logs (timestamp, ip_address, port, attack_type, message, location)
            VALUES (?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, timestamp);
            pstmt.setString(2, ipAddress);
            pstmt.setInt(3, port);
            pstmt.setString(4, attackType);
            pstmt.setString(5, message);
            pstmt.setString(6, location);

            pstmt.executeUpdate();
            System.out.println("[+] Log inserted into database.");
        } catch (SQLException e) {
            System.err.println("[!] Error inserting log: " + e.getMessage());
        }
    }
}
