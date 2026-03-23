package com.dev.policy.service;

import com.dev.policy.dto.PolicyTypeRequest;
import com.dev.policy.dto.PolicyTypeResponse;
import com.dev.policy.entity.PolicyType;
import com.dev.policy.exception.ResourceNotFoundException;
import com.dev.policy.mapper.PolicyTypeMapper;
import com.dev.policy.repository.PolicyTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PolicyTypeService {

    @Autowired
    private PolicyTypeRepository repository;

    @Autowired
    private PolicyTypeMapper mapper;

    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public PolicyTypeResponse createPolicyType(PolicyTypeRequest request) {
        PolicyType type = mapper.toEntity(request);
        PolicyType savedType = repository.save(type);
        PolicyTypeResponse response = mapper.toResponse(savedType);

        // Notify downstream consumers (e.g. Notification Service) about the new asset
        // type
        rabbitTemplate.convertAndSend(
                com.dev.policy.config.RabbitMQConfig.EXCHANGE_NAME,
                com.dev.policy.config.RabbitMQConfig.ROUTING_KEY_CREATED,
                response);
        return response;
    }

    public PolicyTypeResponse updatePolicyType(Long id, PolicyTypeRequest request) {
        PolicyType existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy Type not found with ID: " + id));
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setBasePremium(request.getBasePremium());
        existing.setCoverageAmount(request.getCoverageAmount());
        existing.setAssetType(request.getAssetType());

        return mapper.toResponse(repository.save(existing));
    }

    public void deletePolicyType(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Policy Type not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    public List<PolicyTypeResponse> getAllPolicyTypes() {
        return repository.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    public PolicyTypeResponse getPolicyTypeById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Policy Type not found with ID: " + id));
    }
}
