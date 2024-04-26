package com.smartretail.payment;

import com.smartretail.PaymentProto;
import com.smartretail.PaymentServiceGrpc;
import io.grpc.stub.StreamObserver;

public class PaymentService extends PaymentServiceGrpc.PaymentServiceImplBase {
    @Override
    public void processPayment(PaymentProto.PaymentRequest request, StreamObserver<PaymentProto.PaymentResponse> responseObserver) {
        double amount = request.getAmount();
        String cardNumber = request.getCardNumber();
        String expiryDate = request.getExpiryDate();
        String cvv = request.getCvv();

        // Process the payment logic here, this is just a simple simulation
        boolean paymentSuccess = processPayment(amount, cardNumber, expiryDate, cvv);
        String transactionId = generateTransactionId();

        PaymentProto.PaymentResponse response = PaymentProto.PaymentResponse.newBuilder()
                .setSuccess(paymentSuccess)
                .setTransactionId(transactionId)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<PaymentProto.PaymentRequest> processPaymentBidirectional(StreamObserver<PaymentProto.PaymentResponse> responseObserver) {
        return new StreamObserver<PaymentProto.PaymentRequest>() {
            @Override
            public void onNext(PaymentProto.PaymentRequest request) {
                double amount = request.getAmount();
                String cardNumber = request.getCardNumber();
                String expiryDate = request.getExpiryDate();
                String cvv = request.getCvv();

                // Process the payment request
                boolean paymentSuccess = processPayment(amount, cardNumber, expiryDate, cvv);
                String transactionId = generateTransactionId();

                // Send the payment response
                PaymentProto.PaymentResponse response = PaymentProto.PaymentResponse.newBuilder()
                        .setSuccess(paymentSuccess)
                        .setTransactionId(transactionId)
                        .build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private boolean processPayment(double amount, String cardNumber, String expiryDate, String cvv) {
        // Simulate payment processing, assume payment is always successful
        return true;
    }

    private String generateTransactionId() {
        // Generate a simple transaction ID, in a real application, more complex logic may be needed
        return "TXN" + System.currentTimeMillis();
    }
}