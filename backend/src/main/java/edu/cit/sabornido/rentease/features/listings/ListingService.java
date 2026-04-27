package edu.cit.sabornido.rentease.features.listings;

import edu.cit.sabornido.rentease.core.exception.AppException;
import edu.cit.sabornido.rentease.features.listings.dto.ListingRequest;
import edu.cit.sabornido.rentease.features.listings.dto.ListingResponse;
import edu.cit.sabornido.rentease.features.ratings.RatingRepository;
import edu.cit.sabornido.rentease.features.requests.RentalRequest;
import edu.cit.sabornido.rentease.features.requests.RentalRequestRepository;
import edu.cit.sabornido.rentease.features.users.User;
import edu.cit.sabornido.rentease.features.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    private final RentalRequestRepository rentalRequestRepository;

    public List<ListingResponse> getAllListings() {
        return listingRepository.findByStatus(Listing.ListingStatus.available).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<ListingResponse> getListingsForOwner(UUID ownerId) {
        return listingRepository.findByOwnerId(ownerId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Public listing detail: only {@code available} listings, unless the viewer is the owner
     * (e.g. to edit or review a rented unit from My Listings).
     */
    public ListingResponse getByIdForViewer(Long id, Authentication authentication) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new AppException("DB-001", "Resource not found", Map.of("id", id.toString()), HttpStatus.NOT_FOUND));

        if (listing.getStatus() != Listing.ListingStatus.available) {
            UUID viewerId = resolveUserId(authentication);
            if (viewerId == null || !listing.getOwnerId().equals(viewerId)) {
                throw new AppException("DB-001", "Resource not found", Map.of("id", id.toString()), HttpStatus.NOT_FOUND);
            }
        }
        return toResponse(listing);
    }

    private static UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID uuid) {
            return uuid;
        }
        return null;
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
            .amenities(req.getAmenities() != null ? req.getAmenities().trim() : null)
            .imageUrls(req.getImageUrls() != null ? req.getImageUrls().trim() : null)
            .price(req.getPrice())
            .location(req.getLocation().trim())
            .propertyType(req.getPropertyType().trim())
            .bedrooms(req.getBedrooms())
            .bathrooms(req.getBathrooms())
            .areaSqFt(req.getAreaSqFt())
            .parkingSpaces(req.getParkingSpaces())
            .availableFrom(req.getAvailableFrom())
            .leaseTermMonths(req.getLeaseTermMonths())
            .deposit(req.getDeposit())
            .furnished(req.getFurnished())
            .petsAllowed(req.getPetsAllowed())
            .utilitiesEstimate(req.getUtilitiesEstimate())
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
        listing.setAmenities(req.getAmenities() != null ? req.getAmenities().trim() : null);
        listing.setImageUrls(req.getImageUrls() != null ? req.getImageUrls().trim() : null);
        listing.setPrice(req.getPrice());
        listing.setLocation(req.getLocation().trim());
        listing.setPropertyType(req.getPropertyType().trim());
        listing.setBedrooms(req.getBedrooms());
        listing.setBathrooms(req.getBathrooms());
        listing.setAreaSqFt(req.getAreaSqFt());
        listing.setParkingSpaces(req.getParkingSpaces());
        listing.setAvailableFrom(req.getAvailableFrom());
        listing.setLeaseTermMonths(req.getLeaseTermMonths());
        listing.setDeposit(req.getDeposit());
        listing.setFurnished(req.getFurnished());
        listing.setPetsAllowed(req.getPetsAllowed());
        listing.setUtilitiesEstimate(req.getUtilitiesEstimate());
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

        List<RentalRequest> requests = rentalRequestRepository.findByListingId(id);
        for (RentalRequest rr : requests) {
            ratingRepository.findByRentalRequestId(rr.getId()).ifPresent(ratingRepository::delete);
            rentalRequestRepository.delete(rr);
        }

        listingRepository.delete(listing);
    }

    private ListingResponse toResponse(Listing listing) {
        Double ownerRating = ratingRepository.getAverageRatingByOwnerId(listing.getOwnerId());
        User ownerUser = userRepository.findById(listing.getOwnerId()).orElse(null);
        long listingCount = listingRepository.countByOwnerId(listing.getOwnerId());
        int ownerListingCount = (int) Math.min(listingCount, Integer.MAX_VALUE);
        return ListingResponse.builder()
            .id(listing.getId())
            .title(listing.getTitle())
            .price(listing.getPrice())
            .location(listing.getLocation())
            .description(listing.getDescription())
            .amenities(listing.getAmenities())
            .imageUrls(listing.getImageUrls())
            .propertyType(listing.getPropertyType())
            .status(listing.getStatus().name())
            .ownerRating(ownerRating != null ? Math.round(ownerRating * 10.0) / 10.0 : null)
            .ownerFirstname(ownerUser != null ? ownerUser.getFirstname() : null)
            .ownerLastname(ownerUser != null ? ownerUser.getLastname() : null)
            .ownerListingCount(ownerListingCount > 0 ? ownerListingCount : null)
            .bedrooms(listing.getBedrooms())
            .bathrooms(listing.getBathrooms())
            .areaSqFt(listing.getAreaSqFt())
            .parkingSpaces(listing.getParkingSpaces())
            .availableFrom(listing.getAvailableFrom())
            .leaseTermMonths(listing.getLeaseTermMonths())
            .deposit(listing.getDeposit())
            .furnished(listing.getFurnished())
            .petsAllowed(listing.getPetsAllowed())
            .utilitiesEstimate(listing.getUtilitiesEstimate())
            .build();
    }
}
