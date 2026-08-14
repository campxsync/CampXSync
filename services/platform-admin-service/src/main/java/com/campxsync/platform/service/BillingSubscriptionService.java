package com.campxsync.platform.service;

import com.campxsync.platform.dto.BillingSubscriptionDtos.*;

public interface BillingSubscriptionService {
    BillingAccountResponse getBillingAccount(String institutionId);
    BillingAccountResponse changePlan(String institutionId, ChangePlanRequest request);
    SettlementResponse triggerCharge(String institutionId, ChargeRequest request);
}
