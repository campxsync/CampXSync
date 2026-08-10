package com.campsync.platform.controller;

import com.campsync.platform.dto.BillingSubscriptionDtos.*;
import com.campsync.platform.service.BillingSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/billing-accounts")
@Tag(name = "Billing & Subscription", description = "Institute billing account monitoring, subscription plan changes, and manual settlements")
public class BillingSubscriptionController {

    private final BillingSubscriptionService billingService;

    public BillingSubscriptionController(BillingSubscriptionService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/{institution_id}")
    @Operation(summary = "View an institute's billing and plan status", description = "Fetches current subscription plan, status, and billing history")
    public ResponseEntity<BillingAccountResponse> getBillingAccount(@PathVariable("institution_id") String institutionId) {
        BillingAccountResponse response = billingService.getBillingAccount(institutionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{institution_id}/change-plan")
    @Operation(summary = "Change an institute's subscription plan", description = "Upgrades or downgrades plan after payment gateway settlement confirmation")
    public ResponseEntity<BillingAccountResponse> changePlan(
            @PathVariable("institution_id") String institutionId,
            @Valid @RequestBody ChangePlanRequest request) {
        BillingAccountResponse response = billingService.changePlan(institutionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{institution_id}/charge")
    @Operation(summary = "Trigger manual settlement", description = "Executes a manual settlement charge against Payment Gateway")
    public ResponseEntity<SettlementResponse> triggerCharge(
            @PathVariable("institution_id") String institutionId,
            @Valid @RequestBody ChargeRequest request) {
        SettlementResponse response = billingService.triggerCharge(institutionId, request);
        return ResponseEntity.ok(response);
    }
}
