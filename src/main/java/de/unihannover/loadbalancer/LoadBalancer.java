package de.unihannover.loadbalancer;

import de.unihannover.client.Client;
import de.unihannover.server.Server;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {
    private List<ServerInfo> servers;
    private Map<ServerInfo, Boolean> serverHealth;
    private int currentIndex = 0;

    public LoadBalancer() {
        servers = new ArrayList<>();
        serverHealth = new HashMap<>();

        // Initialize servers and mark them as healthy
        servers.add(new ServerInfo("localhost", 5001));
        servers.add(new ServerInfo("localhost", 5002));
        servers.add(new ServerInfo("localhost", 5003));

        for (ServerInfo server : servers) {
            serverHealth.put(server, true);
        }
    }

    /**
     * Get the next healthy server in a round-robin manner.
     * @return the next healthy server
     */
    public ServerInfo getNextHealthyServer() {
        for (int i = 0; i < servers.size(); i++) {
            ServerInfo server = servers.get(currentIndex);
            currentIndex = (currentIndex + 1) % servers.size(); // Move to the next server
            if (serverHealth.getOrDefault(server, false)) {
                return server;
            }
        }
        return null; // No healthy servers available
    }

    public static class ServerInfo {
        public String host;
        public int port;
        public ServerInfo(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    /**
     * Run tests to verify the load balancer implementation.
     * @throws InterruptedException if the thread is interrupted
     */
    public static void runTests() throws InterruptedException {
        LoadBalancer loadBalancer = new LoadBalancer();

        System.out.println("\n=== Running Load Balancer Tests ===");

        // TEST 1: Round-Robin Distribution
        System.out.println("\n[TEST 1] Round-Robin Distribution");
        List<String> expected = Arrays.asList("5001", "5002", "5003", "5001", "5002");
        List<String> actual = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ServerInfo server = loadBalancer.getNextHealthyServer();
            if (server != null) {
                System.out.println("Selected server: " + server.host + ":" + server.port);
                actual.add(String.valueOf(server.port));
            } else {
                System.out.println("No healthy server available.");
            }
        }
        System.out.println("Expected Order: " + expected);
        System.out.println("Actual Order:   " + actual);
        if (expected.equals(actual)) {
            System.out.println("[PASS] Round-Robin distribution works as expected.");
        } else {
            System.out.println("[FAIL] Round-Robin distribution failed!");
        }

        // TEST 2: Server Failure Handling
        System.out.println("\n[TEST 2] Simulating Server Failure");
        loadBalancer.serverHealth.put(loadBalancer.servers.get(0), false); // Mark server 5001 as unhealthy
        System.out.println("Marked server 5001 as unhealthy.");
        ServerInfo nextServer = loadBalancer.getNextHealthyServer();
        if (nextServer != null) {
            System.out.println("Selected server: " + nextServer.host + ":" + nextServer.port);
        }
        assert nextServer != null;
        if (nextServer.port != 5001) {
            System.out.println("[PASS] Unhealthy server was skipped.");
        } else {
            System.out.println("[FAIL] Unhealthy server was not skipped!");
        }

        // TEST 3: All Servers Down
        System.out.println("\n[TEST 3] All Servers Down");
        for (ServerInfo server : loadBalancer.servers) {
            loadBalancer.serverHealth.put(server, false);
            System.out.println("Marked server " + server.port + " as unhealthy.");
        }
        ServerInfo noServer = loadBalancer.getNextHealthyServer();
        if (noServer == null) {
            System.out.println("[PASS] No server selected when all servers are down.");
        } else {
            System.out.println("[FAIL] A server was selected despite all being down!");
        }

        // TEST 4: Measure Latency
        System.out.println("\n[TEST 4] Measure Latency");
        Server latencyTestServer = new Server(5001, true); //VERBOSE FLAG
        Thread latencyServerThread = new Thread(latencyTestServer::start);
        latencyServerThread.start();
        Thread.sleep(1000);
        long startTime = System.currentTimeMillis();
        try {
            Client.sendRequest("localhost", 5001, "Hello Server");
            long endTime = System.currentTimeMillis();
            System.out.println("Request Latency: " + (endTime - startTime) + " ms");
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
        latencyTestServer.stop();
        latencyServerThread.join();
        System.out.println("Latency test server stopped.");

        // TEST 5: Measure Throughput with Multiple Clients
        System.out.println("\n[TEST 5] Measure Throughput with Multiple Clients");
        Server throughputTestServer = new Server(5001, true); //VERBOSE FLAG
        Thread throughputServerThread = new Thread(throughputTestServer::start);
        throughputServerThread.start();
        Thread.sleep(1000);
        int numClients = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        startTime = System.currentTimeMillis();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numClients; i++) {
            executor.submit(() -> {
                try {
                    Client.sendRequest("localhost", 5001, "Throughput Test Message");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();

        System.out.println("Throughput Test Summary:");
        System.out.println("Total Clients: " + numClients);
        System.out.println("Successful Requests: " + successCount.get());
        System.out.println("Failed Requests: " + failureCount.get());
        System.out.println("Throughput: " + (successCount.get() / ((endTime - startTime) / 1000.0)) + " requests/second");

        throughputTestServer.stop();
        throughputServerThread.join();
        System.out.println("Throughput test server stopped.");

        // TEST 6: Fault Recovery Test with Latency Measurement
        System.out.println("\n[TEST 6] Simulate Fault Recovery with Latency Measurement");
        Server faultRecoveryServer1 = new Server(5001, false);
        Server faultRecoveryServer2 = new Server(5002, false);
        Thread serverThread1 = new Thread(faultRecoveryServer1::start);
        Thread serverThread2 = new Thread(faultRecoveryServer2::start);
        serverThread1.start();
        serverThread2.start();
        Thread.sleep(1000);

        loadBalancer = new LoadBalancer();
        System.out.println("All servers are initially healthy.");
        System.out.println("Marking server 5001 as unhealthy...");
        startTime = System.currentTimeMillis();
        loadBalancer.serverHealth.put(loadBalancer.servers.get(0), false);

        nextServer = loadBalancer.getNextHealthyServer();
        endTime = System.currentTimeMillis();
        long switchLatency = endTime - startTime;

        if (nextServer != null && nextServer.port != 5001) {
            System.out.println("[PASS] Requests correctly rerouted to server: " + nextServer.port);
            System.out.println("Switch Latency: " + switchLatency + " ms");
        } else {
            System.out.println("[FAIL] Requests were not rerouted correctly!");
        }
        faultRecoveryServer1.stop();
        faultRecoveryServer2.stop();
        serverThread1.join();
        serverThread2.join();
        System.out.println("Fault recovery test servers stopped.");


        System.out.println("\n=== Load Balancer Tests Completed ===");
    }
}
