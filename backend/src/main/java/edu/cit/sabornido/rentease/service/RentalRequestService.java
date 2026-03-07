package edu.cit.sabornido.rentease.service;

import edu.cit.sabornido.rentease.dto.request.RentalRequestResponse;
import edu.cit.sabornido.rentease.entity.Listing;
import edu.cit.sabornido.rentease.entity.RentalRequest;
import edu.cit.sabornido.rentease.entity.User;
import edu.cit.sabornido.rentease.exception.AppException;
import edu.cit.sabornido.rentease.repository.ListingRepository;
import edu.cit.sabornido.rentease.repository.RentalRequestRepository;
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
public class RentalRequestService {

    private final RentalRequestRepository rentalRequestRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    @Transactional
    public RentalRequestResponse create(UUID renterId, Long listingId) {
        User renter = userRepository.findById(renterId)
            .orElseThrow(() -> new AppException("DB-001", "User not found", null, HttpStatus.NOT_FOUND));
        if (renter.getRole() != User.UserRole.RENTER) {
            throw new AppException("AUTH-003", "Insufficient permissions", "Only RENTER can send rental requests", HttpStatus.FORBIDDEN);
        }

        if (!listingRepository.existsById(listingId)) {
            throw new AppException("DB-001", "Resource not found", Map.of("listingId", listingId.toString()), HttpStatus.NOT_FOUND);
        }

        if (rentalRequestRepository.existsByListingIdAndRenterId(listingId, renterId)) {
            throw new AppException("DB-002", "Duplicate entry", "You have already requested this listing", HttpStatus.CONFLICT);
        }

        RentalRequest rr = RentalRequest.builder()
            .listingId(listingId)
            .renterId(renterId)
            .status(RentalRequest.RequestStatus.PENDING)
            .build();

        rr = rentalRequestRepository.save(rr);
        return RentalRequestResponse.builder()
            .requestId(rr.getId())
            .status(rr.getStatus().name())
            .build();
    }

    public List<RentalRequestResponse> getByRenter(UUID renterId) {
        return rentalRequestRepository.findByRenterId(renterId).stream()
            .map(rr -> RentalRequestResponse.builder().requestId(rr.getId()).status(rr.getStatus().name()).build())
            .collect(Collectors.toList());
    }

    public List<RentalRequestResponse> getByOwner(UUID ownerId) {
        List<Listing> listings = listingRepository.findByOwnerId(ownerId);
        List<Long> listingIds = listings.stream().map(Listing::getId).toList();
        return rentalRequestRepository.findAll().stream()
            .filter(rr -> listingIds.contains(rr.getListingId()))
            .map(rr -> RentalRequestResponse.builder().requestId(rr.getId()).status(rr.getStatus().name()).build())
            .collect(Collectors.toList());
    }

    @Transactional
    public RentalRequestResponse approve(Long id, UUID ownerId) {
        RentalRequest rr = rentalRequestRepository.findById(id)
            .orElseThrow(() -> new AppException("DB-001", "Resource not found", Map.of("id", id.toString()), HttpStatus.NOT_FOUND));

        Listing listing = listingRepository.findById(rr.getListingId())
            .orElseThrow(() -> new AppException("DB-001", "Resource not found", null, HttpStatus.NOT_FOUND));
        if (!listing.getOwnerId().equals(ownerId)) {
            throw new AppException("AUTH-003", "Insufficient permissions", "You can only approve requests for your listings", HttpStatus.FORBIDDEN);
        }

        rr.setStatus(RentalRequest.RequestStatus.APPROVED);
        rr = rentalRequestRepository.save(rr);
        return RentalRequestResponse.builder().requestId(rr.getId()).status(rr.getStatus().name()).build();
    }

    @Transactional
    public RentalRequestResponse decline(Long id, UUID ownerId) {
        RentalRequest rr = rentalRequestRepository.findById(id)
            .orElseThrow(() -> new AppException("DB-001", "Resource not found", Map.of("id", id.toString()), HttpStatus.NOT_FOUND));

        Listing listing = listingRepository.findById(rr.getListingId())
            .orElseThrow(() -> new AppException("DB-001", "Resource not found", null, HttpStatus.NOT_FOUND));
        if (!listing.getOwnerId().equals(ownerId)) {
            throw new AppException("AUTH-003", "Insufficient permissions", "You can only decline requests for your listings", HttpStatus.FORBIDDEN);
        }

        rr.setStatus(RentalRequest.RequestStatus.DECLINED);
        rr = rentalRequestRepository.save(rr);
        return RentalRequestResponse.builder().requestId(rr.getId()).status(rr.getStatus().name()).build();
    }
}
