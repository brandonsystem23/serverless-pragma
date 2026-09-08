package com.pragma.infrastructure.configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

public class SqsClientProvider {

    private static final SqsClient INSTANCE = SqsClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    private SqsClientProvider() {
    }

    public static SqsClient getClient() {
        return INSTANCE;
    }
}
