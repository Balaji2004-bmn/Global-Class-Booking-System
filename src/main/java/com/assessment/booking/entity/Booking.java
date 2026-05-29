package com.assessment.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "bookings",
        uniqueConstraints = @UniqueConstraint(name = "uq_bookings_parent_offering", columnNames = {"parent_id", "offering_id"}),
        indexes = {
                @Index(name = "idx_bookings_parent", columnList = "parent_id"),
                @Index(name = "idx_bookings_offering", columnList = "offering_id"),
                @Index(name = "idx_bookings_booked_at", columnList = "booked_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookings_parent"))
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offering_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookings_offering"))
    private Offering offering;

    @Column(name = "booked_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime bookedAt;

    @PrePersist
    void prePersist() {
        if (bookedAt == null) {
            bookedAt = OffsetDateTime.now(ZoneOffset.UTC);
            return;
        }
        bookedAt = bookedAt.withOffsetSameInstant(ZoneOffset.UTC);
    }
}

