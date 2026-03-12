package edu.cit.sabornido.rentease.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    private User owner;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "amenities", columnDefinition = "TEXT")
    private String amenities;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String location;

    @Column(name = "property_type", nullable = false)
    private String propertyType;

    @Column(name = "bedrooms")
    private Integer bedrooms;

    @Column(name = "bathrooms")
    private Integer bathrooms;

    @Column(name = "area_sq_ft")
    private Integer areaSqFt;

    @Column(name = "parking_spaces")
    private Integer parkingSpaces;

    @Column(name = "available_from")
    private LocalDate availableFrom;

    @Column(name = "lease_term_months")
    private Integer leaseTermMonths;

    @Column(name = "deposit", precision = 12, scale = 2)
    private BigDecimal deposit;

    @Column(name = "furnished")
    private Boolean furnished;

    @Column(name = "pets_allowed")
    private Boolean petsAllowed;

    @Column(name = "utilities_estimate", precision = 12, scale = 2)
    private BigDecimal utilitiesEstimate;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ListingStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public enum ListingStatus {
        available, rented, pending
    }
}
