package com.smartretail.inventory;

import com.smartretail.InventoryProto;
import com.smartretail.InventoryServiceGrpc;
import io.grpc.stub.StreamObserver;
import java.util.HashMap;
import java.util.Map;

/**
 * The InventoryService class is an implementation of the InventoryServiceGrpc service.
 * It provides methods to handle inventory-related requests using gRPC.
 */
public class InventoryService extends InventoryServiceGrpc.InventoryServiceImplBase {
    // A map to store product information, keyed by product ID.
    private final Map<String, ProductInfo> inventory;

    /**
     * Constructor for InventoryService.
     * Initializes the inventory map with some predefined product information.
     */
    public InventoryService() {
        inventory = new HashMap<>();
        // Adding some products to the inventory for demonstration purposes.
        inventory.put("1", ProductInfo.newBuilder()
                .setProductId("1")
                .setProductName("Product 1")
                .setPrice(10.0)
                .setQuantity(100)
                .build());
        inventory.put("2", ProductInfo.newBuilder()
                .setProductId("2")
                .setProductName("Product 2")
                .setPrice(20.0)
                .setQuantity(50)
                .build());
        inventory.put("3", ProductInfo.newBuilder()
                .setProductId("3")
                .setProductName("Product 3")
                .setPrice(30.0)
                .setQuantity(30)
                .build());
    }

    /**
     * Implementation of the getProductInfo RPC method. This method is called when a client requests
     * information for a specific product by its ID.
     *
     * @param request   The ProductRequest message containing the product ID.
     * @param responseObserver   The StreamObserver to send the response or error back to the client.
     */
    @Override
    public void getProductInfo(InventoryProto.ProductRequest request, StreamObserver<InventoryProto.ProductResponse> responseObserver) {
        String productId = request.getProductId();
        ProductInfo productInfo = inventory.get(productId);

        if (productInfo != null) {
            // If the product is found, build a response and send it to the client.
            InventoryProto.ProductResponse response = InventoryProto.ProductResponse.newBuilder()
                    .setProductId(productInfo.getProductId())
                    .setProductName(productInfo.getProductName())
                    .setPrice(productInfo.getPrice())
                    .setQuantity(productInfo.getQuantity())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            // If the product is not found, send an error to the client.
            responseObserver.onError(new RuntimeException("Product not found: " + productId));
        }
    }

    /**
     * Implementation of the getProductList RPC method. This method is called when a client requests
     * a list of product information for multiple products by their IDs.
     *
     * @param request   The ProductListRequest message containing a list of product IDs.
     * @param responseObserver   The StreamObserver to send the response or error back to the client.
     */
    @Override
    public void getProductList(InventoryProto.ProductListRequest request, StreamObserver<InventoryProto.ProductResponse> responseObserver) {
        for (String productId : request.getProductIdsList()) {
            ProductInfo productInfo = inventory.get(productId);
            if (productInfo != null) {
                // For each product ID, if the product is found, build a response and send it to the client.
                InventoryProto.ProductResponse response = InventoryProto.ProductResponse.newBuilder()
                        .setProductId(productInfo.getProductId())
                        .setProductName(productInfo.getProductName())
                        .setPrice(productInfo.getPrice())
                        .setQuantity(productInfo.getQuantity())
                        .build();
                responseObserver.onNext(response);
            }
        }
        // Once all products have been processed, complete the stream.
        responseObserver.onCompleted();
    }

    /**
     * The ProductInfo class represents the details of a product in the inventory.
     */
    private static class ProductInfo {
        private String productId;
        private String productName;
        private double price;
        private int quantity;

        /**
         * Constructor for ProductInfo.
         *
         * @param productId   The ID of the product.
         * @param productName  The name of the product.
         * @param price        The price of the product.
         * @param quantity     The quantity of the product in stock.
         */
        private ProductInfo(String productId, String productName, double price, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
        }

        /**
         * Static method to create a new Builder instance for building ProductInfo objects.
         * @return A new Builder instance.
         */
        public static Builder newBuilder() {
            return new Builder();
        }

        /**
         * The Builder inner class is used to construct ProductInfo objects in a fluent manner.
         */
        public static class Builder {
            private String productId;
            private String productName;
            private double price;
            private int quantity;

            /**
             * Sets the product ID and returns the Builder instance for chaining.
             *
             * @param productId   The ID of the product to set.
             * @return The Builder instance for chaining.
             */
            public Builder setProductId(String productId) {
                this.productId = productId;
                return this;
            }

            /**
             * Sets the product name and returns the Builder instance for chaining.
             *
             * @param productName  The name of the product to set.
             * @return The Builder instance for chaining.
             */
            public Builder setProductName(String productName) {
                this.productName = productName;
                return this;
            }

            /**
             * Sets the product price and returns the Builder instance for chaining.
             *
             * @param price        The price of the product to set.
             * @return The Builder instance for chaining.
             */
            public Builder setPrice(double price) {
                this.price = price;
                return this;
            }

            /**
             * Sets the product quantity and returns the Builder instance for chaining.
             *
             * @param quantity     The quantity of the product to set.
             * @return The Builder instance for chaining.
             */
            public Builder setQuantity(int quantity) {
                this.quantity = quantity;
                return this;
            }

            /**
             * Builds and returns a new ProductInfo object with the current builder settings.
             *
             * @return A new ProductInfo object.
             */
            public ProductInfo build() {
                return new ProductInfo(productId, productName, price, quantity);
            }
        }

        // Getters for the product information fields.
        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}