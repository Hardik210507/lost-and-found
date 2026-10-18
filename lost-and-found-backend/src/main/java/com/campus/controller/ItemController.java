package com.campus.controller;

import com.campus.model.Item;
import com.campus.repository.ItemRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:5173") // Vite default port
public class ItemController {

    private final ItemRepository itemRepository;
    private final com.campus.service.MatchingService matchingService;

    public ItemController(ItemRepository itemRepository, com.campus.service.MatchingService matchingService) {
        this.itemRepository = itemRepository;
        this.matchingService = matchingService;
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
    public org.springframework.http.ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (!itemRepository.existsById(id)) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        itemRepository.deleteById(id);
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
