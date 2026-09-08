package com.pragma.infrastructure.out.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.domain.model.User;
import com.pragma.domain.spi.IQueueServicePort;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SqsQueueAdapter implements IQueueServicePort {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SqsClient sqsClient;
    private final String queueUrl;

    public SqsQueueAdapter(SqsClient sqsClient, String queueUrl) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Override
    public void sendUserCreated(User user) {
        try {
            String userJson = MAPPER.writeValueAsString(user);

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(userJson)
                    .build();

            sqsClient.sendMessage(sendMessageRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error publicando usuario en SQS", e);
        }
    }
}
