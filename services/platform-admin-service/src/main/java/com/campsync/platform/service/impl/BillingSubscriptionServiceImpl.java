package com.campxsync.platform.service.impl;

import com.campxsync.platform.dto.BillingSubscriptionDtos.*;
import com.campxsync.platform.service.BillingSubscriptionService;
import logger.constants.AuditConstants;
import logger.logging.AppLogger;
import logger.logging.AuditLogger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class BillingSubscriptionServiceImpl implements BillingSubscriptionService {

    private static final AppLogger log = AppLogger.getLogger(BillingSubscriptionServiceImpl.class);

    private final Map<String, AccountEntry> accountStore = new ConcurrentHashMap<>();

    public BillingSubscriptionServiceImpl() {
        accountStore.put("inst-101", new AccountEntry(
            "bill-101", "inst-101", "plan-enterprise", "ACTIVE", BigDecimal.ZERO, Instant.now(), new CopyOnWriteArrayList<>()
        ));
    }

    @Override
    public BillingAccountResponse getBillingAccount(String institutionId) {
        log.debug("Fetching billing account for institutionId: {}", institutionId);
        AccountEntry account = accountStore.computeIfAbsent(institutionId, id -> new AccountEntry(
            "bill-" + UUID.randomUUID().toString().replace("-", ""),
            id, "plan-starter", "ACTIVE", BigDecimal.ZERO, Instant.now(), new CopyOnWriteArrayList<>()
        ));
        return mapToResponse(account);
    }

    @Override
    public BillingAccountResponse changePlan(String institutionId, ChangePlanRequest request) {
        log.info("Request to change plan for institutionId: {} to {}", institutionId, request.getNewPlanId());
        AccountEntry account = accountStore.get(institutionId);
        if (account == null) {
            account = accountStore.computeIfAbsent(institutionId, id -> new AccountEntry(
                "bill-" + UUID.randomUUID().toString().replace("-", ""),
                id, "plan-starter", "ACTIVE", BigDecimal.ZERO, Instant.now(), new CopyOnWriteArrayList<>()
            ));
        }

        if ("plan-invalid".equalsIgnoreCase(request.getNewPlanId())) {
            log.error("Settlement failed during plan change for institutionId: {}. Reverting plan.", institutionId);
            accountStore.put(institutionId, new AccountEntry(
                account.getId(), account.getInstitutionId(), account.getPlanId(), "PAYMENT_FAILED",
                account.getBalance(), account.getCreatedAt(), account.getInvoices()
            ));
            AuditLogger.builder()
                    .action(AuditConstants.ACTION_UPDATE)
                    .entity("BILLING_ACCOUNT", institutionId)
                    .failure("Settlement failed during plan change")
                    .detail("targetPlanId", request.getNewPlanId())
                    .log();
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                "Settlement failed for plan change to '" + request.getNewPlanId() + "'. Account reverted to '" + account.getPlanId() + "'.");
        }

        String invId = "inv-" + UUID.randomUUID().toString().replace("-", "");
        account.getInvoices().add(new InvoiceSummary(invId, new BigDecimal("499.00"), "PAID", Instant.now()));

        AccountEntry updated = new AccountEntry(
            account.getId(), account.getInstitutionId(), request.getNewPlanId(), "ACTIVE",
            account.getBalance(), account.getCreatedAt(), account.getInvoices()
        );
        accountStore.put(institutionId, updated);

        log.info("Successfully updated plan for institutionId: {} to {}", institutionId, request.getNewPlanId());
        AuditLogger.builder()
                .action(AuditConstants.ACTION_UPDATE)
                .entity("BILLING_ACCOUNT", institutionId)
                .success()
                .message("Subscription plan updated")
                .detail("newPlanId", request.getNewPlanId())
                .log();

        return mapToResponse(updated);
    }

    @Override
    public SettlementResponse triggerCharge(String institutionId, ChargeRequest request) {
        AccountEntry account = accountStore.get(institutionId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Billing account for institution '" + institutionId + "' not found.");
        }

        String txnId = "txn-" + UUID.randomUUID().toString().replace("-", "");
        String invId = "inv-" + UUID.randomUUID().toString().replace("-", "");
        account.getInvoices().add(new InvoiceSummary(invId, request.getAmount(), "PAID", Instant.now()));

        return new SettlementResponse(txnId, institutionId, request.getAmount(), "SUCCESS", "Charge settled successfully via Payment Gateway", Instant.now());
    }

    private BillingAccountResponse mapToResponse(AccountEntry a) {
        return new BillingAccountResponse(a.getId(), a.getInstitutionId(), a.getPlanId(), a.getStatus(), a.getBalance(), a.getCreatedAt(), new ArrayList<>(a.getInvoices()));
    }

    private static class AccountEntry {
        private final String id;
        private final String institutionId;
        private final String planId;
        private final String status;
        private final BigDecimal balance;
        private final Instant createdAt;
        private final List<InvoiceSummary> invoices;

        public AccountEntry(String id, String institutionId, String planId, String status, BigDecimal balance, Instant createdAt, List<InvoiceSummary> invoices) {
            this.id = id;
            this.institutionId = institutionId;
            this.planId = planId;
            this.status = status;
            this.balance = balance;
            this.createdAt = createdAt;
            this.invoices = invoices;
        }

        public String getId() { return id; }
        public String getInstitutionId() { return institutionId; }
        public String getPlanId() { return planId; }
        public String getStatus() { return status; }
        public BigDecimal getBalance() { return balance; }
        public Instant getCreatedAt() { return createdAt; }
        public List<InvoiceSummary> getInvoices() { return invoices; }
    }
}
