package com.eventvenue.controller;

import com.eventvenue.dto.EventRequest;
import com.eventvenue.dto.EventResponse;
import com.eventvenue.dto.StudentRegistrationRequest;
import com.eventvenue.entity.Event;
import com.eventvenue.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventResponse>> getAllEvents(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(eventService.getAllEvents(userId));
    }

    @PostMapping("/events")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request,
                                                   @RequestParam Long adminId) {
        return ResponseEntity.ok(eventService.createEvent(request, adminId));
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long eventId,
                                                   @Valid @RequestBody EventRequest request,
                                                   @RequestParam Long adminId) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, request, adminId));
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId,
                                           @RequestParam Long adminId) {
        eventService.deleteEvent(eventId, adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/events/{eventId}/register")
    public ResponseEntity<EventResponse> registerStudent(@PathVariable Long eventId,
                                                       @Valid @RequestBody StudentRegistrationRequest request) {
        return ResponseEntity.ok(eventService.registerParticipant(eventId, request.getStudentId()));
    }
}
