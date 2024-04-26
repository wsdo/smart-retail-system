package com.smartretail.Checkout;

import com.smartretail.CheckoutProto;
import com.smartretail.CheckoutServiceGrpc;
import com.smartretail.InventoryProto;
import com.smartretail.InventoryServiceGrpc;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;

public class CheckoutService extends CheckoutServiceGrpc.CheckoutServiceImplBase {
    private final InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;

    public CheckoutService(InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub) {
        this.inventoryStub = inventoryStub;
    }

    @Override
    public void checkout(CheckoutProto.CheckoutRequest request, StreamObserver<CheckoutProto.CheckoutResponse> responseObserver) {
        List<String> productIds = request.getProductIdsList();
        List<Integer> quantities = request.getQuantitiesList();

        double totalPrice = 0;
        List<String> purchasedProductIds = new ArrayList<>();
        List<Integer> purchasedQuantities = new ArrayList<>();

        for (int i = 0; i < productIds.size(); i++) {
            String productId = productIds.get(i);
            int quantity = quantities.get(i);

            InventoryProto.ProductRequest productRequest = InventoryProto.ProductRequest.newBuilder().setProductId(productId).build();
            InventoryProto.ProductResponse productResponse = inventoryStub.getProductInfo(productRequest);

            if (productResponse.getQuantity() >= quantity) {
                double price = productResponse.getPrice();
                totalPrice += price * quantity;
                purchasedProductIds.add(productId);
                purchasedQuantities.add(quantity);
            }
        }

        CheckoutProto.CheckoutResponse response = CheckoutProto.CheckoutResponse.newBuilder()
                .setTotalPrice(totalPrice)
                .addAllProductIds(purchasedProductIds)
                .addAllQuantities(purchasedQuantities)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<CheckoutProto.CheckoutRequest> checkoutStream(StreamObserver<CheckoutProto.CheckoutResponse> responseObserver) {
        return new StreamObserver<CheckoutProto.CheckoutRequest>() {
            double totalPrice = 0;
            List<String> productIds = new ArrayList<>();
            List<Integer> quantities = new ArrayList<>();

            @Override
            public void onNext(CheckoutProto.CheckoutRequest request) {
                List<String> requestProductIds = request.getProductIdsList();
                List<Integer> requestQuantities = request.getQuantitiesList();

                for (int i = 0; i < requestProductIds.size(); i++) {
                    String productId = requestProductIds.get(i);
                    int quantity = requestQuantities.get(i);

                    InventoryProto.ProductRequest productRequest = InventoryProto.ProductRequest.newBuilder().setProductId(productId).build();
                    InventoryProto.ProductResponse productResponse = inventoryStub.getProductInfo(productRequest);

                    if (productResponse.getQuantity() >= quantity) {
                        double price = productResponse.getPrice();
                        totalPrice += price * quantity;
                        productIds.add(productId);
                        quantities.add(quantity);
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                CheckoutProto.CheckoutResponse response = CheckoutProto.CheckoutResponse.newBuilder()
                        .setTotalPrice(totalPrice)
                        .addAllProductIds(productIds)
                        .addAllQuantities(quantities)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }
}