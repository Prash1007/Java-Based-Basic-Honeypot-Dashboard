package utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;

import java.io.File;
import java.net.InetAddress;

public class GeoIPService {

    private static DatabaseReader dbReader;

    public static void init(String dbPath) {
        try {
            File dbFile = new File("db/GeoLite2-City.mmdb"); // ✅ Fix 1: Correct path
            dbReader = new DatabaseReader.Builder(dbFile).build();
        } catch (Exception e) {
            System.err.println("Failed to load GeoIP database: " + e.getMessage());
        }
    }

    public static String lookup(String ip) {
        if (dbReader == null) {
            return "Unknown";
        }
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = dbReader.city(ipAddress);
            return response.getCity().getName() + ", " + response.getCountry().getName();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public static String getLocation(String ipAddress) {
        try {
            InetAddress ip = InetAddress.getByName(ipAddress);
            CityResponse response = dbReader.city(ip);

            String city = response.getCity().getName();
            String country = response.getCountry().getName();
            return city + ", " + country;
        } catch (Exception e) {
            System.err.println("GeoIP lookup failed for IP " + ipAddress + ": " + e.getMessage()); // ✅ Fix 2: Debug log
            return "Unknown Location";
        }
    }
}
