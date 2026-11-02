package com.campus.repository;

import com.campus.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByItemId(Long itemId);

    @Transactional
    void deleteByItemId(Long itemId);
}
