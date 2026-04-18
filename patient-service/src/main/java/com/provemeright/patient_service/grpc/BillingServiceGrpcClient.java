package com.provemeright.patient_service.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * ============================================================================
 * gRPC CLIENT (PATIENT SERVICE -> BILLING SERVICE)
 * ============================================================================
 *
 * This class is responsible for sending gRPC requests FROM the Patient Service
 * TO the Billing Service.
 *
 * WHY A SEPARATE CLIENT CLASS?
 * ----------------------------
 * It abstracts away all the gRPC networking complexities (channels, stubs).
 * To the rest of the Patient Service (like PatientController), this just looks
 * like a normal Spring @Service. They call `createBillingAccount()` and get a
 * response, completely unaware that a gRPC network call is happening over HTTP/2.
 */
@Service
public class BillingServiceGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);
    
    /**
     * THE gRPC STUB
     * -------------
     * A "Stub" is the local object representing the remote service.
     * "BlockingStub" means it operates synchronously (it waits for the response
     * before continuing to the next line of code), which is exactly what we want
     * in our standard REST controller flow.
     */
    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    /**
     * CONSTRUCTOR INJECTION & gRPC CHANNEL SETUP
     * ------------------------------------------
     * @Value("${billing.service.address:localhost}") extracts the target address
     * from application.properties. If not found, it defaults to 'localhost'.
     *
     * A ManagedChannel is the underlying TCP connection to the gRPC server.
     * Creating channels is expensive, so we create ONE channel when this bean
     * is initialized and reuse it for all requests (gRPC multiplexes multiple
     * requests over single HTTP/2 connection).
     */
    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.service.grpc.port:9001}") int serverPort
    ){
        log.info("Connecting to Billing service GRPC service at {} : {}", serverAddress, serverPort);

        // usePlaintext(): Disables TLS/SSL. NEVER do this in production unless
        // you are inside a secure private VPC. We're using it here for local dev.
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext().build();

        // Create the stub using the configured channel
        blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    /**
     * EXECUTE THE gRPC CALL
     * ---------------------
     * This method translates standard Java parameters into a Protobuf message,
     * sends it to the Billing Service, and returns the response.
     */
    public BillingResponse createBillingAccount(String patientId, String name, String email) {
        
        // Use the Protobuf Builder pattern to construct the payload
        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId(patientId)
                .setName(name)
                .setEmail(email)
                .build();

        // 🚨 NETWORK CALL: This executes the inter-service communication!
        // It's blocking — thread pauses here until Billing Service replies.
        BillingResponse response = blockingStub.createBillingAccount(request);
        
        log.info("Received response from billing service via GRPC: {}", response);
        return response;
    }
}
