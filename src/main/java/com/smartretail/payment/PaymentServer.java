package com.smartretail.payment;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class PaymentServer {
    private Server server;
    private final int port;

    public PaymentServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new PaymentService())
                .build()
                .start();
        System.out.println("PaymentServer started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down PaymentServer...");
            PaymentServer.this.stop();
            System.out.println("PaymentServer shut down.");
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
        int port = 7002;
        PaymentServer server = new PaymentServer(port);
        server.start();

        // Register the service with Consul
        ConsulClient consulClient = new ConsulClient("localhost");
        NewService newService = new NewService();
        newService.setId("payment-service");
        newService.setName("payment-service");
        newService.setAddress("localhost");
        newService.setPort(port);
        consulClient.agentServiceRegister(newService);

        server.blockUntilShutdown();
    }
}
