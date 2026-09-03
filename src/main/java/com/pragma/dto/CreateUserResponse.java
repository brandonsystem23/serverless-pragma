package com.pragma.dto;

import com.pragma.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateUserResponse {
    private String message;
    private User user;
}
