package com.pragma.util;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

public class SnsClientProvider {
    private static final SnsClient INSTANCE = SnsClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    private SnsClientProvider() {}

    public static SnsClient getClient() {
        return INSTANCE;
    }
}