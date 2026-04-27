package edu.cit.sabornido.rentease.features.listings;

import edu.cit.sabornido.rentease.features.listings.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByOwnerId(UUID ownerId);

    long countByOwnerId(UUID ownerId);

    List<Listing> findByStatus(Listing.ListingStatus status);
}
