package com.dev.policy.tests;


import com.dev.policy.dto.BasicPolicyRequest;
import com.dev.policy.dto.BasicPolicyResponse;
import com.dev.policy.entity.BasicPolicy;
import com.dev.policy.entity.PolicyStatus;
import com.dev.policy.entity.PolicyType;
import com.dev.policy.exception.InvalidFieldValueException;
import com.dev.policy.exception.MissingRequiredFieldException;
import com.dev.policy.exception.PolicyNotFoundException;
import com.dev.policy.repository.BasicPolicyRepository;
import com.dev.policy.service.BasicPolicyService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicPolicyServiceTest {

    @Mock private BasicPolicyRepository repo;
    @InjectMocks private BasicPolicyService service;

    private BasicPolicy activePolicy;
    private BasicPolicyRequest validRequest;

    @BeforeEach
    void setUp() {
        activePolicy = BasicPolicy.builder()
                .id(1L)
                .policyName("Home Basic")
                .type(PolicyType.HOME)
                .basePremium(1000.0)
                .maxPremium(5000.0)
                .status(PolicyStatus.ACTIVE)
                .build();

        validRequest = new BasicPolicyRequest();
        validRequest.setPolicyName("Home Basic");
        validRequest.setType(PolicyType.HOME);
        validRequest.setBasePremium(1000.0);
        validRequest.setMaxPremium(5000.0);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create policy successfully with valid request")
        void create_success() {
            when(repo.save(any())).thenReturn(activePolicy);

            BasicPolicyResponse response = service.create(validRequest);

            assertThat(response.getPolicyName()).isEqualTo("Home Basic");
            assertThat(response.getStatus()).isEqualTo(PolicyStatus.ACTIVE);
            verify(repo, times(1)).save(any());
        }

        @Test
        @DisplayName("should throw MissingRequiredFieldException when policyName is null")
        void create_missingPolicyName() {
            validRequest.setPolicyName(null);
            assertThatThrownBy(() -> service.create(validRequest))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessageContaining("policyName");
        }

        @Test
        @DisplayName("should throw MissingRequiredFieldException when policyName is blank")
        void create_blankPolicyName() {
            validRequest.setPolicyName("   ");
            assertThatThrownBy(() -> service.create(validRequest))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessageContaining("policyName");
        }

        @Test
        @DisplayName("should throw MissingRequiredFieldException when type is null")
        void create_missingType() {
            validRequest.setType(null);
            assertThatThrownBy(() -> service.create(validRequest))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessageContaining("type");
        }

        @Test
        @DisplayName("should throw MissingRequiredFieldException when basePremium is null")
        void create_missingBasePremium() {
            validRequest.setBasePremium(null);
            assertThatThrownBy(() -> service.create(validRequest))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessageContaining("basePremium");
        }

        @Test
        @DisplayName("should throw MissingRequiredFieldException when maxPremium is null")
        void create_missingMaxPremium() {
            validRequest.setMaxPremium(null);
            assertThatThrownBy(() -> service.create(validRequest))
                    .isInstanceOf(MissingRequiredFieldException.class)
                    .hasMessageContaining("maxPremium");
        }

        @Test
        @DisplayName("should throw InvalidFieldValueException when basePremium > maxPremium")
        void create_basePremiumGreaterThanMax() {
            validRequest.setBasePremium(9000.0);
            validRequest.setMaxPremium(1000.0);
            assertThatThrownBy(() -> service.create(validRequest))
                    .isInstanceOf(InvalidFieldValueException.class)
                    .hasMessageContaining("basePremium");
        }

        @Test
        @DisplayName("should allow equal basePremium and maxPremium")
        void create_equalPremiums() {
            validRequest.setBasePremium(1000.0);
            validRequest.setMaxPremium(1000.0);
            when(repo.save(any())).thenReturn(activePolicy);
            assertThatCode(() -> service.create(validRequest)).doesNotThrowAnyException();
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("should update policy successfully")
        void update_success() {
            when(repo.findById(1L)).thenReturn(Optional.of(activePolicy));
            when(repo.save(any())).thenReturn(activePolicy);

            BasicPolicyResponse response = service.update(1L, validRequest);

            assertThat(response).isNotNull();
            verify(repo).save(any());
        }

        @Test
        @DisplayName("should throw PolicyNotFoundException for unknown id")
        void update_notFound() {
            when(repo.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.update(99L, validRequest))
                    .isInstanceOf(PolicyNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("should throw InvalidFieldValueException when basePremium > maxPremium on update")
        void update_invalidPremiumRange() {
            when(repo.findById(1L)).thenReturn(Optional.of(activePolicy));
            validRequest.setBasePremium(8000.0);
            validRequest.setMaxPremium(2000.0);
            assertThatThrownBy(() -> service.update(1L, validRequest))
                    .isInstanceOf(InvalidFieldValueException.class);
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should delete policy successfully")
        void delete_success() {
            when(repo.findById(1L)).thenReturn(Optional.of(activePolicy));
            assertThatCode(() -> service.delete(1L)).doesNotThrowAnyException();
            verify(repo).deleteById(1L);
        }

        @Test
        @DisplayName("should throw PolicyNotFoundException when deleting unknown id")
        void delete_notFound() {
            when(repo.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.delete(99L))
                    .isInstanceOf(PolicyNotFoundException.class)
                    .hasMessageContaining("99");
            verify(repo, never()).deleteById(any());
        }
    }

    // ── UPDATE STATUS ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("should set status to INACTIVE successfully")
        void updateStatus_success() {
            when(repo.findById(1L)).thenReturn(Optional.of(activePolicy));
            when(repo.save(any())).thenReturn(activePolicy);

            BasicPolicyResponse response = service.updateStatus(1L, PolicyStatus.INACTIVE);
            assertThat(response).isNotNull();
            verify(repo).save(any());
        }

        @Test
        @DisplayName("should throw PolicyNotFoundException for unknown id")
        void updateStatus_notFound() {
            when(repo.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.updateStatus(99L, PolicyStatus.INACTIVE))
                    .isInstanceOf(PolicyNotFoundException.class);
        }
    }

    // ── GET ───────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("should return policy for valid id")
        void getById_success() {
            when(repo.findById(1L)).thenReturn(Optional.of(activePolicy));
            BasicPolicyResponse response = service.getById(1L);
            assertThat(response.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw PolicyNotFoundException for unknown id")
        void getById_notFound() {
            when(repo.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.getById(99L))
                    .isInstanceOf(PolicyNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("getActive()")
    class GetActive {

        @Test
        @DisplayName("should return only active policies")
        void getActive_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BasicPolicy> page = new PageImpl<>(List.of(activePolicy));
            when(repo.findByStatus(PolicyStatus.ACTIVE, pageable)).thenReturn(page);

            Page<BasicPolicyResponse> result = service.getActive(pageable);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(PolicyStatus.ACTIVE);
        }

        @Test
        @DisplayName("should return empty page when no active policies")
        void getActive_empty() {
            Pageable pageable = PageRequest.of(0, 10);
            when(repo.findByStatus(PolicyStatus.ACTIVE, pageable))
                    .thenReturn(Page.empty());
            Page<BasicPolicyResponse> result = service.getActive(pageable);
            assertThat(result.getContent()).isEmpty();
        }
    }
}