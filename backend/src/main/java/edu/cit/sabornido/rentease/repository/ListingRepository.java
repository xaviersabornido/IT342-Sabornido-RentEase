package edu.cit.sabornido.rentease.repository;

import edu.cit.sabornido.rentease.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByOwnerId(UUID ownerId);

    long countByOwnerId(UUID ownerId);

    List<Listing> findByStatus(Listing.ListingStatus status);
}
