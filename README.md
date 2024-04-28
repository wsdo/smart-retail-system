

# Smart Retail System Setup Guide

Welcome to the Smart Retail System! Follow this guide to get your services up and running.

## Prerequisites

Before you begin, ensure you have the following installed:
- Consul: A service mesh solution providing a full-featured control plane with service discovery, configuration, and segmentation functionality.
- Java: Required to run the services and GUI.

## Step 1: Start Consul

First, we need to start the Consul agent. Open a terminal and run:

```sh
consul agent -dev
```

This command starts Consul in development mode. For production, you'll need a more secure setup.

## Step 2: Access Consul GUI

To verify that Consul is running and to view your services once they are registered, open the following URL in your web browser:

```
http://localhost:8500/
```

This is the Consul dashboard where you can monitor your services.

## Step 3: Run Services

Now, let's get your Smart Retail services up and running. In separate terminal windows, start each service by running its corresponding script.

For the Checkout Service:
```bash
run CheckoutServer
```

For the Inventory Service:
```bash
run InventoryService
```

For the Payment Service:
```bash
run PaymentService
```

Ensure that each service starts without errors and registers with Consul.

## Step 4: Launch GUI

Finally, to interact with your system, start the retail client application:

```bash
run RetailClientApp
```

After running the command, the graphical user interface should open, allowing you to interact with the Smart Retail System.

---

# Troubleshooting

If you encounter any issues:
- Verify that Java is correctly installed and included in your system's PATH.
- Ensure that Consul's dashboard shows the services as 'Healthy'.
- Check the terminal output for any error messages when starting the services.
- Confirm that all services are running on their designated ports.

For further assistance, please refer to the 'Troubleshooting' section in our documentation or submit an issue on this repository.

Thank you for choosing Smart Retail System, where intelligent retailing begins!