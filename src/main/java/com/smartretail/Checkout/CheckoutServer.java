package com.smartretail.Checkout;

import com.smartretail.InventoryServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class CheckoutServer {
    private Server server;
    private final int port;
    private final InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;

    public CheckoutServer(int port, InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub) {
        this.port = port;
        this.inventoryStub = inventoryStub;
    }

    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new CheckoutService(inventoryStub))
                .build()
                .start();
        System.out.println("CheckoutServer started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down CheckoutServer...");
            CheckoutServer.this.stop();
            System.out.println("CheckoutServer shut down.");
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
        int inventoryPort = 50051;
        int checkoutPort = 50052;

        InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub =
                InventoryServiceGrpc.newBlockingStub(io.grpc.ManagedChannelBuilder
                        .forAddress("localhost", inventoryPort)
                        .usePlaintext()
                        .build());

        CheckoutServer server = new CheckoutServer(checkoutPort, inventoryStub);
        server.start();
        server.blockUntilShutdown();
    }
}