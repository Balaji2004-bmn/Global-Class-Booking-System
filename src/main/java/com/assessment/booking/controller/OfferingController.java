package com.assessment.booking.controller;

import com.assessment.booking.config.validation.ValidTimezone;
import com.assessment.booking.dto.request.CreateOfferingRequest;
import com.assessment.booking.dto.request.CreateSessionRequest;
import com.assessment.booking.dto.response.OfferingResponse;
import com.assessment.booking.dto.response.SessionResponse;
import com.assessment.booking.service.OfferingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService offeringService;

    @PostMapping("/offerings")
    public ResponseEntity<OfferingResponse> createOffering(@Valid @RequestBody CreateOfferingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offeringService.createOffering(request));
    }

    @PostMapping("/offerings/{id}/sessions")
    public ResponseEntity<SessionResponse> addSession(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CreateSessionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offeringService.addSession(id, request));
    }

    @GetMapping("/offerings")
    public List<OfferingResponse> getOfferings(
            @RequestParam @NotBlank @ValidTimezone String timezone
    ) {
        return offeringService.getOfferings(timezone);
    }
}

