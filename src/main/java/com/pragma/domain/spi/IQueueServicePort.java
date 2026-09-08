package com.pragma.domain.spi;

import com.pragma.domain.model.User;

public interface IQueueServicePort {
    void sendUserCreated(User user);
}
