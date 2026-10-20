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
        return claimRepository.save(claim);
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
