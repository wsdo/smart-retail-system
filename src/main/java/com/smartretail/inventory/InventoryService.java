package com.smartretail.inventory;

import com.smartretail.InventoryProto;
import com.smartretail.InventoryServiceGrpc;
import io.grpc.stub.StreamObserver;
import java.util.HashMap;
import java.util.Map;

public class InventoryService extends InventoryServiceGrpc.InventoryServiceImplBase {
    private final Map<String, ProductInfo> inventory;

    public InventoryService() {
        inventory = new HashMap<>();
        // 添加一些示例商品信息
        inventory.put("1", ProductInfo.newBuilder().setProductId("1").setProductName("商品1").setPrice(10.0).setQuantity(100).build());
        inventory.put("2", ProductInfo.newBuilder().setProductId("2").setProductName("商品2").setPrice(20.0).setQuantity(50).build());
        inventory.put("3", ProductInfo.newBuilder().setProductId("3").setProductName("商品3").setPrice(30.0).setQuantity(30).build());
    }

    @Override
    public void getProductInfo(InventoryProto.ProductRequest request, StreamObserver<InventoryProto.ProductResponse> responseObserver) {
        String productId = request.getProductId();
        ProductInfo productInfo = inventory.get(productId);

        if (productInfo != null) {
            InventoryProto.ProductResponse response = InventoryProto.ProductResponse.newBuilder()
                    .setProductId(productInfo.getProductId())
                    .setProductName(productInfo.getProductName())
                    .setPrice(productInfo.getPrice())
                    .setQuantity(productInfo.getQuantity())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new RuntimeException("Product not found: " + productId));
        }
    }

    // 内部类,表示商品信息
    private static class ProductInfo {
        private String productId;
        private String productName;
        private double price;
        private int quantity;

        private ProductInfo(String productId, String productName, double price, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        // 内部静态类,用于构建ProductInfo
        public static class Builder {
            private String productId;
            private String productName;
            private double price;
            private int quantity;

            public Builder setProductId(String productId) {
                this.productId = productId;
                return this;
            }

            public Builder setProductName(String productName) {
                this.productName = productName;
                return this;
            }

            public Builder setPrice(double price) {
                this.price = price;
                return this;
            }

            public Builder setQuantity(int quantity) {
                this.quantity = quantity;
                return this;
            }

            public ProductInfo build() {
                return new ProductInfo(productId, productName, price, quantity);
            }
        }

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