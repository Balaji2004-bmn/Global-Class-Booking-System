package com.assessment.booking.service;

import com.assessment.booking.dto.request.BookingRequest;
import com.assessment.booking.dto.response.BookingResponse;
import com.assessment.booking.entity.Booking;
import com.assessment.booking.entity.ClassSession;
import com.assessment.booking.entity.Offering;
import com.assessment.booking.entity.Parent;
import com.assessment.booking.exception.BadRequestException;
import com.assessment.booking.exception.ConflictException;
import com.assessment.booking.exception.ResourceNotFoundException;
import com.assessment.booking.mapper.OfferingMapper;
import com.assessment.booking.repository.BookingRepository;
import com.assessment.booking.repository.OfferingRepository;
import com.assessment.booking.repository.ParentRepository;
import com.assessment.booking.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final ParentRepository parentRepository;
    private final OfferingRepository offeringRepository;
    private final BookingRepository bookingRepository;
    private final SessionRepository sessionRepository;
    private final SessionOverlapService sessionOverlapService;
    private final TimeZoneService timeZoneService;
    private final OfferingMapper mapper;

    @Transactional
    public BookingResponse book(BookingRequest request) {
        Parent parent = parentRepository.findByIdForUpdate(request.parentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found: " + request.parentId()));

        List<Booking> lockedBookings = bookingRepository.findByParentIdForUpdate(parent.getId());

        Offering offering = offeringRepository.findDetailedById(request.offeringId())
                .orElseThrow(() -> new ResourceNotFoundException("Offering not found: " + request.offeringId()));

        if (lockedBookings.stream().anyMatch(booking -> booking.getOffering().getId().equals(offering.getId()))) {
            throw new ConflictException("Parent has already booked this offering");
        }

        List<ClassSession> requestedSessions = sessionRepository.findByOfferingIdOrderByStartTimeUtc(offering.getId());
        if (requestedSessions.isEmpty()) {
            throw new BadRequestException("Offering must have at least one session before it can be booked");
        }

        List<ClassSession> existingBookedSessions = sessionRepository.findBookedSessionsByParentId(parent.getId());
        sessionOverlapService.findFirstConflict(toSessionWindows(existingBookedSessions), toSessionWindows(requestedSessions))
                .ifPresent(conflict -> {
                    throw new ConflictException(
                            "Requested offering overlaps with an existing booking. Existing session id: "
                                    + conflict.existingSession().sessionId()
                                    + ", requested session id: "
                                    + conflict.requestedSession().sessionId()
                    );
                });

        Booking booking = Booking.builder()
                .parent(parent)
                .offering(offering)
                .build();

        Booking saved = bookingRepository.saveAndFlush(booking);
        ZoneId parentZone = timeZoneService.toZoneId(parent.getTimezone());
        return mapper.toBookingResponse(saved, offering, requestedSessions, parentZone);
    }

    private List<SessionOverlapService.SessionWindow> toSessionWindows(List<ClassSession> sessions) {
        return sessions.stream()
                .map(session -> new SessionOverlapService.SessionWindow(
                        session.getId(),
                        session.getStartTimeUtc(),
                        session.getEndTimeUtc()
                ))
                .toList();
    }
}

