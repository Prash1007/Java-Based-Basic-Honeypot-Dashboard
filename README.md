High-Interaction Honeypot Project

Overview

The main objective of the high-interaction honeypot project is to enhance networking security by deploying high-interaction honeypots that emulate real systems in a careful manner. The project seeks to trap and study the attackers' behavior in a controlled environment, gaining useful information on their strategy, techniques and processes. Through developing and applying strategies based on information gathered from high interaction honeypots, the project successfully aims to minimise the security risk. Further, the project attempts to give students experience in cybersecurity, so that is, they can deal with actual cyber threats confidently. The practical risk involved will not only prepare students for the skills but will also induce a profound appreciation of the complexity of cybersecurity. Furthermore, the Honeypot project with high interception enhances the broader field of cybersecurity research by publishing its results and best practices. It aims to enhance the ground for shared knowledge and foster creative solutions for novel cyber threats. By reaching these objectives, the project will enhance overall network security, drive the knowledge and ability of the people in the cybersecurity fields and help build a stronger and more resilient defence framework against cyber threats.
This Java-based honeypot emulates common services (FTP on port 21, SSH on port 22, HTTP on port 8080, and a generic listener on port 2222) to lure and log attacker interactions. It includes credential capture, injection detection, brute-force tracking, a SQLite database for logging, GeoIP lookup for location tracking, and a real-time GUI dashboard for monitoring attacks.
Key Features

Service Emulation: Fake FTP, SSH, and HTTP servers that capture credentials and detect malicious payloads (e.g., SQL injection, XSS).
Logging & Storage: Events logged to files (e.g., log.txt, ftp_credentials.log, credentials_log.txt) and a SQLite database (db/honeypot_logs.db).
Attack Detection: Monitors for brute-force attempts, admin panel scans, and injection patterns.
GeoIP Integration: Uses MaxMind GeoLite2 to geolocate attacker IPs.
GUI Dashboard: Swing-based UI with real-time logs, attack counters per port, pie chart visualization, and dark/light theme toggle.
Modular Design: Thread-safe handlers for concurrent connections.

Prerequisites & Dependencies

Java 8+: The project uses Java features compatible with JDK 8 or higher.
External Libraries (add to classpath or use Maven/Gradle):
sqlite-jdbc (for SQLite database support).
jfreechart and jcommon (for dashboard charts).
flatlaf (for modern UI theming).
geoip2 (MaxMind GeoIP2 library for location lookup).
org.json (for parsing JSON reports in HTTP handler).

GeoLite2 Database: Download the free GeoLite2-City.mmdb from MaxMind and place it in src/utils/ (or adjust the path in GeoIPService.java).
Build Tool: Optional; can compile/run manually with javac and java.

No internet access is required post-setup, except for initial library downloads.
Setup Instructions

Clone/Download the Project:
git clone https://github.com/Prash1007/Java-Based-Basic-Honeypot-Dashboard
cd honeypot-project
Install Dependencies:
Download JARs for the libraries listed above.
Place them in a lib/ folder (create if needed).
Example classpath: -cp "lib/*:src".

Download GeoIP Database:
Sign up at MaxMind and download GeoLite2-City.mmdb.
Move it to src/utils/GeoLite2-City.mmdb.

Compile the Project:
mkdir -p db  # Creates db folder for SQLite
javac -cp "lib/:src" -d bin src/main/.java src/core/.java src/handlers/.java src/ui/.java src/utils/.java
This compiles to a bin/ folder. Adjust paths as needed.

Run the Honeypot:
java -cp "bin:lib/*" main.Honey
The honeypot will start listeners on ports 21, 22, 8080, and 2222.
A GUI dashboard will open automatically.
Check console output for "Listening on port X" messages.

Verify Setup:
Database: Check db/honeypot_logs.db for the attack_logs table.
Logs: Tail log.txt for real-time events.
Test: Use tools like telnet localhost 21 or curl http://localhost:8080 to simulate connections.

Troubleshooting:
Port Conflicts: Ensure ports 21, 22, 8080, 2222 are free (run as admin if needed).
Missing JARs: Errors like ClassNotFoundException indicate missing libraries—add to classpath.
GeoIP Fails: Verify GeoLite2-City.mmdb path in GeoIPService.java.
UI Issues: Ensure JFreeChart and FlatLaf JARs are included.


