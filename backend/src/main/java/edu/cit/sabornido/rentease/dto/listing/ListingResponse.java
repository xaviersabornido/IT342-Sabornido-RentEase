package edu.cit.sabornido.rentease.dto.listing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingResponse {
    private Long id;
    private String title;
    private BigDecimal price;
    private String location;
    private String description;
    private String amenities;
    private String imageUrls;
    private String propertyType;
    private String status;
    private Double ownerRating;
    /** Owner display (for property details sidebar). */
    private String ownerFirstname;
    private String ownerLastname;
    private Integer ownerListingCount;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer areaSqFt;
    private Integer parkingSpaces;
    private LocalDate availableFrom;
    private Integer leaseTermMonths;
    private BigDecimal deposit;
    private Boolean furnished;
    private Boolean petsAllowed;
    private BigDecimal utilitiesEstimate;
}
