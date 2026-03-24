package com.dev.claims.dto;

import com.dev.claims.entity.enums.IncidentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class InitiateClaimRequest {

    @NotNull(message = "Policy ID is mandatory")
    private Long policyId;

    @NotNull(message = "Incident type is mandatory")
    private IncidentType incidentType;

    @NotNull(message = "Incident date is mandatory")
    private LocalDate incidentDate;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}
