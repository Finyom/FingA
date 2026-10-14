package com.example.finga.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
        SELECT COUNT(s) > 0
        FROM User u
        JOIN u.shops s
        WHERE u.id = :userId
          AND s.id = :shopId
    """)
    boolean isAssignedToShop(
            @Param("userId") Long userId,
            @Param("shopId") Long shopId
    );
}