package com.example.finga.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;
@Entity @Table(name="shops") @Getter @Setter @NoArgsConstructor
public class Shop {

 @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
 private Long id;

 @Column(nullable=false,length=120)
 private String name;

 @Column(length=500)
 private String description;

 @Column(nullable=false,length=255)
 private String address;

 @Column(name = "photo_path", length = 500)
 private String photoPath;

 @Column(name="created_at",nullable=false,updatable=false)
 private Instant createdAt;

 @Column(name="updated_at",nullable=false)
 private Instant updatedAt;

 @OneToMany(mappedBy="shop",cascade=CascadeType.ALL,orphanRemoval=true)
 private List<Product> products=new ArrayList<>();

 @PrePersist
 void prePersist(){createdAt=Instant.now();updatedAt=createdAt;}

 @PreUpdate
 void preUpdate(){updatedAt=Instant.now();}
}
