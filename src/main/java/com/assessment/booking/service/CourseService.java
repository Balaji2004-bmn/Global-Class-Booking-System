package com.assessment.booking.service;

import com.assessment.booking.dto.request.CreateCourseRequest;
import com.assessment.booking.dto.response.CourseResponse;
import com.assessment.booking.entity.Course;
import com.assessment.booking.mapper.OfferingMapper;
import com.assessment.booking.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final OfferingMapper mapper;

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        Course course = Course.builder()
                .title(request.title().trim())
                .description(request.description())
                .build();

        return mapper.toCourseResponse(courseRepository.save(course));
    }
}

