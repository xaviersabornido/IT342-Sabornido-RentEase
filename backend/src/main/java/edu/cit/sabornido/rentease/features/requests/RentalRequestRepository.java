package edu.cit.sabornido.rentease.features.requests;

import edu.cit.sabornido.rentease.features.requests.RentalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, Long> {
    @Query("SELECT rr FROM RentalRequest rr JOIN rr.listing l WHERE rr.renterId = :renterId AND rr.status = 'APPROVED' AND l.ownerId = :ownerId")
    Optional<RentalRequest> findApprovedByRenterAndOwner(UUID renterId, UUID ownerId);
    List<RentalRequest> findByRenterId(UUID renterId);
    List<RentalRequest> findByListingId(Long listingId);
    Optional<RentalRequest> findByListingIdAndRenterId(Long listingId, UUID renterId);
    boolean existsByListingIdAndRenterId(Long listingId, UUID renterId);

    List<RentalRequest> findByListingIdAndStatus(Long listingId, RentalRequest.RequestStatus status);

    @Query("SELECT DISTINCT rr FROM RentalRequest rr JOIN FETCH rr.listing JOIN FETCH rr.renter WHERE rr.renterId = :renterId")
    List<RentalRequest> findAllWithDetailsByRenterId(@Param("renterId") UUID renterId);

    @Query("SELECT DISTINCT rr FROM RentalRequest rr JOIN FETCH rr.listing JOIN FETCH rr.renter WHERE rr.listingId IN :listingIds")
    List<RentalRequest> findAllWithDetailsByListingIdIn(@Param("listingIds") Collection<Long> listingIds);
}
