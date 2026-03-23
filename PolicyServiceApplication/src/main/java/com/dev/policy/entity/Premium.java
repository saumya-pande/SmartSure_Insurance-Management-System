package com.dev.policy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "premiums")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Premium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false, name = "amount")
    private Double amount;

    @Column(nullable = false, name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDateTime paidDate;

    @Column(nullable = false)
    private String paymentStatus; // PENDING, PAID, FAILED
}
