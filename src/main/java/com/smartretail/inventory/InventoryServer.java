package com.smartretail.inventory;


import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class InventoryServer {
    private Server server;
    private int port = 7001;

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
        int port = 7001;
        InventoryServer server = new InventoryServer(port);
        server.start();
        // 注册服务到Consul
        ConsulClient consulClient = new ConsulClient("localhost");
        NewService newService = new NewService();
        newService.setName("inventory-service");
        newService.setAddress("localhost");
        newService.setPort(port);
        consulClient.agentServiceRegister(newService);
        System.out.println("register to consul successful, listening on " + port);
        server.blockUntilShutdown();
    }
}
