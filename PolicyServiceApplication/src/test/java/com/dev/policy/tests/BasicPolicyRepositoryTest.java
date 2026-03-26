package com.dev.policy.tests;


import com.dev.policy.entity.BasicPolicy;
import com.dev.policy.entity.PolicyStatus;
import com.dev.policy.entity.PolicyType;
import com.dev.policy.repository.BasicPolicyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BasicPolicyRepositoryTest {

    @Autowired BasicPolicyRepository repo;

    @BeforeEach
    void setUp() {
        repo.save(BasicPolicy.builder()
                .policyName("Active Home")
                .type(PolicyType.HOME)
                .basePremium(1000.0).maxPremium(5000.0)
                .status(PolicyStatus.ACTIVE).build());

        repo.save(BasicPolicy.builder()
                .policyName("Inactive Vehicle")
                .type(PolicyType.VEHICLE)
                .basePremium(500.0).maxPremium(3000.0)
                .status(PolicyStatus.INACTIVE).build());
    }

    @Test
    @DisplayName("findByStatus(ACTIVE) should return only active policies")
    void findByStatus_active() {
        Page<BasicPolicy> result = repo.findByStatus(
                PolicyStatus.ACTIVE, PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPolicyName()).isEqualTo("Active Home");
    }

    @Test
    @DisplayName("findByStatus(INACTIVE) should return only inactive policies")
    void findByStatus_inactive() {
        Page<BasicPolicy> result = repo.findByStatus(
                PolicyStatus.INACTIVE, PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPolicyName()).isEqualTo("Inactive Vehicle");
    }

    @Test
    @DisplayName("should persist and retrieve policy correctly")
    void save_and_findById() {
        BasicPolicy saved = repo.save(BasicPolicy.builder()
                .policyName("Test Policy")
                .type(PolicyType.HOME)
                .basePremium(200.0).maxPremium(1000.0)
                .status(PolicyStatus.ACTIVE).build());

        assertThat(repo.findById(saved.getId())).isPresent();
    }
}