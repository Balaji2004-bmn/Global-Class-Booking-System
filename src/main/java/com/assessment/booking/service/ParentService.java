package com.assessment.booking.service;

import com.assessment.booking.dto.request.CreateParentRequest;
import com.assessment.booking.dto.response.BookingResponse;
import com.assessment.booking.dto.response.ParentResponse;
import com.assessment.booking.entity.Booking;
import com.assessment.booking.entity.ClassSession;
import com.assessment.booking.entity.Offering;
import com.assessment.booking.entity.Parent;
import com.assessment.booking.exception.ResourceNotFoundException;
import com.assessment.booking.mapper.OfferingMapper;
import com.assessment.booking.repository.BookingRepository;
import com.assessment.booking.repository.ParentRepository;
import com.assessment.booking.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;
    private final BookingRepository bookingRepository;
    private final SessionRepository sessionRepository;
    private final OfferingMapper mapper;
    private final TimeZoneService timeZoneService;

    @Transactional
    public ParentResponse createParent(CreateParentRequest request) {
        Parent parent = Parent.builder()
                .name(request.name().trim())
                .timezone(request.timezone())
                .build();

        return mapper.toParentResponse(parentRepository.save(parent));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookings(Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found: " + parentId));

        ZoneId parentZone = timeZoneService.toZoneId(parent.getTimezone());
        List<Booking> bookings = bookingRepository.findDetailedByParentId(parentId);
        Map<Long, List<ClassSession>> sessionsByOffering = loadSessionsByOffering(bookings);

        return bookings.stream()
                .map(booking -> {
                    Offering offering = booking.getOffering();
                    return mapper.toBookingResponse(
                            booking,
                            offering,
                            sessionsByOffering.getOrDefault(offering.getId(), List.of()),
                            parentZone
                    );
                })
                .toList();
    }

    private Map<Long, List<ClassSession>> loadSessionsByOffering(List<Booking> bookings) {
        List<Long> offeringIds = bookings.stream()
                .map(booking -> booking.getOffering().getId())
                .distinct()
                .toList();

        if (offeringIds.isEmpty()) {
            return Map.of();
        }

        return sessionRepository.findByOfferingIdsOrderByStartTimeUtc(offeringIds).stream()
                .collect(Collectors.groupingBy(session -> session.getOffering().getId()));
    }
}

