package com.campxsync.platform.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class BillingSubscriptionDtos {

    public static class ChangePlanRequest {
        @NotBlank(message = "Plan ID is required")
        private String newPlanId;

        public ChangePlanRequest() {}
        public ChangePlanRequest(String newPlanId) { this.newPlanId = newPlanId; }
        public String getNewPlanId() { return newPlanId; }
        public void setNewPlanId(String newPlanId) { this.newPlanId = newPlanId; }
    }

    public static class ChargeRequest {
        @Positive(message = "Amount must be positive")
        private BigDecimal amount;
        @NotBlank(message = "Description is required")
        private String description;

        public ChargeRequest() {}
        public ChargeRequest(BigDecimal amount, String description) {
            this.amount = amount;
            this.description = description;
        }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class BillingAccountResponse {
        private String id;
        private String institutionId;
        private String planId;
        private String status;
        private BigDecimal balance;
        private Instant createdAt;
        private List<InvoiceSummary> invoiceHistory;

        public BillingAccountResponse() {}
        public BillingAccountResponse(String id, String institutionId, String planId, String status, BigDecimal balance, Instant createdAt, List<InvoiceSummary> invoiceHistory) {
            this.id = id;
            this.institutionId = institutionId;
            this.planId = planId;
            this.status = status;
            this.balance = balance;
            this.createdAt = createdAt;
            this.invoiceHistory = invoiceHistory;
        }

        public String getId() { return id; }
        public String getInstitutionId() { return institutionId; }
        public String getPlanId() { return planId; }
        public String getStatus() { return status; }
        public BigDecimal getBalance() { return balance; }
        public Instant getCreatedAt() { return createdAt; }
        public List<InvoiceSummary> getInvoiceHistory() { return invoiceHistory; }
    }

    public static class InvoiceSummary {
        private String invoiceId;
        private BigDecimal amount;
        private String status;
        private Instant issuedAt;

        public InvoiceSummary() {}
        public InvoiceSummary(String invoiceId, BigDecimal amount, String status, Instant issuedAt) {
            this.invoiceId = invoiceId;
            this.amount = amount;
            this.status = status;
            this.issuedAt = issuedAt;
        }

        public String getInvoiceId() { return invoiceId; }
        public BigDecimal getAmount() { return amount; }
        public String getStatus() { return status; }
        public Instant getIssuedAt() { return issuedAt; }
    }

    public static class SettlementResponse {
        private String transactionId;
        private String institutionId;
        private BigDecimal amount;
        private String status;
        private String message;
        private Instant settledAt;

        public SettlementResponse() {}
        public SettlementResponse(String transactionId, String institutionId, BigDecimal amount, String status, String message, Instant settledAt) {
            this.transactionId = transactionId;
            this.institutionId = institutionId;
            this.amount = amount;
            this.status = status;
            this.message = message;
            this.settledAt = settledAt;
        }

        public String getTransactionId() { return transactionId; }
        public String getInstitutionId() { return institutionId; }
        public BigDecimal getAmount() { return amount; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public Instant getSettledAt() { return settledAt; }
    }
}
