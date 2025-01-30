package de.unihannover.server;

import java.io.*;
import java.net.*;

public class Server {
    private int port;
    private volatile boolean running = true;
    private boolean verbose;

    public Server(int port) {
        this.port = port;
        this.verbose = true;
    }

    /**
     * Create a new server with the specified port and verbosity for debugging.
     * @param port the port to listen on
     * @param verbose whether to print verbose output (true or false)
     */
    public Server(int port, boolean verbose) {
        this.port = port;
        this.verbose = verbose;
    }

    /**
     * Start the server and handle incoming client connections.
     */
    public void start() {
        System.out.println("Starting server on port " + port + "...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server successfully started and listening on port " + port);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    /**
     * Stop the server and close the server socket.
     */
    public void stop() {
        running = false;
        try (Socket socket = new Socket("localhost", port)) {
        } catch (IOException ignored) {}
        System.out.println("Server stopped.");
    }


    /**
     * Handle a client connection by reading a message and sending a response.
     * @param clientSocket the client socket to handle
     */
    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String message = input.readLine();
            if (verbose) {
                System.out.println("Received from client: " + message);
            }
            output.println("Response from Server on port " + port);
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }


    /**
     * Run the server tests.
     */
    public static void runTests() {
        System.out.println("Starting Server Tests...");

        Server server = new Server(5001, false); // Ensure verbose mode is ON for debugging
        Thread serverThread = new Thread(server::start);
        serverThread.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try (Socket socket = new Socket("localhost", 5001)) {
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            output.println("Hello Server");
            String response = input.readLine();
            System.out.println("Client received: " + response);

            assert response.equals("Response from Server on port 5001") : "Test 1 Failed: Invalid server response";
            System.out.println("Server Test 1 Passed: Server responded correctly.");
        } catch (IOException e) {
            System.err.println("Server Test 1 Failed: " + e.getMessage());
        }

        server.stop();
    }
}

