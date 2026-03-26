package com.dev.policy.service;

import com.dev.policy.dto.*;
import com.dev.policy.entity.*;
import com.dev.policy.exception.*;
import com.dev.policy.repository.BasicPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BasicPolicyService {

    private final BasicPolicyRepository repo;

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

    public void delete(Long id) {
        repo.findById(id).orElseThrow(() -> new PolicyNotFoundException(id));
        repo.deleteById(id);
    }

    public BasicPolicyResponse updateStatus(Long id, PolicyStatus status) {
        BasicPolicy policy = repo.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id));
        policy.setStatus(status);
        return toResponse(repo.save(policy));
    }

    public Page<BasicPolicyResponse> getActive(Pageable pageable) {
        return repo.findByStatus(PolicyStatus.ACTIVE, pageable).map(this::toResponse);
    }

    public Page<BasicPolicyResponse> getAll(Pageable pageable) {
        return repo.findAll(pageable).map(this::toResponse);
    }

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
            "total",    repo.count(),
            "ACTIVE",   repo.countByStatus(PolicyStatus.ACTIVE),
            "INACTIVE", repo.countByStatus(PolicyStatus.INACTIVE)
        );
    }
}