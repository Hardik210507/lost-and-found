package com.campus;

import com.campus.controller.ItemController;
import com.campus.controller.ItemMatchController;
import com.campus.model.Claim;
import com.campus.model.Item;
import com.campus.model.ItemMatch;
import com.campus.repository.ClaimRepository;
import com.campus.repository.ItemMatchRepository;
import com.campus.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemMatchingFlowTest {

    @Autowired
    private ItemController itemController;

    @Autowired
    private ItemMatchController itemMatchController;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemMatchRepository matchRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Test
    @DisplayName("End-to-end: post items, match reciprocal generation, claim creation, and clean deletion (204)")
    void testMatchingAndDeletionFlow() {
        String uniqueSuffix = "_" + System.currentTimeMillis();
        // 1. Post a LOST item
        Item lost = new Item();
        lost.setType("LOST");
        lost.setTitle("Black leather wallet");
        lost.setDescription("Bi-fold wallet, has a metro card and a photo inside.");
        lost.setCategory("TestCategory" + uniqueSuffix);
        lost.setLocation("Cafeteria" + uniqueSuffix);
        lost.setPostedBy("Alice");

        Item savedLost = itemController.createItem(lost);
        assertNotNull(savedLost.getId());
        assertEquals("PENDING", savedLost.getStatus());

        // 2. Post an UNRELATED FOUND item -> should create NO matches
        Item unrelated = new Item();
        unrelated.setType("FOUND");
        unrelated.setTitle("Green garden umbrella");
        unrelated.setDescription("Found near the stadium after rainfall");
        unrelated.setCategory("OtherCategory" + uniqueSuffix);
        unrelated.setLocation("Stadium" + uniqueSuffix);
        unrelated.setPostedBy("Bob");

        Item savedUnrelated = itemController.createItem(unrelated);
        assertNotNull(savedUnrelated.getId());

        List<ItemMatch> lostMatchesBefore = matchRepository.findByItemId(savedLost.getId());
        assertTrue(lostMatchesBefore.isEmpty(), "Unrelated item should not generate any matches");

        // 3. Post a MATCHING FOUND item
        Item found = new Item();
        found.setType("FOUND");
        found.setTitle("Black wallet near canteen");
        found.setDescription("Found under a table, looks bi-fold, didn't check inside.");
        found.setCategory("TestCategory" + uniqueSuffix);
        found.setLocation("Cafeteria" + uniqueSuffix);
        found.setPostedBy("Charlie");

        Item savedFound = itemController.createItem(found);
        assertNotNull(savedFound.getId());

        // Check both items have status MATCHED
        Item refreshedLost = itemRepository.findById(savedLost.getId()).orElseThrow();
        Item refreshedFound = itemRepository.findById(savedFound.getId()).orElseThrow();
        assertEquals("MATCHED", refreshedLost.getStatus());
        assertEquals("MATCHED", refreshedFound.getStatus());

        // Check matches endpoint for lost item
        List<ItemMatch> matchesForLost = itemMatchController.getMatchesForItem(savedLost.getId());
        assertEquals(1, matchesForLost.size());
        assertEquals(84, matchesForLost.get(0).getScore());
        assertNotNull(matchesForLost.get(0).getReason());
        assertTrue(matchesForLost.get(0).getReason().contains("Category +40"));
        assertEquals(savedFound.getId(), matchesForLost.get(0).getMatchedItem().getId());

        // Check reciprocal match for found item
        List<ItemMatch> matchesForFound = itemMatchController.getMatchesForItem(savedFound.getId());
        assertEquals(1, matchesForFound.size());
        assertEquals(84, matchesForFound.get(0).getScore());
        assertEquals(matchesForLost.get(0).getReason(), matchesForFound.get(0).getReason());
        assertEquals(savedLost.getId(), matchesForFound.get(0).getMatchedItem().getId());

        // 4. Create a claim for the lost item
        Claim claim = new Claim();
        claim.setItem(savedLost);
        claim.setClaimant("Alice (alice@campus.edu)");
        claim.setIdentifyingDetail("Drivers license inside slot 2");
        claim.setStatus("PENDING");
        claimRepository.save(claim);

        assertFalse(claimRepository.findByItemId(savedLost.getId()).isEmpty());

        // 5. Delete the lost item that has matches AND a claim -> must return 204 No Content
        ResponseEntity<Void> deleteResponse = itemController.deleteItem(savedLost.getId());
        assertEquals(204, deleteResponse.getStatusCode().value());

        // Verify item is removed
        assertFalse(itemRepository.existsById(savedLost.getId()));

        // Verify claims are removed
        assertTrue(claimRepository.findByItemId(savedLost.getId()).isEmpty());

        // Verify matches for lost item are removed
        assertTrue(matchRepository.findByItemId(savedLost.getId()).isEmpty());

        // Verify partner item (savedFound) is reset back to PENDING because it has no other matches
        Item partnerAfter = itemRepository.findById(savedFound.getId()).orElseThrow();
        assertEquals("PENDING", partnerAfter.getStatus());

        // 6. Test recompute endpoint
        Map<String, Integer> recomputeRes = itemMatchController.recomputeMatches();
        assertNotNull(recomputeRes);
        assertTrue(recomputeRes.containsKey("matchPairsStored"));

        // Clean up remaining test items
        itemController.deleteItem(savedFound.getId());
        itemController.deleteItem(savedUnrelated.getId());
    }
}
