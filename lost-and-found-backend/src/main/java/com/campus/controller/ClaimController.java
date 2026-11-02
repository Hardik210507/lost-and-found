package com.campus.controller;

import com.campus.model.Claim;
import com.campus.model.Item;
import com.campus.model.ItemMatch;
import com.campus.repository.ClaimRepository;
import com.campus.repository.ItemMatchRepository;
import com.campus.repository.ItemRepository;
import com.campus.service.MatchScorer;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(originPatterns = "*")
public class ClaimController {

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final ItemMatchRepository matchRepository;

    public ClaimController(ClaimRepository claimRepository, ItemRepository itemRepository, ItemMatchRepository matchRepository) {
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
        this.matchRepository = matchRepository;
    }

    @GetMapping
    public List<Claim> getAllClaims() {
        List<Claim> claims = claimRepository.findAll();
        for (Claim c : claims) {
            c.setMatchScore(calculateDynamicClaimScore(c));
        }
        return claims;
    }

    @GetMapping("/item/{itemId}")
    public List<Claim> getClaimsForItem(@PathVariable Long itemId) {
        List<Claim> claims = claimRepository.findByItemId(itemId);
        for (Claim c : claims) {
            c.setMatchScore(calculateDynamicClaimScore(c));
        }
        return claims;
    }

    @PostMapping
    public Claim createClaim(@RequestBody Claim claim) {
        if (claim.getStatus() == null) claim.setStatus("PENDING");
        claim.setMatchScore(calculateDynamicClaimScore(claim));
        return claimRepository.save(claim);
    }

    @PatchMapping("/{id}/status")
    public Claim updateClaimStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Claim claim = claimRepository.findById(id).orElseThrow(() -> new RuntimeException("Claim not found"));
        if (body.containsKey("status")) {
            String newStatus = body.get("status");
            claim.setStatus(newStatus);
            if ("APPROVED".equalsIgnoreCase(newStatus) && claim.getItem() != null) {
                Item item = claim.getItem();
                item.setStatus("RESOLVED");
                itemRepository.save(item);
            }
        }
        claim.setMatchScore(calculateDynamicClaimScore(claim));
        return claimRepository.save(claim);
    }

    /**
     * Dynamically calculates matching score for a claim:
     * 1. If claim is APPROVED by admin -> 100/100 verified score.
     * 2. If the item has opposite matches in item_match -> uses highest item match score.
     * 3. Keyword similarity between identifyingDetail and item details (title, description, category, location).
     */
    public int calculateDynamicClaimScore(Claim claim) {
        if (claim == null) {
            return 0;
        }

        // 1. If already approved by admin, verification is 100% complete
        if ("APPROVED".equalsIgnoreCase(claim.getStatus())) {
            return 100;
        }

        if (claim.getItem() == null || claim.getItem().getId() == null) {
            return 0;
        }

        Long itemId = claim.getItem().getId();
        Item item = itemRepository.findById(itemId).orElse(claim.getItem());

        // 2. Item-level counterpart match score (from ItemMatch table)
        int highestMatchScore = 0;
        if (matchRepository != null) {
            List<ItemMatch> matches = matchRepository.findByItemId(itemId);
            if (matches != null && !matches.isEmpty()) {
                highestMatchScore = matches.stream()
                        .mapToInt(m -> m.getScore() != null ? m.getScore() : 0)
                        .max()
                        .orElse(0);
            }
        }

        // 3. Keyword similarity between identifyingDetail and item manifest
        int textSimilarityScore = 0;
        if (item != null && claim.getIdentifyingDetail() != null && !claim.getIdentifyingDetail().isBlank()) {
            Set<String> claimWords = MatchScorer.keywords(claim.getIdentifyingDetail());
            String itemText = (item.getTitle() != null ? item.getTitle() : "") + " " +
                    (item.getDescription() != null ? item.getDescription() : "") + " " +
                    (item.getCategory() != null ? item.getCategory() : "") + " " +
                    (item.getLocation() != null ? item.getLocation() : "");
            Set<String> itemWords = MatchScorer.keywords(itemText);

            if (!claimWords.isEmpty() && !itemWords.isEmpty()) {
                Set<String> shared = new HashSet<>(claimWords);
                shared.retainAll(itemWords);
                if (!shared.isEmpty()) {
                    double ratio = (double) shared.size() / claimWords.size();
                    textSimilarityScore = (int) Math.min(100, Math.round(ratio * 100));
                }
            }
        }

        return Math.max(highestMatchScore, textSimilarityScore);
    }
}
