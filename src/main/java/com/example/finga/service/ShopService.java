package com.example.finga.service;
import com.example.finga.dto.*; import com.example.finga.entity.Shop; import com.example.finga.exception.ResourceNotFoundException; import com.example.finga.repository.ShopRepository; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
@Service @Transactional
public class ShopService {
 private final ShopRepository repo;
 public ShopService(ShopRepository repo){
  this.repo=repo;
 }

 public List<ShopResponse> findAll(){
  return repo.findAll().stream().map(this::toResponse).toList();
 }

 @Transactional(readOnly=true)
 public ShopResponse findById(Long id){
  return toResponse(get(id));
 }

 public ShopResponse create(ShopRequest r){
  Shop s=new Shop();
  apply(s,r);
  return toResponse(repo.save(s));
 }

 public ShopResponse update(Long id,ShopRequest r){
  Shop s=get(id);
  apply(s,r);
  return toResponse(repo.save(s));
 }

 public void delete(Long id){
  repo.delete(get(id));
 }

 public Shop get(Long id){
  return repo.findById(id)
          .orElseThrow(()->new ResourceNotFoundException("Shop %d not found".formatted(id)));
 }

 private void apply(Shop s,ShopRequest r){
  s.setName(r.name());
  s.setDescription(r.description());
  s.setAddress(r.address());
 }
 
 private ShopResponse toResponse(Shop s){
  return new ShopResponse(s.getId(),
          s.getName(),
          s.getDescription(),
          s.getAddress(),
          s.getPhotoPath(),
          s.getCreatedAt(),
          s.getUpdatedAt());
 }
}
