package edu.cit.sabornido.rentease.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalRequestResponse {
    private Long requestId;
    private String status;
    private Long listingId;
    private String listingTitle;
    private String listingLocation;
    /** Monthly rent for the listing (for owner summary / "Total" column). */
    @JsonAlias({"listing_monthly_price"})
    private BigDecimal listingMonthlyPrice;
    private LocalDate preferredStartDate;
    private Integer leaseDurationMonths;
    private BigDecimal monthlyIncome;
    private String employmentStatus;
    private String message;
    private Boolean hasPets;
    private Boolean smokes;
    private Boolean creditCheckAgreed;
    private String renterEmail;
    private String renterFirstname;
    private String renterLastname;

    /** Populated for renter-facing list: listing owner (for rate-owner flow). */
    private UUID ownerId;
    private String ownerFirstname;
    private String ownerLastname;
    /** True if this renter already submitted a rating for this request. */
    @JsonAlias({"rating_submitted"})
    private Boolean ratingSubmitted;
}
