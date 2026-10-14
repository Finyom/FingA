package com.example.finga.controller;
import com.example.finga.dto.*;
import com.example.finga.service.ShopPhotoService;
import com.example.finga.service.ShopService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/shops")
public class ShopController {

 private final ShopService service;

 private final ShopPhotoService shopPhotoService;

 public ShopController(ShopService service, ShopPhotoService shopPhotoService){
  this.service=service;
     this.shopPhotoService = shopPhotoService;
 }

 @GetMapping
 public List<ShopResponse> all(){

  return service.findAll();
 }
 @GetMapping("/{id}")
 public ShopResponse one(@PathVariable Long id){

  return service.findById(id);
 }

 @PreAuthorize("hasRole('ADMIN')")
 @PostMapping
 public ResponseEntity<ShopResponse> create(@Valid @RequestBody ShopRequest r){
  return ResponseEntity.status(HttpStatus.CREATED)
          .body(service.create(r));
 }

 @PreAuthorize("hasRole('ADMIN')")
 @PutMapping("/{id}")
 public ShopResponse update(@PathVariable Long id,@Valid @RequestBody ShopRequest r){

  return service.update(id,r);
 }

 @PreAuthorize("hasRole('ADMIN')")
 @DeleteMapping("/{id}")
 @ResponseStatus(HttpStatus.NO_CONTENT)
 public void delete(@PathVariable Long id){
  service.delete(id);
 }

 @PostMapping(
         value = "/{shopId}/photo",
         consumes = MediaType.MULTIPART_FORM_DATA_VALUE
 )
 public ResponseEntity<Void> uploadPhoto(
         @PathVariable Long shopId,
         @RequestParam("file") MultipartFile file,
         Authentication authentication) throws IOException {

  shopPhotoService.uploadPhoto(
          shopId,
          file,
          authentication
  );

  return ResponseEntity.ok().build();
 }
}
