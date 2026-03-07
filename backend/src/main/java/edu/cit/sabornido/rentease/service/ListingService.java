package edu.cit.sabornido.rentease.service;

import edu.cit.sabornido.rentease.dto.listing.ListingRequest;
import edu.cit.sabornido.rentease.dto.listing.ListingResponse;
import edu.cit.sabornido.rentease.entity.Listing;
import edu.cit.sabornido.rentease.entity.User;
import edu.cit.sabornido.rentease.exception.AppException;
import edu.cit.sabornido.rentease.repository.ListingRepository;
import edu.cit.sabornido.rentease.repository.RatingRepository;
import edu.cit.sabornido.rentease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    public List<ListingResponse> getAllListings() {
        return listingRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ListingResponse getById(Long id) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new AppException("DB-001", "Resource not found", Map.of("id", id.toString()), HttpStatus.NOT_FOUND));
        return toResponse(listing);
    }

    @Transactional
    public ListingResponse create(UUID ownerId, ListingRequest req) {
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new AppException("DB-001", "User not found", null, HttpStatus.NOT_FOUND));
        if (owner.getRole() != User.UserRole.OWNER) {
            throw new AppException("AUTH-003", "Insufficient permissions", "Only OWNER can create listings", HttpStatus.FORBIDDEN);
        }

        Listing listing = Listing.builder()
            .ownerId(ownerId)
            .title(req.getTitle().trim())
            .description(req.getDescription() != null ? req.getDescription().trim() : null)
            .price(req.getPrice())
            .location(req.getLocation().trim())
            .propertyType(req.getPropertyType().trim())
            .status(Listing.ListingStatus.available)
            .build();

        listing = listingRepository.save(listing);
        return toResponse(listing);
    }

    @Transactional
    public ListingResponse update(Long id, UUID ownerId, ListingRequest req) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new AppException("DB-001", "Resource not found", Map.of("id", id.toString()), HttpStatus.NOT_FOUND));
        if (!listing.getOwnerId().equals(ownerId)) {
            throw new AppException("AUTH-003", "Insufficient permissions", "You can only edit your own listings", HttpStatus.FORBIDDEN);
        }

        listing.setTitle(req.getTitle().trim());
        listing.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
        listing.setPrice(req.getPrice());
        listing.setLocation(req.getLocation().trim());
        listing.setPropertyType(req.getPropertyType().trim());
        listing = listingRepository.save(listing);
        return toResponse(listing);
    }

    @Transactional
    public void delete(Long id, UUID ownerId) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new AppException("DB-001", "Resource not found", Map.of("id", id.toString()), HttpStatus.NOT_FOUND));
        if (!listing.getOwnerId().equals(ownerId)) {
            throw new AppException("AUTH-003", "Insufficient permissions", "You can only delete your own listings", HttpStatus.FORBIDDEN);
        }
        listingRepository.delete(listing);
    }

    private ListingResponse toResponse(Listing listing) {
        Double ownerRating = ratingRepository.getAverageRatingByOwnerId(listing.getOwnerId());
        return ListingResponse.builder()
            .id(listing.getId())
            .title(listing.getTitle())
            .price(listing.getPrice())
            .location(listing.getLocation())
            .description(listing.getDescription())
            .propertyType(listing.getPropertyType())
            .status(listing.getStatus().name())
            .ownerRating(ownerRating != null ? Math.round(ownerRating * 10.0) / 10.0 : null)
            .build();
    }
}
