package com.assessment.booking.service;

import com.assessment.booking.dto.request.CreateTeacherRequest;
import com.assessment.booking.dto.response.OfferingResponse;
import com.assessment.booking.dto.response.TeacherResponse;
import com.assessment.booking.entity.ClassSession;
import com.assessment.booking.entity.Offering;
import com.assessment.booking.entity.Teacher;
import com.assessment.booking.exception.ResourceNotFoundException;
import com.assessment.booking.mapper.OfferingMapper;
import com.assessment.booking.repository.OfferingRepository;
import com.assessment.booking.repository.SessionRepository;
import com.assessment.booking.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final OfferingRepository offeringRepository;
    private final SessionRepository sessionRepository;
    private final OfferingMapper mapper;
    private final TimeZoneService timeZoneService;

    @Transactional
    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        Teacher teacher = Teacher.builder()
                .name(request.name().trim())
                .timezone(request.timezone())
                .build();

        return mapper.toTeacherResponse(teacherRepository.save(teacher));
    }

    @Transactional(readOnly = true)
    public List<OfferingResponse> getOfferings(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + teacherId));

        ZoneId teacherZone = timeZoneService.toZoneId(teacher.getTimezone());
        List<Offering> offerings = offeringRepository.findDetailedByTeacherId(teacherId);
        Map<Long, List<ClassSession>> sessionsByOffering = loadSessionsByOffering(offerings);

        return offerings.stream()
                .map(offering -> mapper.toOfferingResponse(
                        offering,
                        sessionsByOffering.getOrDefault(offering.getId(), List.of()),
                        teacherZone
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

