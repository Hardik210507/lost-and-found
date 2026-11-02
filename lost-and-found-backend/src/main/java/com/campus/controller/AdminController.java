package com.campus.controller;

import com.campus.service.ItemStatusScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin-only utility endpoints.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(originPatterns = "*")
public class AdminController {

    private final ItemStatusScheduler scheduler;

    public AdminController(ItemStatusScheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * POST /api/admin/expire-unclaimed?days=7
     *
     * Manually triggers the expiry job. Accepts an optional `days` param
     * so admins can test with a smaller window (e.g. days=0 marks every
     * PENDING item with no claim as UNRESOLVED immediately).
     */
    @PostMapping("/expire-unclaimed")
    public ResponseEntity<Map<String, Object>> expireUnclaimed(
            @RequestParam(defaultValue = "7") int days) {
        int expired = scheduler.runExpiry(days);
        return ResponseEntity.ok(Map.of(
                "expiredCount", expired,
                "daysThreshold", days,
                "message", expired + " item(s) marked UNRESOLVED."
        ));
    }
}
