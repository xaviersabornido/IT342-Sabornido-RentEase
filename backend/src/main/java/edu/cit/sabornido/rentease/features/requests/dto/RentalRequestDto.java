package edu.cit.sabornido.rentease.features.requests.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RentalRequestDto {
    @NotNull(message = "listingId is required")
    private Long listingId;

    @NotNull(message = "Preferred start date is required")
    private LocalDate preferredStartDate;

    private Integer leaseDurationMonths;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0", inclusive = false, message = "Monthly income must be greater than 0")
    private BigDecimal monthlyIncome;

    @NotBlank(message = "Employment status is required")
    @Size(max = 120)
    private String employmentStatus;

    @Size(max = 500, message = "Message must be at most 500 characters")
    private String message;

    private Boolean hasPets;
    private Boolean smokes;

    @NotNull(message = "Credit check agreement is required")
    @AssertTrue(message = "You must agree to a credit check")
    private Boolean creditCheckAgreed;
}
