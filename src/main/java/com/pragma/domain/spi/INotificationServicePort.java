package com.pragma.domain.spi;

public interface INotificationServicePort {
    void sendNotification(String subject, String message);
}
