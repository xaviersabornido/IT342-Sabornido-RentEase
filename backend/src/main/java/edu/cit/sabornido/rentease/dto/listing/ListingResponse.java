package edu.cit.sabornido.rentease.dto.listing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    private String propertyType;
    private String status;
    private Double ownerRating;
}
