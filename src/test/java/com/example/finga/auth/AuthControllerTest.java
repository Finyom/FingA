package com.example.finga.auth;

import com.example.finga.security.JwtService;
import com.example.finga.user.User;
import com.example.finga.user.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        // Remove the test user if it already exists
        userRepository.findByUsername("logintest")
                .ifPresent(userRepository::delete);

        userRepository.findByEmail("logintest@example.com")
                .ifPresent(userRepository::delete);

        // Create a test user
        User user = new User();

        user.setUsername("logintest");
        user.setEmail("logintest@example.com");

        // Store only the BCrypt hash
        user.setPassword(
                passwordEncoder.encode("test123456")
        );

        user.setRole("USER");

        userRepository.save(user);
    }

    @Test
    void loginWithUsernameShouldReturnJwtToken() throws Exception {

        String requestBody = """
                {
                    "login": "logintest",
                    "password": "test123456"
                }
                """;

        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract JWT token from the response
        String token = JsonPath.read(response, "$.token");

        // Verify that the JWT is valid
        assertTrue(jwtService.isTokenValid(token));

        // Verify that the JWT contains the correct username
        assertEquals(
                "logintest",
                jwtService.extractUsername(token)
        );
    }

    @Test
    void loginWithEmailShouldReturnJwtToken() throws Exception {

        String requestBody = """
                {
                    "login": "logintest@example.com",
                    "password": "test123456"
                }
                """;

        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract JWT token from the response
        String token = JsonPath.read(response, "$.token");

        // Verify that the JWT is valid
        assertTrue(jwtService.isTokenValid(token));

        // The JWT should contain the username,
        // even when the user logged in with the email
        assertEquals(
                "logintest",
                jwtService.extractUsername(token)
        );
    }

    @Test
    void registerShouldCreateNewUser() throws Exception {

        // Remove the test user if it already exists
        userRepository.findByUsername("newuser")
                .ifPresent(userRepository::delete);

        userRepository.findByEmail("newuser@example.com")
                .ifPresent(userRepository::delete);

        String requestBody = """
                {
                    "username": "newuser",
                    "email": "newuser@example.com",
                    "password": "test123456"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        // Verify that the user was created
        User user = userRepository.findByUsername("newuser")
                .orElseThrow();

        assertEquals(
                "newuser@example.com",
                user.getEmail()
        );

        assertEquals(
                "USER",
                user.getRole()
        );

        // The password must not be stored as plain text
        assertTrue(
                passwordEncoder.matches(
                        "test123456",
                        user.getPassword()
                )
        );
    }

    @Test
    void registerWithExistingUsernameShouldReturnConflict() throws Exception {

        String requestBody = """
                {
                    "username": "logintest",
                    "email": "another@example.com",
                    "password": "test123456"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Username is already registered"));
    }

    @Test
    void registerWithExistingEmailShouldReturnConflict() throws Exception {

        String requestBody = """
                {
                    "username": "anotheruser",
                    "email": "logintest@example.com",
                    "password": "test123456"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Email is already registered"));
    }

    @Test
    void loginWithWrongPasswordShouldReturnUnauthorized() throws Exception {

        String requestBody = """
                {
                    "login": "logintest",
                    "password": "wrongpassword"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithUnknownUserShouldReturnUnauthorized() throws Exception {

        String requestBody = """
                {
                    "login": "unknown@example.com",
                    "password": "test123456"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerWithoutEmailShouldReturnBadRequest() throws Exception {

        String requestBody = """
                {
                    "username": "newuser2",
                    "password": "test123456"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerWithInvalidEmailShouldReturnBadRequest() throws Exception {

        String requestBody = """
                {
                    "username": "newuser3",
                    "email": "not-an-email",
                    "password": "test123456"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }
}

