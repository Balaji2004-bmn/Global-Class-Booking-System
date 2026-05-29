package com.assessment.booking.service;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SessionOverlapService {

    public boolean overlaps(SessionWindow existing, SessionWindow requested) {
        return existing.start().isBefore(requested.end()) && existing.end().isAfter(requested.start());
    }

    public Optional<SessionConflict> findFirstConflict(List<SessionWindow> existingSessions, List<SessionWindow> requestedSessions) {
        for (SessionWindow existing : existingSessions) {
            for (SessionWindow requested : requestedSessions) {
                if (overlaps(existing, requested)) {
                    return Optional.of(new SessionConflict(existing, requested));
                }
            }
        }
        return Optional.empty();
    }

    public record SessionWindow(Long sessionId, OffsetDateTime start, OffsetDateTime end) {

        public SessionWindow {
            Objects.requireNonNull(start, "start is required");
            Objects.requireNonNull(end, "end is required");
            if (!start.isBefore(end)) {
                throw new IllegalArgumentException("Session window end must be after start");
            }
        }
    }

    public record SessionConflict(SessionWindow existingSession, SessionWindow requestedSession) {
    }
}

