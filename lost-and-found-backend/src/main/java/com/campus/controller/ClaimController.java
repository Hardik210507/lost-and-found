package com.campus.controller;

import com.campus.model.Claim;
import com.campus.repository.ClaimRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(originPatterns = "*")
public class ClaimController {

    private final ClaimRepository claimRepository;
    private final com.campus.repository.ItemRepository itemRepository;

    public ClaimController(ClaimRepository claimRepository, com.campus.repository.ItemRepository itemRepository) {
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    @PostMapping
    public Claim createClaim(@RequestBody Claim claim) {
        if (claim.getStatus() == null) claim.setStatus("PENDING");
        int score = calculateClaimScore(claim);
        claim.setMatchScore(score);
        return claimRepository.save(claim);
    }

    private int calculateClaimScore(Claim claim) {
        if (claim.getIdentifyingDetail() == null || claim.getIdentifyingDetail().isBlank()) {
            return 0;
        }
        if (claim.getItem() == null || claim.getItem().getId() == null) {
            return 0;
        }
        com.campus.model.Item item = itemRepository.findById(claim.getItem().getId()).orElse(null);
        if (item == null) {
            return 0;
        }

        java.util.Set<String> claimWords = com.campus.service.MatchScorer.keywords(claim.getIdentifyingDetail());
        String itemText = (item.getTitle() != null ? item.getTitle() : "") + " " + (item.getDescription() != null ? item.getDescription() : "");
        java.util.Set<String> itemWords = com.campus.service.MatchScorer.keywords(itemText);

        if (claimWords.isEmpty() || itemWords.isEmpty()) {
            return 0;
        }

        java.util.Set<String> shared = new java.util.HashSet<>(claimWords);
        shared.retainAll(itemWords);

        if (shared.isEmpty()) {
            return 0;
        }

        double ratio = (double) shared.size() / claimWords.size();
        return (int) Math.min(100, Math.round(ratio * 100));
    }

    @PatchMapping("/{id}/status")
    public Claim updateClaimStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        Claim claim = claimRepository.findById(id).orElseThrow(() -> new RuntimeException("Claim not found"));
        if (body.containsKey("status")) {
            String newStatus = body.get("status");
            claim.setStatus(newStatus);
            if ("APPROVED".equalsIgnoreCase(newStatus) && claim.getItem() != null) {
                com.campus.model.Item item = claim.getItem();
                item.setStatus("RESOLVED");
                itemRepository.save(item);
            }
        }
        return claimRepository.save(claim);
    }
}
