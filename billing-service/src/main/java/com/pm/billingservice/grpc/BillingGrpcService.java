package com.pm.billingservice.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC Service Implementation for Billing Operations.
 * 
 * Exposes synchronous gRPC endpoints over HTTP/2. This service is primarily 
 * consumed by the Patient Service to provision new billing accounts as part 
 * of the patient onboarding workflow.
 */
@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    /**
     * Handles the creation of a new billing account via gRPC.
     *
     * @param billingRequest The incoming request payload
     * @param responseObserver The channel used to send the response back to the client
     */
    @Override
    public void createBillingAccount(billing.BillingRequest billingRequest,
                                     StreamObserver<billing.BillingResponse> responseObserver){
        
        log.info("createBillingAccount request received: {}", billingRequest.toString());

        // Simulated business logic. In a real application:
        // 1. Verify the patient exists
        // 2. Create a new BillingAccount entity
        // 3. Save it to the database
        // 4. Return the generated Account ID

        billing.BillingResponse response = billing.BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("ACTIVE")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
