package de.unihannover;

import de.unihannover.loadbalancer.LoadBalancer;
import de.unihannover.server.Server;
import de.unihannover.client.Client;

public class MainApp {

    /**
     * Main entry point of the application.
     * @param args the command line arguments
     * */
    public static void main(String[] args) {
        System.out.println("Welcome to the Load Balancer and Server System!");
        System.out.println("Select an option:");
        System.out.println("1. Run Server Tests");
        System.out.println("2. Run Load Balancer Tests");
        System.out.println("3. Start the Server");
        System.out.println("4. Send a Request to the Server");
        System.out.println("5. Exit");

        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            Thread serverThread = null; // To manage the server thread
            while (true) {
                System.out.print("\nEnter an option: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1 -> {
                        System.out.println("\nRunning Server Tests...");
                        Server.runTests();
                    }
                    case 2 -> {
                        System.out.println("\nRunning Load Balancer Tests...");
                        LoadBalancer.runTests();
                    }
                    case 3 -> {
                        System.out.println("\nStarting the Server...");
                        if (serverThread == null || !serverThread.isAlive()) {
                            serverThread = new Thread(() -> {
                                Server server = new Server(5002);
                                server.start(); // Runs in a separate thread
                            });
                            serverThread.start();
                        } else {
                            System.out.println("Server is already running!");
                        }
                    }
                    case 4 -> {
                        System.out.println("\nSending a Request to the Server...");
                        System.out.print("Enter the message to send: ");
                        scanner.nextLine();
                        String message = scanner.nextLine();
                        Client.sendRequest("localhost", 5002, message);
                    }
                    case 5 -> {
                        System.out.println("\nExiting the application. Goodbye!");
                        if (serverThread != null && serverThread.isAlive()) {
                            serverThread.interrupt();
                            System.out.println("Server stopped.");
                        }
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
