package com.campus.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String type; // "LOST" or "FOUND"
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String category;
    private String location;
    private String status; // "PENDING", "MATCHED", "RESOLVED"
    
    @Column(columnDefinition = "TEXT")
    private String imagePath;
    
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private String postedBy;
}
