package edu.cit.sabornido.rentease.controller;

import edu.cit.sabornido.rentease.dto.ApiResponse;
import edu.cit.sabornido.rentease.dto.request.RentalRequestDto;
import edu.cit.sabornido.rentease.dto.request.RentalRequestResponse;
import edu.cit.sabornido.rentease.service.RentalRequestService;
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
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class RentalRequestController {

    private final RentalRequestService rentalRequestService;

    @PostMapping
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<ApiResponse<RentalRequestResponse>> create(
            Authentication auth,
            @Valid @RequestBody RentalRequestDto dto
    ) {
        UUID renterId = (UUID) auth.getPrincipal();
        RentalRequestResponse res = rentalRequestService.create(renterId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<ApiResponse<List<RentalRequestResponse>>> getByUser(Authentication auth) {
        UUID renterId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(rentalRequestService.getByRenter(renterId)));
    }

    @GetMapping("/owner")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<RentalRequestResponse>>> getByOwner(Authentication auth) {
        UUID ownerId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(rentalRequestService.getByOwner(ownerId)));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<RentalRequestResponse>> approve(
            @PathVariable Long id,
            Authentication auth
    ) {
        UUID ownerId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(rentalRequestService.approve(id, ownerId)));
    }

    @PutMapping("/{id}/decline")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<RentalRequestResponse>> decline(
            @PathVariable Long id,
            Authentication auth
    ) {
        UUID ownerId = (UUID) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(rentalRequestService.decline(id, ownerId)));
    }
}
