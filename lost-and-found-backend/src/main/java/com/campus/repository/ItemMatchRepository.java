package com.campus.repository;

import com.campus.model.ItemMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ItemMatchRepository extends JpaRepository<ItemMatch, Long> {
    List<ItemMatch> findByItemId(Long itemId);

    List<ItemMatch> findByItemIdOrderByScoreDesc(Long itemId);

    @Transactional
    void deleteByItemIdOrMatchedItemId(Long itemId, Long matchedItemId);
}
