package edu.cit.sabornido.rentease.controller;

import edu.cit.sabornido.rentease.dto.ApiResponse;
import edu.cit.sabornido.rentease.dto.listing.ListingRequest;
import edu.cit.sabornido.rentease.dto.listing.ListingResponse;
import edu.cit.sabornido.rentease.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ListingResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(listingService.getAllListings()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ListingResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(listingService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<ListingResponse>> create(
            Authentication auth,
            @Valid @RequestBody ListingRequest request
    ) {
        UUID ownerId = (UUID) auth.getPrincipal();
        ListingResponse res = listingService.create(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<ListingResponse>> update(
            @PathVariable Long id,
            Authentication auth,
            @Valid @RequestBody ListingRequest request
    ) {
        UUID ownerId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(listingService.update(id, ownerId, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication auth) {
        UUID ownerId = (UUID) auth.getPrincipal();
        listingService.delete(id, ownerId);
        return ResponseEntity.noContent().build();
    }
}
