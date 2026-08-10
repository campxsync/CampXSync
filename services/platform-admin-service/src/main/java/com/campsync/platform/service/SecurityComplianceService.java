package com.campsync.platform.service;

import com.campsync.platform.dto.SecurityComplianceDtos.*;
import java.util.List;

public interface SecurityComplianceService {
    ComplianceCheckTriggerResponse runComplianceCheck(RunComplianceCheckRequest request);
    ComplianceCheckResultResponse getLatestResultForInstitute(String institutionId);
    List<ComplianceCheckResultResponse> listNonCompliantInstitutes();
}
