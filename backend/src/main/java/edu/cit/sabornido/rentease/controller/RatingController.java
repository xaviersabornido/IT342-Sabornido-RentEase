package edu.cit.sabornido.rentease.controller;

import edu.cit.sabornido.rentease.dto.ApiResponse;
import edu.cit.sabornido.rentease.dto.rating.OwnerRatingSummaryResponse;
import edu.cit.sabornido.rentease.dto.rating.RatingRequest;
import edu.cit.sabornido.rentease.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @GetMapping("/owners/{ownerId}/summary")
    public ResponseEntity<ApiResponse<OwnerRatingSummaryResponse>> ownerSummary(
            @PathVariable UUID ownerId
    ) {
        return ResponseEntity.ok(ApiResponse.success(ratingService.getOwnerSummary(ownerId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(
            Authentication auth,
            @Valid @RequestBody RatingRequest request
    ) {
        UUID renterId = (UUID) auth.getPrincipal();
        ratingService.create(renterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }
}
