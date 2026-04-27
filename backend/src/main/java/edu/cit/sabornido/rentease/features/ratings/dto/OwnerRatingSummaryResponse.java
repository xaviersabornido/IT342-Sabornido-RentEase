package edu.cit.sabornido.rentease.features.ratings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerRatingSummaryResponse {
    private Double averageRating;
    private long reviewCount;
    /** ISO-8601 instant string when the owner joined. */
    private String memberSince;
}
