package com.dev.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumResponse {
    private Long id;
    private Long policyId;
    private Double amount;
    private LocalDate dueDate;
    private LocalDateTime paidDate;
    private String paymentStatus;
}
