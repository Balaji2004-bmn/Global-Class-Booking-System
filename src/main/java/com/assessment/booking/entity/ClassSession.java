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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "sessions", indexes = {
        @Index(name = "idx_sessions_offering", columnList = "offering_id"),
        @Index(name = "idx_sessions_time_range", columnList = "start_time_utc, end_time_utc")
})
@Check(constraints = "end_time_utc > start_time_utc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offering_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sessions_offering"))
    private Offering offering;

    @Column(name = "start_time_utc", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime startTimeUtc;

    @Column(name = "end_time_utc", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime endTimeUtc;

    @PrePersist
    @PreUpdate
    void normalizeAndValidate() {
        if (startTimeUtc == null || endTimeUtc == null) {
            return;
        }

        startTimeUtc = startTimeUtc.withOffsetSameInstant(ZoneOffset.UTC);
        endTimeUtc = endTimeUtc.withOffsetSameInstant(ZoneOffset.UTC);

        if (!startTimeUtc.isBefore(endTimeUtc)) {
            throw new IllegalArgumentException("Session end time must be after start time");
        }
    }
}

