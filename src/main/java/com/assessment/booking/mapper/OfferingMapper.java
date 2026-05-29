package com.assessment.booking.mapper;

import com.assessment.booking.dto.response.BookingResponse;
import com.assessment.booking.dto.response.CourseResponse;
import com.assessment.booking.dto.response.OfferingResponse;
import com.assessment.booking.dto.response.ParentResponse;
import com.assessment.booking.dto.response.SessionResponse;
import com.assessment.booking.dto.response.TeacherResponse;
import com.assessment.booking.entity.Booking;
import com.assessment.booking.entity.ClassSession;
import com.assessment.booking.entity.Course;
import com.assessment.booking.entity.Offering;
import com.assessment.booking.entity.Parent;
import com.assessment.booking.entity.Teacher;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;

@Component
public class OfferingMapper {

    public TeacherResponse toTeacherResponse(Teacher teacher) {
        return new TeacherResponse(teacher.getId(), teacher.getName(), teacher.getTimezone());
    }

    public ParentResponse toParentResponse(Parent parent) {
        return new ParentResponse(parent.getId(), parent.getName(), parent.getTimezone());
    }

    public CourseResponse toCourseResponse(Course course) {
        return new CourseResponse(course.getId(), course.getTitle(), course.getDescription());
    }

    public SessionResponse toSessionResponse(ClassSession session, ZoneId targetZone) {
        return new SessionResponse(
                session.getId(),
                session.getStartTimeUtc().atZoneSameInstant(targetZone),
                session.getEndTimeUtc().atZoneSameInstant(targetZone),
                targetZone.getId()
        );
    }

    public OfferingResponse toOfferingResponse(Offering offering, List<ClassSession> sessions, ZoneId targetZone) {
        return new OfferingResponse(
                offering.getId(),
                offering.getCourse().getId(),
                offering.getCourse().getTitle(),
                offering.getTeacher().getId(),
                offering.getTeacher().getName(),
                offering.getTitle(),
                targetZone.getId(),
                offering.getCreatedAt(),
                sessions.stream()
                        .map(session -> toSessionResponse(session, targetZone))
                        .toList()
        );
    }

    public BookingResponse toBookingResponse(Booking booking, Offering offering, List<ClassSession> sessions, ZoneId targetZone) {
        return new BookingResponse(
                booking.getId(),
                booking.getParent().getId(),
                offering.getId(),
                offering.getTitle(),
                booking.getBookedAt(),
                sessions.stream()
                        .map(session -> toSessionResponse(session, targetZone))
                        .toList()
        );
    }
}
