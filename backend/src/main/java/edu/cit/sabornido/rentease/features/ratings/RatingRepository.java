package edu.cit.sabornido.rentease.features.ratings;

import edu.cit.sabornido.rentease.features.ratings.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByRentalRequestId(Long rentalRequestId);
    boolean existsByRentalRequestId(Long rentalRequestId);

    @Query("SELECT AVG(r.ratingValue) FROM Rating r WHERE r.ownerId = :ownerId")
    Double getAverageRatingByOwnerId(UUID ownerId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.ownerId = :ownerId")
    long countByOwnerId(UUID ownerId);

    @Query("SELECT r.rentalRequestId FROM Rating r WHERE r.rentalRequestId IN :ids")
    List<Long> findRatedRentalRequestIdsIn(@Param("ids") Collection<Long> ids);
}
