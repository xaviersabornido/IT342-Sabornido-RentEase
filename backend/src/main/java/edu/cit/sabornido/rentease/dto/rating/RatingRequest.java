package edu.cit.sabornido.rentease.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RatingRequest {
    @NotNull(message = "rentalRequestId is required")
    private Long rentalRequestId;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @Size(max = 2000, message = "Comment must be at most 2000 characters")
    private String comment;

    @Min(value = 1, message = "Aspect ratings must be between 1 and 5")
    @Max(value = 5, message = "Aspect ratings must be between 1 and 5")
    private Integer responsivenessRating;

    @Min(value = 1, message = "Aspect ratings must be between 1 and 5")
    @Max(value = 5, message = "Aspect ratings must be between 1 and 5")
    private Integer listingAccuracyRating;

    @Min(value = 1, message = "Aspect ratings must be between 1 and 5")
    @Max(value = 5, message = "Aspect ratings must be between 1 and 5")
    private Integer communicationRating;

    @Min(value = 1, message = "Aspect ratings must be between 1 and 5")
    @Max(value = 5, message = "Aspect ratings must be between 1 and 5")
    private Integer fairnessRating;
}
