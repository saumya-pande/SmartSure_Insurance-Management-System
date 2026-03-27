package com.dev.policy.client;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class KycClientFallback implements KycClient {
    
    private static final Logger logger = LoggerFactory.getLogger(KycClientFallback.class);

    @Override
    public KycResponse getMyKyc(String email, String role) {
        logger.error("KYC Service is down or unreachable. Executing Fallback for getMyKyc.");
        return new KycResponse();
    }
}
