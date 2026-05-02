package com.dev.policy.init;

import com.dev.policy.entity.BasicPolicy;
import com.dev.policy.entity.PolicyStatus;
import com.dev.policy.entity.PolicyType;
import com.dev.policy.repository.BasicPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyDataInitializer implements CommandLineRunner {

    private final BasicPolicyRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Basic policies already exist. Skipping initialization.");
            return;
        }

        log.info("Initializing basic policies with Indian market data...");

        List<BasicPolicy> policies = Arrays.asList(
            BasicPolicy.builder()
                .policyName("Comprehensive Car Secure")
                .type(PolicyType.VEHICLE)
                .basePremium(4500.0)
                .maxPremium(12000.0)
                .maxMonthCoverage(12)
                .billingCycle(com.dev.policy.entity.BillingCycle.YEARLY)
                .description("Full coverage for your car including third-party liability and own damage. Includes zero depreciation cover.")
                .status(PolicyStatus.ACTIVE)
                .build(),

            BasicPolicy.builder()
                .policyName("Two-Wheeler Protect")
                .type(PolicyType.VEHICLE)
                .basePremium(850.0)
                .maxPremium(3000.0)
                .maxMonthCoverage(36)
                .billingCycle(com.dev.policy.entity.BillingCycle.YEARLY)
                .description("Essential protection for bikes and scooters. Covers accidents, theft, and natural calamities. Long-term 3-year plan.")
                .status(PolicyStatus.ACTIVE)
                .build(),

            BasicPolicy.builder()
                .policyName("Home Shield Basic")
                .type(PolicyType.HOME)
                .basePremium(1200.0)
                .maxPremium(4500.0)
                .maxMonthCoverage(60)
                .billingCycle(com.dev.policy.entity.BillingCycle.YEARLY)
                .description("Covers the structure of your home against fire, lightning, and earthquake. Ideal for apartment owners.")
                .status(PolicyStatus.ACTIVE)
                .build(),

            BasicPolicy.builder()
                .policyName("Premium Home & Content")
                .type(PolicyType.HOME)
                .basePremium(3500.0)
                .maxPremium(15000.0)
                .maxMonthCoverage(120)
                .billingCycle(com.dev.policy.entity.BillingCycle.MONTHLY)
                .description("Comprehensive protection for both home structure and valuable contents like jewelry and electronics.")
                .status(PolicyStatus.ACTIVE)
                .build(),

            BasicPolicy.builder()
                .policyName("Commercial Vehicle Prime")
                .type(PolicyType.VEHICLE)
                .basePremium(8000.0)
                .maxPremium(25000.0)
                .maxMonthCoverage(12)
                .billingCycle(com.dev.policy.entity.BillingCycle.YEARLY)
                .description("Tailored coverage for taxis and delivery vans. High liability limits and specialized breakdown assistance.")
                .status(PolicyStatus.ACTIVE)
                .build()
        );

        repository.saveAll(policies);
        log.info("Successfully initialized {} new policies.", policies.size());

        // Update existing policies that might have been created before the metadata fields were added
        List<BasicPolicy> existing = repository.findAll();
        boolean updated = false;
        for (BasicPolicy p : existing) {
            if (p.getDescription() == null || p.getMaxMonthCoverage() == null || p.getBillingCycle() == null) {
                if (p.getDescription() == null) p.setDescription("Standard coverage for " + p.getPolicyName() + ". Includes basic protection against common risks.");
                if (p.getMaxMonthCoverage() == null) p.setMaxMonthCoverage(12);
                if (p.getBillingCycle() == null) p.setBillingCycle(com.dev.policy.entity.BillingCycle.YEARLY);
                repository.save(p);
                updated = true;
            }
        }
        if (updated) log.info("Updated existing policies with missing metadata.");
    }
}
