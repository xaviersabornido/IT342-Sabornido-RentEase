package edu.cit.sabornido.rentease.features.ratings;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ratings", uniqueConstraints = {
    @UniqueConstraint(columnNames = "rental_request_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "renter_id", nullable = false)
    private UUID renterId;

    @Column(name = "rental_request_id", nullable = false, unique = true)
    private Long rentalRequestId;

    @Column(name = "rating_value", nullable = false)
    private Integer ratingValue;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "responsiveness_rating")
    private Integer responsivenessRating;

    @Column(name = "listing_accuracy_rating")
    private Integer listingAccuracyRating;

    @Column(name = "communication_rating")
    private Integer communicationRating;

    @Column(name = "fairness_rating")
    private Integer fairnessRating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