Project Structure & File Descriptions
The source code is organized into packages for modularity. Below is a breakdown of each file:
/src/core/

ClientHandler.java: Generic handler for non-specific ports (e.g., 2222). Implements Runnable to echo client messages back, logs events via Honey.logEvent(), and handles basic I/O. Used as a fallback for unknown ports.
PortListener.java: Core listener class implementing Runnable. Binds to a specified port (e.g., via ServerSocket on 0.0.0.0), accepts incoming connections, logs new connections, updates the dashboard attack counter, and routes to specialized handlers (HTTP for 8080, FTP for 21, SSH for 22, or generic ClientHandler otherwise).

/src/handlers/

FTPHandler.java: FTP service emulator (port 21). Implements Runnable. Greets clients with a fake banner, prompts for username/password, logs credentials to ftp_credentials.log and DB, simulates failed login, and closes the connection. Includes timestamped logging and DB insertion via DBHelper.
HTTPHandler.java: HTTP service emulator (port 8080). Implements Runnable. Parses requests, detects admin scans (/admin), SQL/XSS injections in URLs/bodies, and brute-force logins (tracks attempts per IP). Serves a fake login HTML page. Handles POST /report for external alerts (e.g., JSON parsing with org.json). Logs alerts and inserts to DB.
SSHHandler.java: SSH service emulator (port 22). Implements Runnable. Sends a fake SSH banner, prompts for login credentials, captures and logs them to credentials_log.txt and DB. Tracks failed attempts per IP for brute-force detection (alerts after 3 tries). Denies access and closes.

/src/main/

Honey.java: Entry point (main method). Initializes the SQLite DB, GeoIP service, and dashboard. Starts threads for PortListener on ports [21, 22, 8080, 2222]. Provides static logEvent() for centralized logging (file, console, UI). Includes an example ExampleAttackHandler for simulating logs.

/src/ui/

Dashboard.java: Swing-based GUI extending JFrame. Uses FlatLaf for theming (light/dark toggle). Displays:
Total attack counter.
Per-port counters (21, 22, 8080, 2222) in a grid.
Real-time log textarea (auto-scrolls).
Pie chart (JFreeChart) for attack distribution by port.
Methods: addLog() for appending logs, updateAttack(port) for incrementing counters and refreshing UI.


/src/utils/

DBHelper.java: SQLite utility for logging. initializeDatabase() creates db/honeypot_logs.db and attack_logs table (columns: id, timestamp, ip_address, port, attack_type, message, location). insertLog() inserts records using prepared statements.
GeoIPService.java: Integrates MaxMind GeoIP2. init() loads GeoLite2-City.mmdb. lookup(ip) and getLocation(ip) return city/country or "Unknown" on failure. Used in handlers for attacker geolocation.

Other Files/Directories

db/: Auto-created for honeypot_logs.db.
Log Files: Generated at runtime (log.txt, ftp_credentials.log, credentials_log.txt).
lib/: Recommended folder for JAR dependencies.

Usage

Run the Honeypot: As per setup. It listens indefinitely.
Monitor Attacks:
GUI: Watch live updates in the dashboard.
Logs: tail -f log.txt or query DB with SQLite tools (e.g., sqlite3 db/honeypot_logs.db "SELECT * FROM attack_logs;").

Simulate Attacks:
SSH: ssh user@localhost -p 22 (enter fake creds).
FTP: ftp localhost 21.
HTTP: curl -X POST http://localhost:8080 -d "username=admin&password=pass".

External Integration: HTTP handler accepts JSON reports at /report (e.g., from other tools) for unified logging.
Stop: Ctrl+C or close GUI (exits all threads).

Example Output

Console: 2025-11-18 10:30:45 - Listening on port 21
Log: 2025-11-18 10:31:00 - FTP Connection from: 127.0.0.1
DB Query: Shows entries like timestamp: 2025-11-18 10:31:00, ip_address: 127.0.0.1, port: 21, attack_type: FTP, message: Login failed...

Contributing

Fork the repo, make changes, and submit a PR.
Focus on adding more service emulators (e.g., SMTP) or improving detection rules.
Report issues for bugs or enhancements.

License
This project is open-source under the MIT License. See LICENSE for details (add if not present).
Disclaimer
This honeypot is for educational/research purposes only. Deploy in isolated environments (e.g., VMs) to avoid legal issues. It does not provide real security—use production tools like Cowrie or Dionaea for advanced setups.
