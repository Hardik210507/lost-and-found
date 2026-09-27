package com.campus.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ItemMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "matched_item_id")
    private Item matchedItem;

    private Integer score;
}
