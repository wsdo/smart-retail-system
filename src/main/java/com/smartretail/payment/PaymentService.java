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

        // 在这里处理支付逻辑,这里只是一个简单的模拟
        boolean paymentSuccess = processPayment(amount, cardNumber, expiryDate, cvv);
        String transactionId = generateTransactionId();

        PaymentProto.PaymentResponse response = PaymentProto.PaymentResponse.newBuilder()
                .setSuccess(paymentSuccess)
                .setTransactionId(transactionId)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private boolean processPayment(double amount, String cardNumber, String expiryDate, String cvv) {
        // 模拟支付处理,假设支付总是成功
        return true;
    }

    private String generateTransactionId() {
        // 生成一个简单的事务ID,实际应用中可能需要更复杂的逻辑
        return "TXN" + System.currentTimeMillis();
    }
}