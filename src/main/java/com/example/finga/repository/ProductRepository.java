package com.example.finga.repository; import com.example.finga.entity.Product; import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ProductRepository extends JpaRepository<Product,Long>{
    List<Product> findByShopId(Long shopId);
    Optional<Product> findByIdAndShopId(Long id,Long shopId);
    boolean existsByShopIdAndNameIgnoreCase(Long shopId, String name);
}
