package edu.cit.sabornido.rentease.features.ratings;

import edu.cit.sabornido.rentease.core.exception.AppException;
import edu.cit.sabornido.rentease.features.listings.Listing;
import edu.cit.sabornido.rentease.features.listings.ListingRepository;
import edu.cit.sabornido.rentease.features.ratings.dto.OwnerRatingSummaryResponse;
import edu.cit.sabornido.rentease.features.ratings.dto.RatingRequest;
import edu.cit.sabornido.rentease.features.requests.RentalRequest;
import edu.cit.sabornido.rentease.features.requests.RentalRequestRepository;
import edu.cit.sabornido.rentease.features.users.User;
import edu.cit.sabornido.rentease.features.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RentalRequestRepository rentalRequestRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    @Transactional
    public void create(UUID renterId, RatingRequest req) {
        User renter = userRepository.findById(renterId)
            .orElseThrow(() -> new AppException("DB-001", "User not found", null, HttpStatus.NOT_FOUND));
        if (renter.getRole() != User.UserRole.RENTER) {
            throw new AppException("AUTH-003", "Insufficient permissions", "Only RENTER can submit ratings", HttpStatus.FORBIDDEN);
        }

        RentalRequest rr = rentalRequestRepository.findById(req.getRentalRequestId())
            .orElseThrow(() -> new AppException("DB-001", "Resource not found", Map.of("rentalRequestId", req.getRentalRequestId().toString()), HttpStatus.NOT_FOUND));

        if (!rr.getRenterId().equals(renterId)) {
            throw new AppException("AUTH-003", "Insufficient permissions", "You can only rate your own rental requests", HttpStatus.FORBIDDEN);
        }

        if (rr.getStatus() != RentalRequest.RequestStatus.APPROVED) {
            throw new AppException("BUSINESS-001", "Cannot rate", "You can only rate an owner after your request has been approved", HttpStatus.BAD_REQUEST);
        }

        if (ratingRepository.existsByRentalRequestId(rr.getId())) {
            throw new AppException("DB-002", "Duplicate entry", "You have already submitted a rating for this rental", HttpStatus.CONFLICT);
        }

        Listing listing = listingRepository.findById(rr.getListingId())
            .orElseThrow(() -> new AppException("DB-001", "Resource not found", null, HttpStatus.NOT_FOUND));

        UUID ownerId = listing.getOwnerId();

        String comment = req.getComment() != null ? req.getComment().trim() : null;
        if (comment != null && comment.isEmpty()) {
            comment = null;
        }

        Rating rating = Rating.builder()
            .ownerId(ownerId)
            .renterId(renterId)
            .rentalRequestId(rr.getId())
            .ratingValue(req.getRating())
            .comment(comment)
            .responsivenessRating(req.getResponsivenessRating())
            .listingAccuracyRating(req.getListingAccuracyRating())
            .communicationRating(req.getCommunicationRating())
            .fairnessRating(req.getFairnessRating())
            .build();

        ratingRepository.save(rating);
    }

    public OwnerRatingSummaryResponse getOwnerSummary(UUID ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new AppException("DB-001", "Resource not found", Map.of("ownerId", ownerId.toString()), HttpStatus.NOT_FOUND);
        }
        Double avg = ratingRepository.getAverageRatingByOwnerId(ownerId);
        long count = ratingRepository.countByOwnerId(ownerId);
        String memberSince = userRepository.findById(ownerId)
            .map(u -> u.getCreatedAt().toString())
            .orElse(null);
        return OwnerRatingSummaryResponse.builder()
            .averageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : null)
            .reviewCount(count)
            .memberSince(memberSince)
            .build();
    }
}
