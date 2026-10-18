package com.campus.service;

import com.campus.model.Item;
import com.campus.model.ItemMatch;
import com.campus.repository.ItemMatchRepository;
import com.campus.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchingService {

    private final ItemRepository itemRepository;
    private final ItemMatchRepository matchRepository;

    public MatchingService(ItemRepository itemRepository, ItemMatchRepository matchRepository) {
        this.itemRepository = itemRepository;
        this.matchRepository = matchRepository;
    }

    public void calculateMatchesForNewItem(Item newItem) {
        if (newItem == null || newItem.getType() == null) return;

        // Compare against opposite type (LOST compares with FOUND, FOUND compares with LOST)
        String targetType = "LOST".equalsIgnoreCase(newItem.getType()) ? "FOUND" : "LOST";
        List<Item> candidates = itemRepository.findAll().stream()
                .filter(i -> targetType.equalsIgnoreCase(i.getType()))
                .filter(i -> !"RESOLVED".equalsIgnoreCase(i.getStatus()))
                .filter(i -> !i.getId().equals(newItem.getId()))
                .toList();

        for (Item candidate : candidates) {
            int score = computeMatchScore(newItem, candidate);
            if (score >= 40) {
                // Save match for newItem -> candidate
                ItemMatch match1 = new ItemMatch();
                match1.setItem(newItem);
                match1.setMatchedItem(candidate);
                match1.setScore(score);
                matchRepository.save(match1);

                // Save reciprocal match for candidate -> newItem
                ItemMatch match2 = new ItemMatch();
                match2.setItem(candidate);
                match2.setMatchedItem(newItem);
                match2.setScore(score);
                matchRepository.save(match2);

                // Update status to MATCHED if still PENDING
                if ("PENDING".equalsIgnoreCase(newItem.getStatus())) {
                    newItem.setStatus("MATCHED");
                    itemRepository.save(newItem);
                }
                if ("PENDING".equalsIgnoreCase(candidate.getStatus())) {
                    candidate.setStatus("MATCHED");
                    itemRepository.save(candidate);
                }
            }
        }
    }

    private int computeMatchScore(Item a, Item b) {
        int score = 0;

        // 1. Category Match (40 points)
        if (a.getCategory() != null && a.getCategory().equalsIgnoreCase(b.getCategory())) {
            score += 40;
        }

        // 2. Location Match (30 points)
        if (a.getLocation() != null && a.getLocation().equalsIgnoreCase(b.getLocation())) {
            score += 30;
        }

        // 3. Keyword Similarity in Title & Description (Up to 30 points)
        Set<String> wordsA = extractKeywords(a.getTitle() + " " + (a.getDescription() != null ? a.getDescription() : ""));
        Set<String> wordsB = extractKeywords(b.getTitle() + " " + (b.getDescription() != null ? b.getDescription() : ""));

        if (!wordsA.isEmpty() && !wordsB.isEmpty()) {
            Set<String> intersection = new HashSet<>(wordsA);
            intersection.retainAll(wordsB);

            if (!intersection.isEmpty()) {
                double overlap = (double) intersection.size() / Math.min(wordsA.size(), wordsB.size());
                int keywordScore = (int) Math.min(30, Math.round(overlap * 30));
                score += Math.max(15, keywordScore); // At least 15 points if any keyword matched
            }
        }

        return Math.min(100, score);
    }

    private Set<String> extractKeywords(String text) {
        if (text == null) return Collections.emptySet();
        Set<String> keywords = new HashSet<>();
        String[] tokens = text.toLowerCase().replaceAll("[^a-z0-9 ]", "").split("\\s+");
        Set<String> stopWords = Set.of("the", "and", "with", "this", "that", "near", "found", "lost", "for", "from");
        for (String t : tokens) {
            if (t.length() >= 3 && !stopWords.contains(t)) {
                keywords.add(t);
            }
        }
        return keywords;
    }
}
