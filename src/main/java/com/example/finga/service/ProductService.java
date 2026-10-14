package com.example.finga.service;

import com.example.finga.dto.*;
import com.example.finga.entity.*;
import com.example.finga.exception.ResourceNotFoundException;
import com.example.finga.repository.*;
import com.example.finga.user.User;
import com.example.finga.user.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

 private final ProductRepository products;
 private final ProductPhotoRepository productPhotos;
 private final ShopService shops;
 private final UserRepository users;

 public ProductService(
         ProductRepository products,
         ProductPhotoRepository productPhotos,
         ShopService shops,
         UserRepository users) {

  this.products = products;
  this.productPhotos = productPhotos;
  this.shops = shops;
  this.users = users;
 }

 // =========================
 // READ
 // =========================

 @Transactional(readOnly = true)
 public List<ProductResponse> findByShop(Long shopId) {

  shops.get(shopId);

  List<Product> productList =
          products.findByShopId(shopId);

  return toResponses(productList);
 }


 @Transactional(readOnly = true)
 public ProductResponse getProduct(
         Long shopId,
         Long productId) {

  Product product = products
          .findByIdAndShopId(productId, shopId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Product %d not found in shop %d"
                                  .formatted(productId, shopId)
                  )
          );

  return toResponse(product);
 }

 @Transactional(readOnly = true)
 public ProductResponse findById(
         Long shopId,
         Long id) {

  shops.get(shopId);

  Product product = products
          .findByIdAndShopId(id, shopId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Product %d not found in shop %d"
                                  .formatted(id, shopId)
                  )
          );

  return toResponse(product);
 }

 // =========================
 // CREATE
 // =========================

 public ProductResponse create(
         Long shopId,
         ProductRequest r,
         Authentication authentication) {

  User user = getCurrentUser(authentication);
  Shop shop = shops.get(shopId);

  checkProductManagementPermission(user, shop);

  if (products.existsByShopIdAndNameIgnoreCase(
          shopId, r.name())) {

   throw new IllegalArgumentException(
           "A product with this name already exists in this shop"
   );
  }

  Product product = new Product();
  product.setShop(shop);

  apply(product, r);

  return toResponse(products.save(product));
 }

 // =========================
 // UPDATE
 // =========================

 public ProductResponse update(
         Long shopId,
         Long id,
         ProductRequest r,
         Authentication authentication) {

  User user = getCurrentUser(authentication);
  Shop shop = shops.get(shopId);

  checkProductManagementPermission(user, shop);

  Product product = products
          .findByIdAndShopId(id, shopId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Product %d not found in shop %d"
                                  .formatted(id, shopId)
                  )
          );

  apply(product, r);

  return toResponse(products.save(product));
 }

 // =========================
 // DELETE
 // =========================

 public void delete(
         Long shopId,
         Long id,
         Authentication authentication) {

  User user = getCurrentUser(authentication);
  Shop shop = shops.get(shopId);

  checkProductManagementPermission(user, shop);

  Product product = products
          .findByIdAndShopId(id, shopId)
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "Product %d not found in shop %d"
                                  .formatted(id, shopId)
                  )
          );

  products.delete(product);
 }

 // =========================
 // USER
 // =========================

 private User getCurrentUser(
         Authentication authentication) {

  if (authentication == null) {
   throw new AccessDeniedException(
           "Authentication required"
   );
  }

  return users.findByUsername(authentication.getName())
          .orElseThrow(() ->
                  new ResourceNotFoundException(
                          "User not found"
                  )
          );
 }

 // =========================
 // PERMISSION
 // =========================

 private void checkProductManagementPermission(
         User user,
         Shop shop) {

  // ADMIN darf Produkte in allen Shops verwalten.
  if ("ADMIN".equals(user.getRole())) {
   return;
  }

  // USER_ADMIN darf nur zugewiesene Shops verwalten.
  if ("USER_ADMIN".equals(user.getRole())) {

   boolean assigned = users.isAssignedToShop(
           user.getId(),
           shop.getId()
   );

   if (assigned) {
    return;
   }
  }

  throw new AccessDeniedException(
          "You are not allowed to manage products in this shop"
  );
 }

 // =========================
 // MAPPING
 // =========================

 private void apply(
         Product product,
         ProductRequest request) {

  product.setName(request.name());
  product.setDescription(request.description());
  product.setPrice(request.price());
  product.setStock(request.stock());
 }

 /**
  * Wandelt mehrere Produkte in Responses um.
  * Alle Fotopfade werden gemeinsam geladen.
  */
 private List<ProductResponse> toResponses(
         List<Product> productList) {

  if (productList.isEmpty()) {
   return List.of();
  }

  List<Long> productIds = productList.stream()
          .map(Product::getId)
          .toList();

  Map<Long, List<String>> photosByProductId =
          productPhotos
                  .findPhotoPathsByProductIds(productIds)
                  .stream()
                  .collect(Collectors.groupingBy(
                          ProductPhotoPath::getProductId,
                          LinkedHashMap::new,
                          Collectors.mapping(
                                  ProductPhotoPath::getPhotoPath,
                                  Collectors.toList()
                          )
                  ));

  return productList.stream()
          .map(product -> toResponse(
                  product,
                  photosByProductId.getOrDefault(
                          product.getId(),
                          List.of()
                  )
          ))
          .toList();
 }

 /**
  * Mapping eines einzelnen Produkts.
  */
 private ProductResponse toResponse(Product product) {

  List<String> photoPaths =
          productPhotos.findByProductId(product.getId())
                  .stream()
                  .map(ProductPhoto::getPhotoPath)
                  .toList();

  return toResponse(product, photoPaths);
 }

 /**
  * Erstellt die Response ohne weitere Datenbankabfrage.
  */
 private ProductResponse toResponse(
         Product product,
         List<String> photoPaths) {

  // Alte Fotos berücksichtigen, die noch in photo_path stehen.
  if (photoPaths.isEmpty()
          && product.getPhotoPath() != null
          && !product.getPhotoPath().isBlank()) {

   photoPaths = List.of(product.getPhotoPath());
  }

  String primaryPhotoPath = photoPaths.isEmpty()
          ? product.getPhotoPath()
          : photoPaths.get(0);

  return new ProductResponse(
          product.getId(),
          product.getName(),
          product.getDescription(),
          product.getPrice(),
          product.getStock(),
          product.getShop().getId(),
          primaryPhotoPath,
          photoPaths,
          product.getCreatedAt(),
          product.getUpdatedAt()
  );
 }
}
