package edu.cit.sabornido.rentease.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RentalRequestDto {
    @NotNull(message = "listingId is required")
    private Long listingId;
}
