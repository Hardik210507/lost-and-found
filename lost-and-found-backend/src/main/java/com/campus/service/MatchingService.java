package com.campus.service;

import com.campus.model.Item;
import com.campus.model.ItemMatch;
import com.campus.repository.ItemMatchRepository;
import com.campus.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MatchingService {

    private final ItemRepository itemRepository;
    private final ItemMatchRepository matchRepository;

    public MatchingService(ItemRepository itemRepository, ItemMatchRepository matchRepository) {
        this.itemRepository = itemRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional
    public void calculateMatchesForNewItem(Item newItem) {
        if (newItem == null || newItem.getType() == null) return;

        // Compare against opposite type (LOST compares with FOUND, FOUND compares with LOST)
        String targetType = "LOST".equalsIgnoreCase(newItem.getType()) ? "FOUND" : "LOST";
        List<Item> candidates = itemRepository.findAll().stream()
                .filter(i -> targetType.equalsIgnoreCase(i.getType()))
                .filter(i -> !"RESOLVED".equalsIgnoreCase(i.getStatus()))
                .filter(i -> !Objects.equals(i.getId(), newItem.getId()))
                .toList();

        MatchScorer.Candidate newCandidate = MatchScorer.Candidate.of(
                newItem.getCategory(),
                newItem.getLocation(),
                newItem.getTitle(),
                newItem.getDescription(),
                newItem.getImagePath()
        );

        boolean newItemMatched = false;

        for (Item candidate : candidates) {
            MatchScorer.Candidate cand = MatchScorer.Candidate.of(
                    candidate.getCategory(),
                    candidate.getLocation(),
                    candidate.getTitle(),
                    candidate.getDescription(),
                    candidate.getImagePath()
            );

            MatchScorer.Result result = MatchScorer.score(newCandidate, cand);
            if (result.isMatch()) {
                ItemMatch match1 = new ItemMatch();
                match1.setItem(newItem);
                match1.setMatchedItem(candidate);
                match1.setScore(result.total());
                match1.setReason(result.explain());
                matchRepository.save(match1);

                ItemMatch match2 = new ItemMatch();
                match2.setItem(candidate);
                match2.setMatchedItem(newItem);
                match2.setScore(result.total());
                match2.setReason(result.explain());
                matchRepository.save(match2);

                newItemMatched = true;

                if ("PENDING".equalsIgnoreCase(candidate.getStatus())) {
                    candidate.setStatus("MATCHED");
                    itemRepository.save(candidate);
                }
            }
        }

        if (newItemMatched && "PENDING".equalsIgnoreCase(newItem.getStatus())) {
            newItem.setStatus("MATCHED");
            itemRepository.save(newItem);
        }
    }

    @Transactional
    public int recomputeAll() {
        matchRepository.deleteAllInBatch();

        List<Item> allItems = itemRepository.findAll();
        List<Item> lostItems = allItems.stream()
                .filter(i -> "LOST".equalsIgnoreCase(i.getType()))
                .filter(i -> !"RESOLVED".equalsIgnoreCase(i.getStatus()))
                .toList();
        List<Item> foundItems = allItems.stream()
                .filter(i -> "FOUND".equalsIgnoreCase(i.getType()))
                .filter(i -> !"RESOLVED".equalsIgnoreCase(i.getStatus()))
                .toList();

        Map<Long, MatchScorer.Candidate> candidateMap = new HashMap<>();
        for (Item item : lostItems) {
            candidateMap.put(item.getId(), MatchScorer.Candidate.of(
                    item.getCategory(), item.getLocation(), item.getTitle(), item.getDescription(), item.getImagePath()
            ));
        }
        for (Item item : foundItems) {
            candidateMap.put(item.getId(), MatchScorer.Candidate.of(
                    item.getCategory(), item.getLocation(), item.getTitle(), item.getDescription(), item.getImagePath()
            ));
        }

        Set<Long> matchedItemIds = new HashSet<>();
        int pairsStored = 0;

        for (Item lost : lostItems) {
            MatchScorer.Candidate candLost = candidateMap.get(lost.getId());
            for (Item found : foundItems) {
                if (Objects.equals(lost.getId(), found.getId())) continue;
                MatchScorer.Candidate candFound = candidateMap.get(found.getId());

                MatchScorer.Result result = MatchScorer.score(candLost, candFound);
                if (result.isMatch()) {
                    ItemMatch match1 = new ItemMatch();
                    match1.setItem(lost);
                    match1.setMatchedItem(found);
                    match1.setScore(result.total());
                    match1.setReason(result.explain());
                    matchRepository.save(match1);

                    ItemMatch match2 = new ItemMatch();
                    match2.setItem(found);
                    match2.setMatchedItem(lost);
                    match2.setScore(result.total());
                    match2.setReason(result.explain());
                    matchRepository.save(match2);

                    matchedItemIds.add(lost.getId());
                    matchedItemIds.add(found.getId());
                    pairsStored++;
                }
            }
        }

        for (Item item : allItems) {
            if ("RESOLVED".equalsIgnoreCase(item.getStatus())) {
                continue;
            }
            if (matchedItemIds.contains(item.getId())) {
                if (!"MATCHED".equalsIgnoreCase(item.getStatus())) {
                    item.setStatus("MATCHED");
                    itemRepository.save(item);
                }
            } else {
                if ("MATCHED".equalsIgnoreCase(item.getStatus())) {
                    item.setStatus("PENDING");
                    itemRepository.save(item);
                }
            }
        }

        return pairsStored;
    }

    @Transactional
    public void removeMatchesFor(Long itemId) {
        if (itemId == null) return;

        List<ItemMatch> matchesByItem = matchRepository.findByItemId(itemId);
        Set<Long> partnerIds = new HashSet<>();
        for (ItemMatch m : matchesByItem) {
            if (m.getMatchedItem() != null && !Objects.equals(m.getMatchedItem().getId(), itemId)) {
                partnerIds.add(m.getMatchedItem().getId());
            }
        }

        for (ItemMatch m : matchRepository.findAll()) {
            if (m.getMatchedItem() != null && Objects.equals(m.getMatchedItem().getId(), itemId)) {
                if (m.getItem() != null && !Objects.equals(m.getItem().getId(), itemId)) {
                    partnerIds.add(m.getItem().getId());
                }
            }
        }

        matchRepository.deleteByItemIdOrMatchedItemId(itemId, itemId);

        for (Long partnerId : partnerIds) {
            itemRepository.findById(partnerId).ifPresent(partner -> {
                if ("MATCHED".equalsIgnoreCase(partner.getStatus())) {
                    List<ItemMatch> remaining = matchRepository.findByItemId(partnerId);
                    if (remaining.isEmpty()) {
                        partner.setStatus("PENDING");
                        itemRepository.save(partner);
                    }
                }
            });
        }
    }
}
