package com.example.finga.service;

import com.example.finga.dto.UserResponse;
import com.example.finga.entity.Shop;
import com.example.finga.repository.ShopRepository;
import com.example.finga.user.User;
import com.example.finga.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserShopService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    public void assignShop(Long userId, Long shopId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Shop not found"));

        if (!"USER_ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException(
                    "Only USER_ADMIN can be assigned to shops"
            );
        }

        user.getShops().add(shop);

        userRepository.save(user);
    }

    public void removeShop(Long userId, Long shopId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        user.getShops()
                .removeIf(shop -> shop.getId().equals(shopId));

        userRepository.save(user);
    }

    public void changeRoleToUserAdmin(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        user.setRole("USER_ADMIN");

        userRepository.save(user);
    }

    public void changeRoleToUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        user.setRole("USER");

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        user.getShops()
                                .stream()
                                .map(Shop::getId)
                                .toList()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        List<Long> shopIds = user.getShops()
                .stream()
                .map(Shop::getId)
                .toList();

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                shopIds
        );
    }
}