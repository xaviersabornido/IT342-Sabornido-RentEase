package edu.cit.sabornido.rentease.service;

import edu.cit.sabornido.rentease.dto.rating.RatingRequest;
import edu.cit.sabornido.rentease.entity.Rating;
import edu.cit.sabornido.rentease.entity.RentalRequest;
import edu.cit.sabornido.rentease.entity.User;
import edu.cit.sabornido.rentease.exception.AppException;
import edu.cit.sabornido.rentease.repository.RatingRepository;
import edu.cit.sabornido.rentease.repository.RentalRequestRepository;
import edu.cit.sabornido.rentease.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public void create(UUID renterId, RatingRequest req) {
        User renter = userRepository.findById(renterId)
            .orElseThrow(() -> new AppException("DB-001", "User not found", null, HttpStatus.NOT_FOUND));
        if (renter.getRole() != User.UserRole.RENTER) {
            throw new AppException("AUTH-003", "Insufficient permissions", "Only RENTER can submit ratings", HttpStatus.FORBIDDEN);
        }

        if (!userRepository.existsById(req.getOwnerId())) {
            throw new AppException("DB-001", "Resource not found", Map.of("ownerId", req.getOwnerId().toString()), HttpStatus.NOT_FOUND);
        }

        RentalRequest rr = rentalRequestRepository.findApprovedByRenterAndOwner(renterId, req.getOwnerId())
            .orElseThrow(() -> new AppException("BUSINESS-001", "Cannot rate", "You can only rate an owner after an approved rental request for their listing", HttpStatus.BAD_REQUEST));

        if (ratingRepository.existsByRentalRequestId(rr.getId())) {
            throw new AppException("DB-002", "Duplicate entry", "You have already rated this rental", HttpStatus.CONFLICT);
        }

        Rating rating = Rating.builder()
            .ownerId(req.getOwnerId())
            .renterId(renterId)
            .rentalRequestId(rr.getId())
            .ratingValue(req.getRating())
            .comment(req.getComment() != null ? req.getComment().trim() : null)
            .build();

        ratingRepository.save(rating);
    }
}
