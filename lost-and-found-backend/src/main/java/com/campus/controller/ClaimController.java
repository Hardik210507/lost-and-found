package com.campus.controller;

import com.campus.model.Claim;
import com.campus.repository.ClaimRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "http://localhost:5173")
public class ClaimController {

    private final ClaimRepository claimRepository;

    public ClaimController(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
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
            claim.setStatus(body.get("status"));
        }
        return claimRepository.save(claim);
    }
}
