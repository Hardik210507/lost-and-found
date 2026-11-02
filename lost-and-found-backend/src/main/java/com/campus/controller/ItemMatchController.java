package com.campus.controller;

import com.campus.model.ItemMatch;
import com.campus.repository.ItemMatchRepository;
import com.campus.service.MatchingService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(originPatterns = "*")
public class ItemMatchController {

    private final ItemMatchRepository matchRepository;
    private final MatchingService matchingService;

    public ItemMatchController(ItemMatchRepository matchRepository, MatchingService matchingService) {
        this.matchRepository = matchRepository;
        this.matchingService = matchingService;
    }

    @GetMapping
    public List<ItemMatch> getAllMatches() {
        return matchRepository.findAll();
    }

    @GetMapping("/item/{itemId}")
    public List<ItemMatch> getMatchesForItem(@PathVariable Long itemId) {
        return matchRepository.findByItemIdOrderByScoreDesc(itemId).stream()
                .filter(m -> m.getMatchedItem() != null && !"RESOLVED".equalsIgnoreCase(m.getMatchedItem().getStatus()))
                .toList();
    }

    // NOTE: This endpoint is unauthenticated and should be removed after use.
    @PostMapping("/recompute")
    public Map<String, Integer> recomputeMatches() {
        int matchPairsStored = matchingService.recomputeAll();
        return Map.of("matchPairsStored", matchPairsStored);
    }
}
