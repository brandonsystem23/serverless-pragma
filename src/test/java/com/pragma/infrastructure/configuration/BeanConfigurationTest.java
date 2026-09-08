package com.pragma.infrastructure.configuration;

import com.pragma.application.handler.IUserHandler;
import com.pragma.application.handler.impl.UserHandlerImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanConfigurationTest {

    @Test
    void userHandlerShouldReturnConfiguredInstance() {
        IUserHandler handler = BeanConfiguration.userHandler();

        assertNotNull(handler);
        assertInstanceOf(UserHandlerImpl.class, handler);
    }
}
