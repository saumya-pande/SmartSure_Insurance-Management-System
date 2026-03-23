package com.dev.policy.service;

import com.dev.policy.dto.PolicyResponse;
import com.dev.policy.dto.PurchasePolicyRequest;
import com.dev.policy.entity.HomeDetails;
import com.dev.policy.entity.Policy;
import com.dev.policy.entity.PolicyType;
import com.dev.policy.entity.Premium;
import com.dev.policy.entity.VehicleDetails;
import com.dev.policy.entity.enums.AssetType;
import com.dev.policy.entity.enums.HomeType;
import com.dev.policy.entity.enums.PolicyStatus;
import com.dev.policy.exception.ConflictException;
import com.dev.policy.exception.ResourceNotFoundException;
import com.dev.policy.mapper.HomeDetailsMapper;
import com.dev.policy.mapper.PolicyMapper;
import com.dev.policy.mapper.VehicleDetailsMapper;
import com.dev.policy.repository.HomeDetailsRepository;
import com.dev.policy.repository.PolicyRepository;
import com.dev.policy.repository.PolicyTypeRepository;
import com.dev.policy.repository.PremiumRepository;
import com.dev.policy.repository.VehicleDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Service
@Transactional
public class PolicyPurchaseService {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyTypeRepository policyTypeRepository;

    @Autowired
    private HomeDetailsRepository homeDetailsRepository;

    @Autowired
    private VehicleDetailsRepository vehicleDetailsRepository;

    @Autowired
    private PremiumRepository premiumRepository;

    @Autowired
    private PolicyMapper policyMapper;

    @Autowired
    private HomeDetailsMapper homeDetailsMapper;

    @Autowired
    private VehicleDetailsMapper vehicleDetailsMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public PolicyResponse purchasePolicy(PurchasePolicyRequest request, Long userId, String email) {
        PolicyType type = policyTypeRepository.findById(request.getPolicyTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy Type not found: " + request.getPolicyTypeId()));

        Policy policy = Policy.builder()
                .userId(userId)
                .policyType(type)
                .status(PolicyStatus.CREATED)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .build();

        policy = policyRepository.save(policy);

        if (type.getAssetType() == AssetType.HOME) {
            if (request.getHomeDetails() == null) {
                throw new IllegalArgumentException("Home Details are required for HOME policy");
            }
            HomeDetails home = homeDetailsMapper.toEntity(request.getHomeDetails());
            home.setPolicy(policy);
            
            // Conditional validation and sanitization
            if (home.getHomeType() == HomeType.FLAT) {
                if (homeDetailsRepository.existsByUnitNumberAndSocietyNameAndPincode(
                        home.getUnitNumber(), home.getSocietyName(), home.getPincode())) {
                    throw new ConflictException("Unit " + home.getUnitNumber() + " in this society is already covered under an active policy");
                }
                home.setPropertyId(null); // Clear Villa fields
            } else if (home.getHomeType() == HomeType.VILLA) {
                if (homeDetailsRepository.existsByPropertyId(home.getPropertyId())) {
                    throw new ConflictException("This property ID is already covered under an active policy");
                }
                home.setUnitNumber(null); // Clear Flat fields
                home.setSocietyName(null);
            }
            homeDetailsRepository.save(home);

        } else if (type.getAssetType() == AssetType.VEHICLE) {
            if (request.getVehicleDetails() == null) {
                throw new IllegalArgumentException("Vehicle Details are required for VEHICLE policy");
            }
            VehicleDetails vehicle = vehicleDetailsMapper.toEntity(request.getVehicleDetails());
            vehicle.setPolicy(policy);

            if (vehicleDetailsRepository.existsByVehicleNumber(vehicle.getVehicleNumber())) {
                throw new ConflictException("Vehicle with number " + vehicle.getVehicleNumber() + " is already covered");
            }
            vehicleDetailsRepository.save(vehicle);
        }

        // Calculate Premium
        createPremium(policy, type.getBasePremium());

        policy.setStatus(PolicyStatus.ACTIVE);
        return policyMapper.toResponse(policyRepository.save(policy));
    }

    private void createPremium(Policy policy, Double amount) {
        Premium premium = Premium.builder()
                .policy(policy)
                .amount(amount)
                .dueDate(LocalDate.now().plusDays(30))
                .paymentStatus("PENDING")
                .build();
        premiumRepository.save(premium);
    }

    public List<PolicyResponse> getMyPolicies(Long userId) {
        return policyRepository.findByUserId(userId).stream()
                .map(policyMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PolicyResponse getPolicyById(Long id) {
        return policyRepository.findById(id)
                .map(policyMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found: " + id));
    }
}
