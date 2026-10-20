package com.campus.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Claim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private String claimant;

    @Column(columnDefinition = "TEXT")
    private String identifyingDetail;
    private String status; // "PENDING", "APPROVED", "REJECTED"
}
