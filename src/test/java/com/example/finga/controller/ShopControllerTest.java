package com.example.finga.controller;

import com.example.finga.entity.Shop;
import com.example.finga.repository.ShopRepository;
import com.example.finga.security.JwtService;
import com.example.finga.user.User;
import com.example.finga.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {

        // Alte Testbenutzer entfernen, damit die Tests wiederholbar sind.
        userRepository.findByUsername("shoptestuser")
                .ifPresent(userRepository::delete);

        userRepository.findByUsername("shoptestadmin")
                .ifPresent(userRepository::delete);

        // Normalen Benutzer erstellen.
        User user = new User();
        user.setUsername("shoptestuser");
        user.setEmail("shoptestuser@example.com");
        user.setPassword(passwordEncoder.encode("test123456"));
        user.setRole("USER");
        userRepository.save(user);

        // Administrator erstellen.
        User admin = new User();
        admin.setUsername("shoptestadmin");
        admin.setEmail("shoptestadmin@example.com");
        admin.setPassword(passwordEncoder.encode("test123456"));
        admin.setRole("ADMIN");
        userRepository.save(admin);

        // JWT-Tokens für die HTTP-Anfragen erzeugen.
        userToken = jwtService.generateToken("shoptestuser", "USER");
        adminToken = jwtService.generateToken("shoptestadmin", "ADMIN");
    }

    private Shop createShop() {
        Shop shop = new Shop();
        shop.setName("Test Shop");
        shop.setDescription("Shop for controller tests");
        shop.setAddress("Test Street 1");

        return shopRepository.save(shop);
    }

    @Test
    void unauthenticatedUserShouldBeAllowedToGetShops() throws Exception {

        mockMvc.perform(get("/api/shops"))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedUserShouldGetAllShops() throws Exception {

        mockMvc.perform(
                get("/api/shops")
                        .header("Authorization", "Bearer " + userToken)
        ).andExpect(status().isOk());
    }

    @Test
    void authenticatedUserShouldGetShopById() throws Exception {

        Shop shop = createShop();

        mockMvc.perform(
                get("/api/shops/" + shop.getId())
                        .header("Authorization", "Bearer " + userToken)
        ).andExpect(status().isOk());
    }

    @Test
    void userShouldNotCreateShop() throws Exception {

        String requestBody = """
                {
                    "name": "User Shop",
                    "description": "Created by normal user",
                    "address": "User Street 1"
                }
                """;

        mockMvc.perform(
                post("/api/shops")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isForbidden());
    }

    @Test
    void adminShouldCreateShop() throws Exception {

        String requestBody = """
                {
                    "name": "Admin Shop",
                    "description": "Created by administrator",
                    "address": "Admin Street 1"
                }
                """;

        mockMvc.perform(
                post("/api/shops")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isCreated());
    }

    @Test
    void adminShouldUpdateShop() throws Exception {

        Shop shop = createShop();

        String requestBody = """
                {
                    "name": "Updated Shop",
                    "description": "Updated by administrator",
                    "address": "Updated Street 2"
                }
                """;

        mockMvc.perform(
                put("/api/shops/" + shop.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isOk());
    }

    @Test
    void userShouldNotUpdateShop() throws Exception {

        Shop shop = createShop();

        String requestBody = """
                {
                    "name": "Forbidden Update",
                    "description": "Should not be updated",
                    "address": "Forbidden Street 3"
                }
                """;

        mockMvc.perform(
                put("/api/shops/" + shop.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isForbidden());
    }

    @Test
    void adminShouldDeleteShop() throws Exception {

        Shop shop = createShop();

        mockMvc.perform(
                delete("/api/shops/" + shop.getId())
                        .header("Authorization", "Bearer " + adminToken)
        ).andExpect(status().isNoContent());
    }

    @Test
    void userShouldNotDeleteShop() throws Exception {

        Shop shop = createShop();

        mockMvc.perform(
                delete("/api/shops/" + shop.getId())
                        .header("Authorization", "Bearer " + userToken)
        ).andExpect(status().isForbidden());
    }
}