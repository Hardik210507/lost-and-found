package com.campus.service;

import com.campus.model.Item;
import com.campus.repository.ClaimRepository;
import com.campus.repository.ItemRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Runs daily and marks PENDING items as UNRESOLVED when:
 *  - The item was created more than 7 days ago, AND
 *  - No claim has been submitted against it in the last 7 days.
 */
@Service
public class ItemStatusScheduler {

    /** Number of days without a claim before an item is marked UNRESOLVED. */
    private static final int EXPIRY_DAYS = 7;

    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;

    public ItemStatusScheduler(ItemRepository itemRepository, ClaimRepository claimRepository) {
        this.itemRepository = itemRepository;
        this.claimRepository = claimRepository;
    }

    /**
     * Runs every day at midnight (server time).
     * Cron expression: "0 0 0 * * *"  →  second=0, minute=0, hour=0, every day.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireUnclaimed() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(EXPIRY_DAYS);

        // Find all items that are still PENDING and were posted more than 7 days ago
        List<Item> staleItems = itemRepository.findByStatusAndCreatedAtBefore("PENDING", cutoff);

        int expired = 0;
        for (Item item : staleItems) {
            // Skip if a claim was filed within the last 7 days (give claimants a grace period)
            boolean hasRecentClaim = claimRepository.existsByItemIdAndCreatedAtAfter(item.getId(), cutoff);
            if (!hasRecentClaim) {
                item.setStatus("UNRESOLVED");
                itemRepository.save(item);
                expired++;
            }
        }

        if (expired > 0) {
            System.out.println("[ItemStatusScheduler] Marked " + expired + " item(s) as UNRESOLVED (no claim in " + EXPIRY_DAYS + " days).");
        }
    }
}
