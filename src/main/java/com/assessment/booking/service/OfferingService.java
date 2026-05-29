package com.assessment.booking.service;

import com.assessment.booking.dto.request.CreateOfferingRequest;
import com.assessment.booking.dto.request.CreateSessionRequest;
import com.assessment.booking.dto.response.OfferingResponse;
import com.assessment.booking.dto.response.SessionResponse;
import com.assessment.booking.entity.ClassSession;
import com.assessment.booking.entity.Course;
import com.assessment.booking.entity.Offering;
import com.assessment.booking.entity.Teacher;
import com.assessment.booking.exception.BadRequestException;
import com.assessment.booking.exception.ResourceNotFoundException;
import com.assessment.booking.mapper.OfferingMapper;
import com.assessment.booking.repository.CourseRepository;
import com.assessment.booking.repository.OfferingRepository;
import com.assessment.booking.repository.SessionRepository;
import com.assessment.booking.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferingService {

    private final OfferingRepository offeringRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final SessionRepository sessionRepository;
    private final OfferingMapper mapper;
    private final TimeZoneService timeZoneService;

    @Transactional
    public OfferingResponse createOffering(CreateOfferingRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + request.courseId()));
        Teacher teacher = teacherRepository.findById(request.teacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + request.teacherId()));

        ZoneId offeringZone = timeZoneService.toZoneId(request.timezone());
        Offering offering = Offering.builder()
                .course(course)
                .teacher(teacher)
                .title(request.title().trim())
                .timezone(offeringZone.getId())
                .build();

        Offering saved = offeringRepository.save(offering);
        return mapper.toOfferingResponse(saved, List.of(), offeringZone);
    }

    @Transactional
    public SessionResponse addSession(Long offeringId, CreateSessionRequest request) {
        Offering offering = offeringRepository.findDetailedById(offeringId)
                .orElseThrow(() -> new ResourceNotFoundException("Offering not found: " + offeringId));

        ZoneId offeringZone = timeZoneService.toZoneId(offering.getTimezone());
        OffsetDateTime startUtc = timeZoneService.toUtc(request.startTime(), offeringZone);
        OffsetDateTime endUtc = timeZoneService.toUtc(request.endTime(), offeringZone);

        if (!startUtc.isBefore(endUtc)) {
            throw new BadRequestException("Session endTime must be after startTime once converted to UTC");
        }

        ClassSession session = ClassSession.builder()
                .offering(offering)
                .startTimeUtc(startUtc)
                .endTimeUtc(endUtc)
                .build();

        return mapper.toSessionResponse(sessionRepository.save(session), offeringZone);
    }

    @Transactional(readOnly = true)
    public List<OfferingResponse> getOfferings(String timezone) {
        ZoneId targetZone = timeZoneService.toZoneId(timezone);
        List<Offering> offerings = offeringRepository.findAllDetailed();
        Map<Long, List<ClassSession>> sessionsByOffering = loadSessionsByOffering(offerings);

        return offerings.stream()
                .map(offering -> mapper.toOfferingResponse(
                        offering,
                        sessionsByOffering.getOrDefault(offering.getId(), List.of()),
                        targetZone
                ))
                .toList();
    }

    private Map<Long, List<ClassSession>> loadSessionsByOffering(List<Offering> offerings) {
        List<Long> offeringIds = offerings.stream().map(Offering::getId).toList();
        if (offeringIds.isEmpty()) {
            return Map.of();
        }

        return sessionRepository.findByOfferingIdsOrderByStartTimeUtc(offeringIds).stream()
                .collect(Collectors.groupingBy(session -> session.getOffering().getId()));
    }
}

