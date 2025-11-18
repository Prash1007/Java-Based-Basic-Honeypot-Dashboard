package core;

import handlers.FTPHandler;
import handlers.HTTPHandler;
import handlers.SSHHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import main.Honey;
import ui.Dashboard;

public class PortListener implements Runnable {

    private int port;

    public PortListener(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(this.port, 0, InetAddress.getByName("0.0.0.0"))) {

            System.out.println("Listening on port " + this.port);
            Honey.logEvent("Listening on port " + this.port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientAddress = clientSocket.getInetAddress().toString();

                System.out.println("New connection on port " + this.port + " from: " + clientAddress);
                Honey.logEvent("New connection on port " + this.port + " from: " + clientAddress);

                // Call the method to update the attack count in the dashboard
                Dashboard dashboard = Honey.getDashboard();
                if (dashboard != null) {
                    dashboard.updateAttack(this.port);  // Update the attack count for the respective port
                }

                // Handle connection based on port type
                if (this.port == 8080) {
                    new Thread(new HTTPHandler(clientSocket)).start();
                } else if (this.port == 21) {
                    new Thread(new FTPHandler(clientSocket)).start();
                } else if (this.port == 22) {
                    new Thread(new SSHHandler(clientSocket)).start();
                } else {
                    new Thread(new ClientHandler(clientSocket, this.port)).start();
                }
            }
        } catch (IOException e) {
            System.err.println("Error on port " + this.port + ": " + e.getMessage());
            Honey.logEvent("Error on port " + this.port + ": " + e.getMessage());
        }
    }
}
