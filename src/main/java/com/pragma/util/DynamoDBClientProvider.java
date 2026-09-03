package com.pragma.util;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDBClientProvider {

    private static final DynamoDbClient INSTANCE = DynamoDbClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    private DynamoDBClientProvider() {
    }

    public static DynamoDbClient getClient() {
        return INSTANCE;
    }
}