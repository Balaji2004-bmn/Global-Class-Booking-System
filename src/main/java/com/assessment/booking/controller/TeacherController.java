package com.assessment.booking.controller;

import com.assessment.booking.dto.request.CreateTeacherRequest;
import com.assessment.booking.dto.response.OfferingResponse;
import com.assessment.booking.dto.response.TeacherResponse;
import com.assessment.booking.service.TeacherService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping("/teachers")
    public ResponseEntity<TeacherResponse> createTeacher(@Valid @RequestBody CreateTeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.createTeacher(request));
    }

    @GetMapping("/teachers/{teacherId}/offerings")
    public List<OfferingResponse> getTeacherOfferings(@PathVariable @Positive Long teacherId) {
        return teacherService.getOfferings(teacherId);
    }
}

