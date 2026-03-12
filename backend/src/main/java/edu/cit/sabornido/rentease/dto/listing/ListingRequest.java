package edu.cit.sabornido.rentease.dto.listing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ListingRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0", message = "Price must be >= 0")
    private BigDecimal price;

    @NotBlank(message = "Location is required")
    private String location;

    private String description;

    // Optional: comma-separated list, e.g. "WiFi, Air conditioning"
    private String amenities;

    // Optional: comma-separated URLs or JSON string
    private String imageUrls;

    @NotBlank(message = "Property type is required")
    private String propertyType;

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
