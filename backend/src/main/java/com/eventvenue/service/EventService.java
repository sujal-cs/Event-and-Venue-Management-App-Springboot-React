package com.eventvenue.service;

import com.eventvenue.dto.EventRequest;
import com.eventvenue.dto.EventResponse;
import com.eventvenue.entity.Event;
import com.eventvenue.entity.User;
import com.eventvenue.entity.Venue;
import com.eventvenue.exception.VenueConflictException;
import com.eventvenue.repository.EventRepository;
import com.eventvenue.repository.UserRepository;
import com.eventvenue.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, VenueRepository venueRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EventResponse createEvent(EventRequest request, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new IllegalArgumentException("Only admins can create events.");
        }

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue not found."));

        if (!request.getEndDateTime().isAfter(request.getStartDateTime())) {
            throw new IllegalArgumentException("Event end time must be after the start time.");
        }

        List<Event> overlapping = eventRepository.findOverlappingEvents(
                venue.getId(),
                request.getStartDateTime(),
                request.getEndDateTime()
        );

        if (!overlapping.isEmpty()) {
            String blockedTime = request.getStartDateTime() + " to " + request.getEndDateTime();
            throw new VenueConflictException("Venue '" + venue.getName() + "' is already booked for " + blockedTime + ".");
        }

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setVenue(venue);
        event.setCreatedBy(admin);
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());

        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse registerParticipant(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String role = user.getRole() == null ? "" : user.getRole().toUpperCase();
        if (!"USER".equals(role) && !"STUDENT".equals(role)) {
            throw new IllegalArgumentException("Only normal users can attend events.");
        }

        boolean alreadyRegistered = event.getRegisteredStudents().stream()
                .anyMatch(member -> member.getId().equals(userId));

        if (alreadyRegistered) {
            throw new IllegalArgumentException("User is already registered for this event.");
        }

        event.getRegisteredStudents().add(user);
        return toResponse(eventRepository.save(event), userId);
    }

    @Transactional
    public EventResponse updateEvent(Long eventId, EventRequest request, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new IllegalArgumentException("Only admins can update events.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found."));

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue not found."));

        if (!request.getEndDateTime().isAfter(request.getStartDateTime())) {
            throw new IllegalArgumentException("Event end time must be after the start time.");
        }

        List<Event> overlapping = eventRepository.findOverlappingEvents(
                venue.getId(),
                request.getStartDateTime(),
                request.getEndDateTime()
        ).stream()
                .filter(existing -> !existing.getId().equals(eventId))
                .collect(Collectors.toList());

        if (!overlapping.isEmpty()) {
            throw new VenueConflictException("Venue '" + venue.getName() + "' is already booked for the selected time.");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setVenue(venue);
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        return toResponse(eventRepository.save(event), adminId);
    }

    @Transactional
    public void deleteEvent(Long eventId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new IllegalArgumentException("Only admins can delete events.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found."));

        eventRepository.delete(event);
    }

    @Transactional
    public EventResponse registerStudent(Long eventId, Long studentId) {
        return registerParticipant(eventId, studentId);
    }

    public List<EventResponse> getAllEvents(Long userId) {
        return eventRepository.findAll().stream()
                .map(event -> toResponse(event, userId))
                .collect(Collectors.toList());
    }

    public List<EventResponse> getAllEvents() {
        return getAllEvents(null);
    }

    private EventResponse toResponse(Event event) {
        return toResponse(event, null);
    }

    private EventResponse toResponse(Event event, Long userId) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setStartDateTime(event.getStartDateTime());
        response.setEndDateTime(event.getEndDateTime());
        response.setVenueId(event.getVenue().getId());
        response.setVenueName(event.getVenue().getName());
        response.setCreatedByName(event.getCreatedBy().getName());
        response.setRegisteredCount(event.getRegisteredStudents().size());
        response.setUserRegistered(userId != null && event.getRegisteredStudents().stream()
                .anyMatch(student -> student.getId().equals(userId)));
        response.setAttendeeNames(event.getRegisteredStudents().stream()
                .map(student -> student.getName() != null && !student.getName().isBlank()
                        ? student.getName()
                        : student.getUsername())
                .collect(Collectors.toList()));
        return response;
    }
}
