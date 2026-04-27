package edu.cit.sabornido.rentease.features.requests;

import edu.cit.sabornido.rentease.features.listings.Listing;
import edu.cit.sabornido.rentease.features.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "rental_requests", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"listing_id", "renter_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;

    @Column(name = "renter_id", nullable = false)
    private UUID renterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", insertable = false, updatable = false)
    private User renter;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Column(name = "preferred_start_date")
    private LocalDate preferredStartDate;

    @Column(name = "lease_duration_months")
    private Integer leaseDurationMonths;

    @Column(name = "monthly_income", precision = 14, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "employment_status", length = 120)
    private String employmentStatus;

    @Column(length = 500)
    private String message;

    @Column(name = "has_pets")
    private Boolean hasPets;

    @Column(name = "smokes")
    private Boolean smokes;

    @Column(name = "credit_check_agreed")
    private Boolean creditCheckAgreed;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (requestedAt == null) requestedAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public enum RequestStatus {
        PENDING, APPROVED, DECLINED
    }
}
