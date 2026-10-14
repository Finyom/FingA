package com.example.finga.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String password = "user123";

        String hash = encoder.encode(password);

        System.out.println(hash);
    }
}