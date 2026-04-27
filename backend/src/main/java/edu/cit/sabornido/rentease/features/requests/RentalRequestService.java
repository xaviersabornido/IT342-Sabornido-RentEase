package edu.cit.sabornido.rentease.features.requests;

import edu.cit.sabornido.rentease.core.exception.AppException;
import edu.cit.sabornido.rentease.features.listings.Listing;
import edu.cit.sabornido.rentease.features.listings.ListingRepository;
import edu.cit.sabornido.rentease.features.ratings.RatingRepository;
import edu.cit.sabornido.rentease.features.requests.dto.RentalRequestDto;
import edu.cit.sabornido.rentease.features.requests.dto.RentalRequestResponse;
import edu.cit.sabornido.rentease.features.users.User;
import edu.cit.sabornido.rentease.features.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalRequestService {

    private final RentalRequestRepository rentalRequestRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    @Transactional
    public RentalRequestResponse create(UUID renterId, RentalRequestDto dto) {
        User renter = userRepository.findById(renterId)
            .orElseThrow(() -> new AppException("DB-001", "User not found", null, HttpStatus.NOT_FOUND));
        if (renter.getRole() != User.UserRole.RENTER) {
            throw new AppException("AUTH-003", "Insufficient permissions", "Only RENTER can send rental requests", HttpStatus.FORBIDDEN);
        }

        Listing listing = listingRepository.findById(dto.getListingId())
            .orElseThrow(() -> new AppException("DB-001", "Resource not found", Map.of("listingId", dto.getListingId().toString()), HttpStatus.NOT_FOUND));

        if (listing.getOwnerId().equals(renterId)) {
            throw new AppException("BUSINESS-001", "Invalid request", "You cannot send a rental request for your own listing", HttpStatus.BAD_REQUEST);
        }

        if (listing.getStatus() != Listing.ListingStatus.available) {
            throw new AppException("BUSINESS-001", "Listing unavailable", "This property is no longer available for rental requests", HttpStatus.BAD_REQUEST);
        }

        if (rentalRequestRepository.existsByListingIdAndRenterId(dto.getListingId(), renterId)) {
            throw new AppException("DB-002", "Duplicate entry", "You have already requested this listing", HttpStatus.CONFLICT);
        }

        String msg = dto.getMessage() != null ? dto.getMessage().trim() : null;
        if (msg != null && msg.isEmpty()) {
            msg = null;
        }

        RentalRequest rr = RentalRequest.builder()
            .listingId(dto.getListingId())
            .renterId(renterId)
            .status(RentalRequest.RequestStatus.PENDING)
            .preferredStartDate(dto.getPreferredStartDate())
            .leaseDurationMonths(dto.getLeaseDurationMonths())
            .monthlyIncome(dto.getMonthlyIncome())
            .employmentStatus(dto.getEmploymentStatus().trim())
            .message(msg)
            .hasPets(Boolean.TRUE.equals(dto.getHasPets()))
            .smokes(Boolean.TRUE.equals(dto.getSmokes()))
            .creditCheckAgreed(Boolean.TRUE.equals(dto.getCreditCheckAgreed()))
            .build();

        rr = rentalRequestRepository.save(rr);
        rr = rentalRequestRepository.findById(rr.getId()).orElse(rr);
        return toDetail(rr, null, null, null, false);
    }

    public List<RentalRequestResponse> getByRenter(UUID renterId) {
        List<RentalRequest> rows = rentalRequestRepository.findAllWithDetailsByRenterId(renterId);
        Map<Long, Listing> listingById = loadListingsForRequests(rows);
        Set<UUID> ownerIds = rows.stream()
            .map(rr -> {
                Listing l = listingById.get(rr.getListingId());
                return l != null ? l.getOwnerId() : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<UUID, User> ownerById = ownerIds.isEmpty()
            ? Map.of()
            : userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        List<Long> reqIds = rows.stream().map(RentalRequest::getId).toList();
        Set<Long> ratedRequestIds = new HashSet<>();
        if (!reqIds.isEmpty()) {
            ratedRequestIds.addAll(ratingRepository.findRatedRentalRequestIdsIn(reqIds));
        }
        return rows.stream()
            .map(rr -> toDetail(rr, listingById, ownerById, ratedRequestIds, true))
            .collect(Collectors.toList());
    }

    public List<RentalRequestResponse> getByOwner(UUID ownerId) {
        List<Listing> listings = listingRepository.findByOwnerId(ownerId);
        List<Long> listingIds = listings.stream().map(Listing::getId).toList();
        if (listingIds.isEmpty()) {
            return List.of();
        }
        List<RentalRequest> rows = rentalRequestRepository.findAllWithDetailsByListingIdIn(listingIds);
        Map<Long, Listing> listingById = loadListingsForRequests(rows);
        return rows.stream()
            .map(rr -> toDetail(rr, listingById, null, null, false))
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

        if (listing.getStatus() != Listing.ListingStatus.available) {
            throw new AppException("BUSINESS-001", "Listing unavailable", "This listing is already filled or not open for approval", HttpStatus.BAD_REQUEST);
        }

        rr.setStatus(RentalRequest.RequestStatus.APPROVED);
        rr = rentalRequestRepository.save(rr);

        listing.setStatus(Listing.ListingStatus.rented);
        listingRepository.save(listing);

        for (RentalRequest other : rentalRequestRepository.findByListingIdAndStatus(rr.getListingId(), RentalRequest.RequestStatus.PENDING)) {
            if (!other.getId().equals(rr.getId())) {
                other.setStatus(RentalRequest.RequestStatus.DECLINED);
                rentalRequestRepository.save(other);
            }
        }

        rr = rentalRequestRepository.findById(rr.getId()).orElse(rr);
        return toDetail(rr, null, null, null, false);
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
        return toDetail(rr, null, null, null, false);
    }

    /**
     * Loads listing rows from the {@code listings} table so {@code price} is always read from DB
     * (avoids empty price when the JPA association is not hydrated as expected).
     */
    private Map<Long, Listing> loadListingsForRequests(List<RentalRequest> rows) {
        if (rows.isEmpty()) {
            return Map.of();
        }
        Set<Long> ids = new HashSet<>();
        for (RentalRequest rr : rows) {
            if (rr.getListingId() != null) {
                ids.add(rr.getListingId());
            }
        }
        if (ids.isEmpty()) {
            return Map.of();
        }
        return listingRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(Listing::getId, Function.identity()));
    }

    private RentalRequestResponse toDetail(
        RentalRequest rr,
        Map<Long, Listing> listingById,
        Map<UUID, User> ownerById,
        Set<Long> ratedRequestIds,
        boolean renterRatingMeta
    ) {
        Listing listing = resolveListingForDetail(rr, listingById);
        User renter = rr.getRenter();
        RentalRequestResponse.RentalRequestResponseBuilder b = RentalRequestResponse.builder()
            .requestId(rr.getId())
            .status(rr.getStatus().name())
            .listingId(rr.getListingId())
            .listingTitle(listing != null ? listing.getTitle() : null)
            .listingLocation(listing != null ? listing.getLocation() : null)
            .listingMonthlyPrice(listing != null ? listing.getPrice() : null)
            .preferredStartDate(rr.getPreferredStartDate())
            .leaseDurationMonths(rr.getLeaseDurationMonths())
            .monthlyIncome(rr.getMonthlyIncome())
            .employmentStatus(rr.getEmploymentStatus())
            .message(rr.getMessage())
            .hasPets(rr.getHasPets())
            .smokes(rr.getSmokes())
            .creditCheckAgreed(rr.getCreditCheckAgreed())
            .renterEmail(renter != null ? renter.getEmail() : null)
            .renterFirstname(renter != null ? renter.getFirstname() : null)
            .renterLastname(renter != null ? renter.getLastname() : null);

        if (renterRatingMeta && listing != null) {
            UUID oid = listing.getOwnerId();
            b.ownerId(oid);
            User ou = null;
            if (ownerById != null) {
                ou = ownerById.get(oid);
            }
            if (ou == null) {
                ou = userRepository.findById(oid).orElse(null);
            }
            b.ownerFirstname(ou != null ? ou.getFirstname() : null)
                .ownerLastname(ou != null ? ou.getLastname() : null)
                .ratingSubmitted(ratedRequestIds != null && ratedRequestIds.contains(rr.getId()));
        }

        return b.build();
    }

    private Listing resolveListingForDetail(RentalRequest rr, Map<Long, Listing> listingById) {
        Long lid = rr.getListingId();
        if (lid != null && listingById != null) {
            Listing fromMap = listingById.get(lid);
            if (fromMap != null) {
                return fromMap;
            }
        }
        if (lid != null) {
            return listingRepository.findById(lid).orElse(rr.getListing());
        }
        return rr.getListing();
    }
}
