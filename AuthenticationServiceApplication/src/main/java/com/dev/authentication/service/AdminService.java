package com.dev.authentication.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.dev.authentication.dto.KycAdminResponse;
import com.dev.authentication.dto.UserResponse;
import com.dev.authentication.entity.Kyc;
import com.dev.authentication.entity.KycStatus;
import com.dev.authentication.entity.Role;
import com.dev.authentication.entity.User;
import com.dev.authentication.exception.EntityNotFoundException;
import com.dev.authentication.mapper.KycMapper;
import com.dev.authentication.repository.KycRepository;
import com.dev.authentication.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepo;
    private final KycRepository kycRepo;
    private final KycMapper mapper;

    public Page<UserResponse> getUsers(String email, String name, Role role, Boolean active, int page, int size) {
        return userRepo.findAll(PageRequest.of(page, size)).map(u -> {
            if ((email == null || u.getEmail().contains(email)) &&
                    (name == null || u.getName().contains(name)) &&
                    (role == null || u.getRole() == role) &&
                    (active == null || u.isActive() == active)) {
                return mapper.toUserResponse(u);
            }
            return null;
        }).map(u -> u);
    }

    public UserResponse toggleStatus(Long id, boolean active) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", "id", id));
        user.setActive(active);
        return mapper.toUserResponse(userRepo.save(user));
    }

    public Map<String, Long> getUserCounts() {
        long total = userRepo.count();
        long activeCount = userRepo.countByActive(true);
        return Map.of("total", total, "active", activeCount,
                "suspended", total - activeCount);
    }

    public Page<KycAdminResponse> getKyc(KycStatus status, String email, int page, int size) {
        return kycRepo.findAll(PageRequest.of(page, size))
                .map(mapper::toAdminResponse);
    }

    public KycAdminResponse updateKycStatus(Long id, KycStatus status) {
        Kyc kyc = kycRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("KYC", "id", id));
        kyc.setStatus(status);
        return mapper.toAdminResponse(kycRepo.save(kyc));
    }

    public Map<String, Long> getKycCounts() {
        return Map.of(
                "total", kycRepo.count(),
                "PENDING", kycRepo.countByStatus(KycStatus.PENDING),
                "APPROVED", kycRepo.countByStatus(KycStatus.APPROVED),
                "REJECTED", kycRepo.countByStatus(KycStatus.REJECTED));
    }
}
