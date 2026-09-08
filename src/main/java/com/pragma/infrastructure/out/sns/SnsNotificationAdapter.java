package com.pragma.infrastructure.out.sns;

import com.pragma.domain.spi.INotificationServicePort;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

public class SnsNotificationAdapter implements INotificationServicePort {

    private final SnsClient snsClient;
    private final String topicArn;

    public SnsNotificationAdapter(SnsClient snsClient, String topicArn) {
        this.snsClient = snsClient;
        this.topicArn = topicArn;
    }

    @Override
    public void sendNotification(String subject, String message) {
        PublishRequest publishRequest = PublishRequest.builder()
                .topicArn(topicArn)
                .subject(subject)
                .message(message)
                .build();

        snsClient.publish(publishRequest);
    }
}
