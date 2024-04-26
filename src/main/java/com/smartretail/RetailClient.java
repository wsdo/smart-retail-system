package com.smartretail;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RetailClient extends Application {
    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;
    private InventoryServiceGrpc.InventoryServiceStub inventoryAsyncStub;
    private CheckoutServiceGrpc.CheckoutServiceBlockingStub checkoutStub;
    private CheckoutServiceGrpc.CheckoutServiceStub checkoutAsyncStub;
    private PaymentServiceGrpc.PaymentServiceBlockingStub paymentStub;
    private PaymentServiceGrpc.PaymentServiceStub paymentAsyncStub;

    private List<String> cart = new ArrayList<>();
    private List<Double> prices = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smart Retail Client");

        GridPane grid = createGrid();
        Scene scene = new Scene(grid, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        ManagedChannel inventoryChannel = ManagedChannelBuilder.forAddress("localhost", 7001).usePlaintext().build();
        ManagedChannel checkoutChannel = ManagedChannelBuilder.forAddress("localhost", 7003).usePlaintext().build();
        ManagedChannel paymentChannel = ManagedChannelBuilder.forAddress("localhost", 7002).usePlaintext().build();

        inventoryStub = InventoryServiceGrpc.newBlockingStub(inventoryChannel);
        inventoryAsyncStub = InventoryServiceGrpc.newStub(inventoryChannel);
        checkoutStub = CheckoutServiceGrpc.newBlockingStub(checkoutChannel);
        checkoutAsyncStub = CheckoutServiceGrpc.newStub(checkoutChannel);
        paymentStub = PaymentServiceGrpc.newBlockingStub(paymentChannel);
        paymentAsyncStub = PaymentServiceGrpc.newStub(paymentChannel);
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        Label productIdLabel = new Label("Product ID:");
        TextField productIdField = new TextField();
        Button addToCartButton = new Button("Add to Cart");
        Label productListLabel = new Label("Product List:");
        TextField productListField = new TextField();
        Button getProductListButton = new Button("Get Product List");
        ListView<String> cartListView = new ListView<>();
        Button checkoutButton = new Button("Checkout");
        Button streamCheckoutButton = new Button("Stream Checkout");
        Button bidirectionalPaymentButton = new Button("Bidirectional Payment");
        Label resultLabel = new Label();

        GridPane.setConstraints(productIdLabel, 0, 0);
        GridPane.setConstraints(productIdField, 1, 0);
        GridPane.setConstraints(addToCartButton, 2, 0);
        GridPane.setConstraints(productListLabel, 0, 1);
        GridPane.setConstraints(productListField, 1, 1);
        GridPane.setConstraints(getProductListButton, 2, 1);
        GridPane.setConstraints(cartListView, 0, 2, 3, 1);
        GridPane.setConstraints(checkoutButton, 0, 3);
        GridPane.setConstraints(streamCheckoutButton, 1, 3);
        GridPane.setConstraints(bidirectionalPaymentButton, 2, 3);
        GridPane.setConstraints(resultLabel, 0, 4, 3, 1);

        grid.getChildren().addAll(
                productIdLabel, productIdField, addToCartButton,
                productListLabel, productListField, getProductListButton,
                cartListView, checkoutButton, streamCheckoutButton, bidirectionalPaymentButton,
                resultLabel);

        addToCartButton.setOnAction(e -> addToCart(productIdField.getText(), cartListView));
        getProductListButton.setOnAction(e -> getProductList(productListField.getText(), cartListView));
        checkoutButton.setOnAction(e -> checkout(cartListView, resultLabel));
        streamCheckoutButton.setOnAction(e -> streamCheckout(cartListView, resultLabel));
        bidirectionalPaymentButton.setOnAction(e -> bidirectionalPayment(resultLabel));

        return grid;
    }

    private void addToCart(String productId, ListView<String> cartListView) {
        InventoryProto.ProductRequest request = InventoryProto.ProductRequest.newBuilder().setProductId(productId).build();
        InventoryProto.ProductResponse response = inventoryStub.getProductInfo(request);
        cart.add(productId);
        prices.add(response.getPrice());
        Platform.runLater(() -> {
            cartListView.getItems().add(response.getProductName() + " - $" + response.getPrice());
        });
    }

    private void getProductList(String productIds, ListView<String> cartListView) {
        InventoryProto.ProductListRequest.Builder requestBuilder = InventoryProto.ProductListRequest.newBuilder();
        for (String productId : productIds.split(",")) {
            requestBuilder.addProductIds(productId.trim());
        }
        Iterator<InventoryProto.ProductResponse> productResponses = inventoryStub.getProductList(requestBuilder.build());
        Platform.runLater(() -> {
            while (productResponses.hasNext()) {
                InventoryProto.ProductResponse response = productResponses.next();
                cartListView.getItems().add(response.getProductName() + " - $" + response.getPrice());
            }
        });
    }

    private void checkout(ListView<String> cartListView, Label resultLabel) {
        CheckoutProto.CheckoutRequest.Builder requestBuilder = CheckoutProto.CheckoutRequest.newBuilder();
        for (int i = 0; i < cart.size(); i++) {
            requestBuilder.addProductIds(cart.get(i));
            requestBuilder.addQuantities(1);
        }
        CheckoutProto.CheckoutResponse checkoutResponse = checkoutStub.checkout(requestBuilder.build());
        processPaymentAndDisplayResult(checkoutResponse.getTotalPrice(), resultLabel);
        cart.clear();
        prices.clear();
        Platform.runLater(() -> {
            cartListView.getItems().clear();
        });
    }

    private void streamCheckout(ListView<String> cartListView, Label resultLabel) {
        StreamObserver<CheckoutProto.CheckoutResponse> responseObserver = new StreamObserver<CheckoutProto.CheckoutResponse>() {
            @Override
            public void onNext(CheckoutProto.CheckoutResponse response) {
                processPaymentAndDisplayResult(response.getTotalPrice(), resultLabel);
            }

            @Override
            public void onError(Throwable t) {
                Platform.runLater(() -> {
                    resultLabel.setText("Error: " + t.getMessage());
                });
            }

            @Override
            public void onCompleted() {
                Platform.runLater(() -> {
                    cart.clear();
                    prices.clear();
                    cartListView.getItems().clear();
                });
            }
        };

        StreamObserver<CheckoutProto.CheckoutRequest> requestObserver = checkoutAsyncStub.checkoutStream(responseObserver);
        for (int i = 0; i < cart.size(); i++) {
            CheckoutProto.CheckoutRequest request = CheckoutProto.CheckoutRequest.newBuilder()
                    .addProductIds(cart.get(i))
                    .addQuantities(1)
                    .build();
            requestObserver.onNext(request);
        }
        requestObserver.onCompleted();
    }

    private void bidirectionalPayment(Label resultLabel) {
        StreamObserver<PaymentProto.PaymentResponse> responseObserver = new StreamObserver<PaymentProto.PaymentResponse>() {
            @Override
            public void onNext(PaymentProto.PaymentResponse response) {
                Platform.runLater(() -> {
                    if (response.getSuccess()) {
                        resultLabel.setText("Payment successful. Transaction ID: " + response.getTransactionId());
                    } else {
                        resultLabel.setText("Payment failed.");
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                Platform.runLater(() -> {
                    resultLabel.setText("Error: " + t.getMessage());
                });
            }

            @Override
            public void onCompleted() {
                Platform.runLater(() -> {
                    resultLabel.setText("Payment completed.");
                });
            }
        };

        StreamObserver<PaymentProto.PaymentRequest> requestObserver = paymentAsyncStub.processPaymentBidirectional(responseObserver);
        for (double amount : prices) {
            PaymentProto.PaymentRequest request = PaymentProto.PaymentRequest.newBuilder()
                    .setAmount(amount)
                    .setCardNumber("1234567812345678")
                    .setExpiryDate("12/25")
                    .setCvv("123")
                    .build();
            requestObserver.onNext(request);
        }
        requestObserver.onCompleted();
    }

    private void processPaymentAndDisplayResult(double amount, Label resultLabel) {
        PaymentProto.PaymentRequest paymentRequest = PaymentProto.PaymentRequest.newBuilder()
                .setAmount(amount)
                .setCardNumber("1234567812345678")
                .setExpiryDate("12/25")
                .setCvv("123")
                .build();
        PaymentProto.PaymentResponse paymentResponse = paymentStub.processPayment(paymentRequest);

        Platform.runLater(() -> {
            if (paymentResponse.getSuccess()) {
                resultLabel.setText("Checkout successful. Total: $" + amount + ". Transaction ID: " + paymentResponse.getTransactionId());
            } else {
                resultLabel.setText("Checkout failed.");
            }
        });
    }
}