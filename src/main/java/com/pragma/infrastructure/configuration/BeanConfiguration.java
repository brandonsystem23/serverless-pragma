package com.pragma.infrastructure.configuration;

import com.pragma.application.handler.IUserHandler;
import com.pragma.application.handler.impl.UserHandlerImpl;
import com.pragma.application.mapper.UserDtoMapper;
import com.pragma.application.dto.request.UserRequest;
import com.pragma.domain.api.IUserServicePort;
import com.pragma.domain.model.User;
import com.pragma.domain.spi.INotificationServicePort;
import com.pragma.domain.spi.IQueueServicePort;
import com.pragma.domain.spi.IUserPersistencePort;
import com.pragma.domain.usecase.UserUseCase;
import com.pragma.infrastructure.out.dynamodb.UserDynamoDbAdapter;
import com.pragma.infrastructure.out.sns.SnsNotificationAdapter;
import com.pragma.infrastructure.out.sqs.SqsQueueAdapter;

public class BeanConfiguration {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");
    private static final String QUEUE_URL = System.getenv("QUEUE_URL");
    private static final String TOPIC_ARN = System.getenv("TOPIC_ARN");

    private BeanConfiguration() {
    }

    public static IUserHandler userHandler() {
        IUserPersistencePort persistencePort = new UserDynamoDbAdapter(
                DynamoDBClientProvider.getClient(),
                TABLE_NAME
        );

        IQueueServicePort queueServicePort = new SqsQueueAdapter(
                SqsClientProvider.getClient(),
                QUEUE_URL
        );

        INotificationServicePort notificationServicePort = new SnsNotificationAdapter(
                SnsClientProvider.getClient(),
                TOPIC_ARN
        );

        IUserServicePort userServicePort = new UserUseCase(
                persistencePort,
                queueServicePort,
                notificationServicePort
        );

        UserDtoMapper mapper = new UserDtoMapper() {
            @Override
            public User toUser(UserRequest request) {
                if (request == null) {
                    return null;
                }
                return new User(null, request.getName(), request.getEmail());
            }
        };

        return new UserHandlerImpl(userServicePort, mapper);
    }
}
