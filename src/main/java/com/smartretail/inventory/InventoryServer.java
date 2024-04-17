package com.smartretail.inventory;


import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class InventoryServer {
    private Server server;
    private final int port;

    public InventoryServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new InventoryService())
                .build()
                .start();
        System.out.println("InventoryServer started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down InventoryServer...");
            InventoryServer.this.stop();
            System.out.println("InventoryServer shut down.");
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50051;
        InventoryServer server = new InventoryServer(port);
        server.start();
        server.blockUntilShutdown();
    }
}
