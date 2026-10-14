package com.example.finga.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    private String login;

    @NotBlank(message = "Password is required")
    private String password;
}