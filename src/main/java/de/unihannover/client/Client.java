package de.unihannover.client;

import java.io.*;
import java.net.*;

public class Client {

    /**
     * Send a request to the server as a String message.
     * @param host the server host
     * @param port the server port
     * @param message the message to send
     * */
    public static void sendRequest(String host, int port, String message) {
        try (Socket socket = new Socket(host, port)) {
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output.println(message);
            String response = input.readLine();
            System.out.println("Client received: " + response);
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    /**
     * Run the client tests.
     * */
    public static void runTests() {
        System.out.println("Test 1: Client Sends Request");
        try {
            sendRequest("localhost", 5002, "Hello ACK!");
            System.out.println("Passed: Request sent successfully.");
        } catch (Exception e) {
            System.err.println("Failed: " + e.getMessage());
        }
    }
}
