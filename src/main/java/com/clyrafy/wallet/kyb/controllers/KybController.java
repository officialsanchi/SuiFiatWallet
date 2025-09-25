package com.clyrafy.wallet.kyb.controllers;

import com.clyrafy.wallet.kyb.enums.KybStatus;
import com.clyrafy.wallet.kyb.service.KybService;
import com.clyrafy.wallet.org.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/kyc")
public class KybController {
    private final KybService kybService;
    private final UserService userService;

    public KybController(KybService kybService, UserService userService) {
        this.kybService = kybService;
        this.userService = userService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getKycStatus() {
        UUID orgId = userService.getCurrentUserOrganizationId();
        KybStatus status = kybService.getKybStatus(orgId);
        return ResponseEntity.ok(Map.of("status", status.name()));
    }

    //admin
    @PostMapping("/update")
    public ResponseEntity<?> updateKycStatus(@RequestParam UUID orgId, @RequestParam KybStatus status) {
        kybService.updateKycStatus(orgId, status);
        return ResponseEntity.ok(Map.of("message", "KYC updated to " + status));
    }
}
