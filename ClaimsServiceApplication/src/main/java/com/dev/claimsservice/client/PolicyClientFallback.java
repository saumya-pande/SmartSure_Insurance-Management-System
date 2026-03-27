package com.dev.claimsservice.client;

import org.springframework.stereotype.Component;
import com.dev.claimsservice.dto.CustomerPolicyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PolicyClientFallback implements PolicyClient {

    private static final Logger logger = LoggerFactory.getLogger(PolicyClientFallback.class);

    @Override
    public CustomerPolicyResponse getPolicyById(Long id, String email, String role) {
        logger.error("Policy Service is down or unreachable. Executing Fallback for getPolicyById.");
        return new CustomerPolicyResponse(); 
    }
}
