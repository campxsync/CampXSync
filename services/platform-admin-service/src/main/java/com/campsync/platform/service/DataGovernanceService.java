package com.campxsync.platform.service;

import com.campxsync.platform.dto.DataGovernanceDtos.*;
import java.util.List;

public interface DataGovernanceService {
    PolicyResponse createPolicy(CreatePolicyRequest request);
    List<PolicyResponse> listPolicies(String appliesTo);
    PolicyResponse updatePolicy(String id, UpdatePolicyRequest request);
    PolicyResponse retirePolicy(String id);
}
