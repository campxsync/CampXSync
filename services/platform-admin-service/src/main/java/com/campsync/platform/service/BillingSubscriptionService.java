package com.campsync.platform.service;

import com.campsync.platform.dto.BillingSubscriptionDtos.*;

public interface BillingSubscriptionService {
    BillingAccountResponse getBillingAccount(String institutionId);
    BillingAccountResponse changePlan(String institutionId, ChangePlanRequest request);
    SettlementResponse triggerCharge(String institutionId, ChargeRequest request);
}
