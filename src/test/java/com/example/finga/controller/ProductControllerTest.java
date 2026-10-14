package com.example.finga.controller;

import com.example.finga.entity.Product;
import com.example.finga.entity.Shop;
import com.example.finga.repository.ProductRepository;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String userToken;
    private String userAdminToken;
    private String adminToken;

    @BeforeEach
    void setUp() {

        userRepository.findByUsername("productuser")
                .ifPresent(userRepository::delete);

        userRepository.findByUsername("productadmin")
                .ifPresent(userRepository::delete);

        userRepository.findByUsername("productuseradmin")
                .ifPresent(userRepository::delete);

        User user = new User();
        user.setUsername("productuser");
        user.setEmail("productuser@example.com");
        user.setPassword(passwordEncoder.encode("test123456"));
        user.setRole("USER");

        userRepository.save(user);

        User userAdmin = new User();
        userAdmin.setUsername("productuseradmin");
        userAdmin.setEmail("productuseradmin@example.com");
        userAdmin.setPassword(passwordEncoder.encode("test123456"));
        userAdmin.setRole("USER_ADMIN");

        userRepository.save(userAdmin);

        User admin = new User();
        admin.setUsername("productadmin");
        admin.setEmail("productadmin@example.com");
        admin.setPassword(passwordEncoder.encode("test123456"));
        admin.setRole("ADMIN");

        userRepository.save(admin);

        userToken = jwtService.generateToken("productuser", "USER");
        userAdminToken = jwtService.generateToken("productuseradmin", "USER_ADMIN");
        adminToken = jwtService.generateToken("productadmin", "ADMIN");
    }

    private Shop createShop() {

        Shop shop = new Shop();
        shop.setName("Test Shop");
        shop.setDescription("Shop for product tests");
        shop.setAddress("Test Street 1");

        return shopRepository.save(shop);
    }

    private Product createProduct(Shop shop) {

        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Product for tests");
        product.setPrice(new BigDecimal("99.99"));
        product.setStock(10);
        product.setShop(shop);

        return productRepository.save(product);
    }


    @Test
    void unauthenticatedUserShouldBeAllowedToGetProducts() throws Exception {

        // Create a shop for the test
        Shop shop = createShop();

        // Public GET requests do not require authentication
        mockMvc.perform(
                get("/api/shops/" + shop.getId() + "/products")
        ).andExpect(status().isOk());
    }




    @Test
    void unauthenticatedUserShouldNotCreateProduct() throws Exception {

        Shop shop = createShop();

        String requestBody = """
            {
                "name": "Unauthorized Product",
                "description": "Product without authentication",
                "price": 49.99,
                "stock": 5
            }
            """;

        mockMvc.perform(
                post("/api/shops/" + shop.getId() + "/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isUnauthorized());
    }


    @Test
    void userShouldBeAllowedToGetProducts() throws Exception {

        Shop shop = createShop();

        mockMvc.perform(
                get("/api/shops/" + shop.getId() + "/products")
                        .header("Authorization", "Bearer " + userToken)
        ).andExpect(status().isOk());
    }

    @Test
    void adminShouldBeAllowedToGetProducts() throws Exception {

        Shop shop = createShop();

        mockMvc.perform(
                get("/api/shops/" + shop.getId() + "/products")
                        .header("Authorization", "Bearer " + adminToken)
        ).andExpect(status().isOk());
    }


    @Test
    void userShouldBeAllowedToGetProductById() throws Exception {

        Shop shop = createShop();
        Product product = createProduct(shop);

        mockMvc.perform(
                get("/api/shops/" + shop.getId()
                        + "/products/" + product.getId())
                        .header("Authorization", "Bearer " + userToken)
        ).andExpect(status().isOk());
    }


    @Test
    void userShouldNotBeAllowedToCreateProduct() throws Exception {

        Shop shop = createShop();

        String requestBody = """
                {
                    "name": "User Product",
                    "description": "Created by normal user",
                    "price": 49.99,
                    "stock": 5
                }
                """;

        mockMvc.perform(
                post("/api/shops/" + shop.getId() + "/products")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isForbidden());
    }

    @Test
    void userShouldNotBeAllowedToUpdateProduct() throws Exception {

        Shop shop = createShop();
        Product product = createProduct(shop);

        String requestBody = """
                {
                    "name": "Updated Product",
                    "description": "Updated by normal user",
                    "price": 59.99,
                    "stock": 20
                }
                """;

        mockMvc.perform(
                put("/api/shops/" + shop.getId()
                        + "/products/" + product.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isForbidden());
    }

    @Test
    void userShouldNotBeAllowedToDeleteProduct() throws Exception {

        Shop shop = createShop();
        Product product = createProduct(shop);

        mockMvc.perform(
                delete("/api/shops/" + shop.getId()
                        + "/products/" + product.getId())
                        .header("Authorization", "Bearer " + userToken)
        ).andExpect(status().isForbidden());
    }

    @Test
    void adminShouldBeAllowedToCreateProduct() throws Exception {

        Shop shop = createShop();

        String requestBody = """
                {
                    "name": "Admin Product",
                    "description": "Created by administrator",
                    "price": 199.99,
                    "stock": 15
                }
                """;

        mockMvc.perform(
                post("/api/shops/" + shop.getId() + "/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isCreated());
    }

    @Test
    void adminShouldBeAllowedToUpdateProduct() throws Exception {

        Shop shop = createShop();
        Product product = createProduct(shop);

        String requestBody = """
                {
                    "name": "Updated Admin Product",
                    "description": "Updated by administrator",
                    "price": 299.99,
                    "stock": 25
                }
                """;

        mockMvc.perform(
                put("/api/shops/" + shop.getId()
                        + "/products/" + product.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isOk());
    }

    @Test
    void adminShouldBeAllowedToDeleteProduct() throws Exception {

        Shop shop = createShop();
        Product product = createProduct(shop);

        mockMvc.perform(
                delete("/api/shops/" + shop.getId()
                        + "/products/" + product.getId())
                        .header("Authorization", "Bearer " + adminToken)
        ).andExpect(status().isNoContent());
    }

    @Test
    void userAdminShouldNotBeAllowedToCreateProductInUnassignedShop()
            throws Exception {

        Shop shop = createShop();

        String requestBody = """
            {
                "name": "User Admin Product",
                "description": "Created by user admin",
                "price": 79.99,
                "stock": 10
            }
            """;

        mockMvc.perform(
                post("/api/shops/" + shop.getId() + "/products")
                        .header(
                                "Authorization",
                                "Bearer " + userAdminToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isForbidden());
    }

}

