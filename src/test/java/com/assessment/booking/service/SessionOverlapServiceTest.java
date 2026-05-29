package com.assessment.booking.service;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SessionOverlapServiceTest {

    private final SessionOverlapService service = new SessionOverlapService();

    @Test
    void detectsOverlapWhenExistingStartsBeforeRequestedEndsAndEndsAfterRequestedStarts() {
        SessionOverlapService.SessionWindow existing = window(1L, "2026-06-01T10:00:00Z", "2026-06-01T11:00:00Z");
        SessionOverlapService.SessionWindow requested = window(2L, "2026-06-01T10:30:00Z", "2026-06-01T11:30:00Z");

        assertThat(service.overlaps(existing, requested)).isTrue();
    }

    @Test
    void doesNotDetectOverlapWhenSessionsTouchAtBoundary() {
        SessionOverlapService.SessionWindow existing = window(1L, "2026-06-01T10:00:00Z", "2026-06-01T11:00:00Z");
        SessionOverlapService.SessionWindow requested = window(2L, "2026-06-01T11:00:00Z", "2026-06-01T12:00:00Z");

        assertThat(service.overlaps(existing, requested)).isFalse();
    }

    @Test
    void findsFirstConflictAcrossMultipleSessions() {
        List<SessionOverlapService.SessionWindow> existing = List.of(
                window(1L, "2026-06-01T08:00:00Z", "2026-06-01T09:00:00Z"),
                window(2L, "2026-06-01T12:00:00Z", "2026-06-01T13:00:00Z")
        );
        List<SessionOverlapService.SessionWindow> requested = List.of(
                window(3L, "2026-06-01T09:00:00Z", "2026-06-01T10:00:00Z"),
                window(4L, "2026-06-01T12:30:00Z", "2026-06-01T13:30:00Z")
        );

        assertThat(service.findFirstConflict(existing, requested))
                .isPresent()
                .get()
                .satisfies(conflict -> {
                    assertThat(conflict.existingSession().sessionId()).isEqualTo(2L);
                    assertThat(conflict.requestedSession().sessionId()).isEqualTo(4L);
                });
    }

    @Test
    void returnsEmptyWhenNoSessionsOverlap() {
        List<SessionOverlapService.SessionWindow> existing = List.of(
                window(1L, "2026-06-01T08:00:00Z", "2026-06-01T09:00:00Z")
        );
        List<SessionOverlapService.SessionWindow> requested = List.of(
                window(2L, "2026-06-01T09:00:00Z", "2026-06-01T10:00:00Z")
        );

        assertThat(service.findFirstConflict(existing, requested)).isEmpty();
    }

    private SessionOverlapService.SessionWindow window(Long id, String start, String end) {
        return new SessionOverlapService.SessionWindow(
                id,
                OffsetDateTime.parse(start),
                OffsetDateTime.parse(end)
        );
    }
}

