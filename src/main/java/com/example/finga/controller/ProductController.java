package com.example.finga.controller;

import com.example.finga.dto.*;
import com.example.finga.service.ProductService;
import com.example.finga.service.ProductPhotoService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * REST Controller für Produkte.
 *
 * URL-Struktur:
 *
 * /api/shops/{shopId}/products
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/shops/{shopId}/products")
public class ProductController {

 private final ProductService productService;

 private final ProductPhotoService productPhotoService;

 /**
  * Konstruktor.
  */
 public ProductController(
         ProductService service,
         ProductPhotoService productPhotoService) {

  this.productService = service;
  this.productPhotoService = productPhotoService;
 }

 /**
  * Gibt alle Produkte eines Shops zurück.
  */
 @GetMapping
 public List<ProductResponse> all(
         @PathVariable Long shopId) {

  return productService.findByShop(shopId);
 }

 /**
  * Gibt ein bestimmtes Produkt zurück.
  */
 @GetMapping("/{productId}")
 public ResponseEntity<ProductResponse> getProduct(
         @PathVariable Long shopId,
         @PathVariable Long productId) {

  return ResponseEntity.ok(
          productService.findById(shopId, productId)
  );
 }

 /**
  * Erstellt ein neues Produkt.
  */
 @PostMapping
 public ResponseEntity<ProductResponse> create(
         @PathVariable Long shopId,
         @Valid @RequestBody ProductRequest r,
         Authentication authentication) {

  return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(
                  productService.create(
                          shopId,
                          r,
                          authentication
                  )
          );
 }

 /**
  * Aktualisiert ein bestehendes Produkt.
  */
 @PutMapping("/{id}")
 public ProductResponse update(
         @PathVariable Long shopId,
         @PathVariable Long id,
         @Valid @RequestBody ProductRequest r,
         Authentication authentication) {

  return productService.update(
          shopId,
          id,
          r,
          authentication
  );
 }

 /**
  * Löscht ein Produkt.
  */
 @DeleteMapping("/{id}")
 @ResponseStatus(HttpStatus.NO_CONTENT)
 public void delete(
         @PathVariable Long shopId,
         @PathVariable Long id,
         Authentication authentication) {

  productService.delete(
          shopId,
          id,
          authentication
  );
 }

 /**
  * Lädt ein Foto für ein Produkt hoch.
  *
  * Nur ADMIN und USER_ADMIN mit
  * entsprechender Shop-Zuweisung dürfen
  * das Foto ändern.
  *
  * Endpoint:
  *
  * POST /api/shops/{shopId}/products/{productId}/photo
  */
 @PostMapping(
         value = "/{productId}/photo",
         consumes = MediaType.MULTIPART_FORM_DATA_VALUE
 )
 public ResponseEntity<Void> uploadPhoto(
         @PathVariable Long shopId,
         @PathVariable Long productId,
         @RequestParam("file") MultipartFile file,
         Authentication authentication)
         throws IOException {

  productPhotoService.uploadPhoto(
          shopId,
          productId,
          file,
          authentication
  );

  return ResponseEntity.ok().build();
 }
}

