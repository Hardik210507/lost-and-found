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

    /** Triggered automatically every midnight. */
    @Scheduled(cron = "0 0 0 * * *")
    public void expireUnclaimed() {
        runExpiry(EXPIRY_DAYS);
    }

    /**
     * Public method so the admin endpoint can call it on demand.
     *
     * @param days items older than this many days with no recent claim get marked UNRESOLVED
     * @return number of items that were expired
     */
    @Transactional
    public int runExpiry(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);

        List<Item> staleItems = itemRepository.findByStatusAndCreatedAtBefore("PENDING", cutoff);

        int expired = 0;
        for (Item item : staleItems) {
            boolean hasRecentClaim = claimRepository.existsByItemIdAndCreatedAtAfter(item.getId(), cutoff);
            if (!hasRecentClaim) {
                item.setStatus("UNRESOLVED");
                itemRepository.save(item);
                expired++;
            }
        }

        System.out.println("[ItemStatusScheduler] runExpiry(" + days + "d): marked " + expired + " item(s) UNRESOLVED.");
        return expired;
    }
}
