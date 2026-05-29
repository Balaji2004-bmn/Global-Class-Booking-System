package com.assessment.booking.controller;

import com.assessment.booking.dto.request.CreateParentRequest;
import com.assessment.booking.dto.response.BookingResponse;
import com.assessment.booking.dto.response.ParentResponse;
import com.assessment.booking.service.ParentService;
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
public class ParentController {

    private final ParentService parentService;

    @PostMapping("/parents")
    public ResponseEntity<ParentResponse> createParent(@Valid @RequestBody CreateParentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parentService.createParent(request));
    }

    @GetMapping("/parents/{parentId}/bookings")
    public List<BookingResponse> getParentBookings(@PathVariable @Positive Long parentId) {
        return parentService.getBookings(parentId);
    }
}

