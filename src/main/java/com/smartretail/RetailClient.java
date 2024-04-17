package com.smartretail;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class RetailClient extends Application {
    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;
    private CheckoutServiceGrpc.CheckoutServiceBlockingStub checkoutStub;
    private PaymentServiceGrpc.PaymentServiceBlockingStub paymentStub;

    private List<String> cart = new ArrayList<>();
    private List<Double> prices = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smart Retail Client");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        Label productIdLabel = new Label("Product ID:");
        TextField productIdField = new TextField();
        Button addToCartButton = new Button("Add to Cart");
        ListView<String> cartListView = new ListView<>();
        Button checkoutButton = new Button("Checkout");
        Label resultLabel = new Label();

        GridPane.setConstraints(productIdLabel, 0, 0);
        GridPane.setConstraints(productIdField, 1, 0);
        GridPane.setConstraints(addToCartButton, 2, 0);
        GridPane.setConstraints(cartListView, 0, 1, 3, 1);
        GridPane.setConstraints(checkoutButton, 0, 2);
        GridPane.setConstraints(resultLabel, 0, 3, 3, 1);

        grid.getChildren().addAll(productIdLabel, productIdField, addToCartButton, cartListView, checkoutButton, resultLabel);

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

        ManagedChannel inventoryChannel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
        ManagedChannel checkoutChannel = ManagedChannelBuilder.forAddress("localhost", 50052).usePlaintext().build();
        ManagedChannel paymentChannel = ManagedChannelBuilder.forAddress("localhost", 50053).usePlaintext().build();

        inventoryStub = InventoryServiceGrpc.newBlockingStub(inventoryChannel);
        checkoutStub = CheckoutServiceGrpc.newBlockingStub(checkoutChannel);
        paymentStub = PaymentServiceGrpc.newBlockingStub(paymentChannel);

        addToCartButton.setOnAction(e -> addToCart(productIdField.getText(), cartListView));
        checkoutButton.setOnAction(e -> checkout(cartListView, resultLabel));

    }

    private void addToCart(String productId, ListView<String> cartListView) {
        InventoryProto.ProductRequest request = InventoryProto.ProductRequest.newBuilder().setProductId(productId).build();
        InventoryProto.ProductResponse response = inventoryStub.getProductInfo(request);
        cart.add(productId);
        prices.add(response.getPrice());
        cartListView.getItems().add(response.getProductName() + " - $" + response.getPrice());
    }

    private void checkout(ListView<String> cartListView, Label resultLabel) {
        CheckoutProto.CheckoutRequest.Builder requestBuilder = CheckoutProto.CheckoutRequest.newBuilder();
        for (int i = 0; i < cart.size(); i++) {
            requestBuilder.addProductIds(cart.get(i));
            requestBuilder.addQuantities(1);
        }
        CheckoutProto.CheckoutResponse checkoutResponse = checkoutStub.checkout(requestBuilder.build());

        double amount = checkoutResponse.getTotalPrice();
        PaymentProto.PaymentRequest paymentRequest = PaymentProto.PaymentRequest.newBuilder()
                .setAmount(amount)
                .setCardNumber("1234567812345678")
                .setExpiryDate("12/25")
                .setCvv("123")
                .build();
        PaymentProto.PaymentResponse paymentResponse = paymentStub.processPayment(paymentRequest);

        if (paymentResponse.getSuccess()) {
            resultLabel.setText("Checkout successful. Total: $" + amount + ". Transaction ID: " + paymentResponse.getTransactionId());
            cart.clear();
            prices.clear();
            cartListView.getItems().clear();
        } else {
            resultLabel.setText("Checkout failed.");
        }
    }
}