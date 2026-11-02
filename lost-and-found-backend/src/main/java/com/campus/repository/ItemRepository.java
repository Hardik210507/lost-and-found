package com.campus.repository;

import com.campus.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByStatusAndCreatedAtBefore(String status, LocalDateTime cutoff);
}
