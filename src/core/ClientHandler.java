package core;

import java.io.*;
import java.net.*;
import main.Honey;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private int port;

    public ClientHandler(Socket socket, int port) {
        this.socket = socket;
        this.port = port;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            writer.println("Welcome! You are connected to port " + port);
            System.out.println("Client connected on port " + port);

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                System.out.println("Received: " + clientMessage);
                Honey.logEvent("Received from " + socket.getInetAddress() + ": " + clientMessage);
                writer.println("Echo: " + clientMessage);
            }

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            Honey.logEvent("Connection error: " + e.getMessage());
        }
    }
}
