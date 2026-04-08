package com.dev.policy.service;

import com.dev.policy.dto.*;
import com.dev.policy.dto.RestPage;
import com.dev.policy.entity.*;
import com.dev.policy.exception.*;
import com.dev.policy.repository.BasicPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BasicPolicyService {

    private final BasicPolicyRepository repo;

    @CacheEvict(cacheNames = "policies", allEntries = true)
    public BasicPolicyResponse create(BasicPolicyRequest request) {
        if (request.getPolicyName() == null || request.getPolicyName().isBlank())
            throw new MissingRequiredFieldException("policyName");
        if (request.getType() == null)
            throw new MissingRequiredFieldException("type");
        if (request.getBasePremium() == null)
            throw new MissingRequiredFieldException("basePremium");
        if (request.getMaxPremium() == null)
            throw new MissingRequiredFieldException("maxPremium");
        if (request.getBasePremium() > request.getMaxPremium())
            throw new InvalidFieldValueException("basePremium", request.getBasePremium(),
                    "basePremium cannot be greater than maxPremium");

        BasicPolicy policy = BasicPolicy.builder()
                .policyName(request.getPolicyName())
                .type(request.getType())
                .basePremium(request.getBasePremium())
                .maxPremium(request.getMaxPremium())
                .status(PolicyStatus.ACTIVE)
                .build();
        return toResponse(repo.save(policy));
    }

    @CacheEvict(cacheNames = "policies", allEntries = true)
    public BasicPolicyResponse update(Long id, BasicPolicyRequest request) {
        BasicPolicy policy = repo.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id));
        if (request.getBasePremium() != null && request.getMaxPremium() != null
                && request.getBasePremium() > request.getMaxPremium())
            throw new InvalidFieldValueException("basePremium", request.getBasePremium(),
                    "basePremium cannot be greater than maxPremium");

        policy.setPolicyName(request.getPolicyName());
        policy.setType(request.getType());
        policy.setBasePremium(request.getBasePremium());
        policy.setMaxPremium(request.getMaxPremium());
        return toResponse(repo.save(policy));
    }

    @CacheEvict(cacheNames = "policies", allEntries = true)
    public void delete(Long id) {
        repo.findById(id).orElseThrow(() -> new PolicyNotFoundException(id));
        repo.deleteById(id);
    }

    @CacheEvict(cacheNames = "policies", allEntries = true)
    public BasicPolicyResponse updateStatus(Long id, PolicyStatus status) {
        BasicPolicy policy = repo.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id));
        policy.setStatus(status);
        return toResponse(repo.save(policy));
    }

    @Cacheable(cacheNames = "policies", key = "'active-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public RestPage<BasicPolicyResponse> getActive(Pageable pageable) {
        return new RestPage<>(repo.findByStatus(PolicyStatus.ACTIVE, pageable).map(this::toResponse));
    }

    @Cacheable(cacheNames = "policies", key = "'all-' + #status + '-' + #type + '-' + #policyName + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public RestPage<BasicPolicyResponse> getAll(PolicyStatus status, PolicyType type, String policyName, Pageable pageable) {
        Page<BasicPolicyResponse> page;

        boolean hasName = policyName != null && !policyName.isBlank();

        if (status != null && type != null && hasName) {
            page = repo.findByStatusAndTypeAndPolicyNameContainingIgnoreCase(status, type, policyName, pageable).map(this::toResponse);
        } else if (status != null && type != null) {
            page = repo.findByStatusAndType(status, type, pageable).map(this::toResponse);
        } else if (status != null && hasName) {
            page = repo.findByStatusAndPolicyNameContainingIgnoreCase(status, policyName, pageable).map(this::toResponse);
        } else if (type != null && hasName) {
            page = repo.findByTypeAndPolicyNameContainingIgnoreCase(type, policyName, pageable).map(this::toResponse);
        } else if (status != null) {
            page = repo.findByStatus(status, pageable).map(this::toResponse);
        } else if (type != null) {
            page = repo.findByType(type, pageable).map(this::toResponse);
        } else if (hasName) {
            page = repo.findByPolicyNameContainingIgnoreCase(policyName, pageable).map(this::toResponse);
        } else {
            page = repo.findAll(pageable).map(this::toResponse);
        }
        return new RestPage<>(page);
    }

    @Cacheable(cacheNames = "policies", key = "#id")
    public BasicPolicyResponse getById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id)));
    }

    private BasicPolicyResponse toResponse(BasicPolicy p) {
        return BasicPolicyResponse.builder()
                .id(p.getId())
                .policyName(p.getPolicyName())
                .type(p.getType())
                .basePremium(p.getBasePremium())
                .maxPremium(p.getMaxPremium())
                .status(p.getStatus())
                .build();
    }

    public Map<String, Long> getPolicyCounts() {
        return Map.of(
                "total", repo.count(),
                "ACTIVE", repo.countByStatus(PolicyStatus.ACTIVE),
                "INACTIVE", repo.countByStatus(PolicyStatus.INACTIVE),
                "HOME", repo.countByType(PolicyType.HOME),
                "VEHICLE", repo.countByType(PolicyType.VEHICLE));
    }
}