package com.campus.controller;

import com.campus.model.Item;
import com.campus.repository.ClaimRepository;
import com.campus.repository.ItemRepository;
import com.campus.service.MatchingService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(originPatterns = "*")
public class ItemController {

    private final ItemRepository itemRepository;
    private final MatchingService matchingService;
    private final ClaimRepository claimRepository;

    public ItemController(ItemRepository itemRepository, MatchingService matchingService, ClaimRepository claimRepository) {
        this.itemRepository = itemRepository;
        this.matchingService = matchingService;
        this.claimRepository = claimRepository;
    }

    @GetMapping
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @PostMapping
    public Item createItem(@RequestBody Item item) {
        if (item.getStatus() == null) item.setStatus("PENDING");
        Item saved = itemRepository.save(item);
        try {
            matchingService.calculateMatchesForNewItem(saved);
        } catch (Exception e) {
            // Keep item creation resilient even if matching calculation logs warning
            System.err.println("Matching computation error: " + e.getMessage());
        }
        return itemRepository.findById(saved.getId()).orElse(saved);
    }
    
    @GetMapping("/{id}")
    public Item getItem(@PathVariable Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public org.springframework.http.ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (!itemRepository.existsById(id)) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        matchingService.removeMatchesFor(id);
        claimRepository.deleteByItemId(id);
        itemRepository.deleteById(id);
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
