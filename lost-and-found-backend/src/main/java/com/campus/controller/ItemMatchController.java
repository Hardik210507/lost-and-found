package com.campus.controller;

import com.campus.model.ItemMatch;
import com.campus.repository.ItemMatchRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(originPatterns = "*")
public class ItemMatchController {

    private final ItemMatchRepository matchRepository;

    public ItemMatchController(ItemMatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @GetMapping
    public List<ItemMatch> getAllMatches() {
        return matchRepository.findAll();
    }

    @GetMapping("/item/{itemId}")
    public List<ItemMatch> getMatchesForItem(@PathVariable Long itemId) {
        return matchRepository.findByItemId(itemId);
    }
}
