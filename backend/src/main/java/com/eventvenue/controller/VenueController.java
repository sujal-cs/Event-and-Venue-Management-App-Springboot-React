package com.eventvenue.controller;

import com.eventvenue.dto.VenueRequest;
import com.eventvenue.entity.User;
import com.eventvenue.entity.Venue;
import com.eventvenue.repository.EventRepository;
import com.eventvenue.repository.UserRepository;
import com.eventvenue.repository.VenueRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class VenueController {
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public VenueController(VenueRepository venueRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/venues")
    public ResponseEntity<List<Venue>> getAllVenues() {
        return ResponseEntity.ok(venueRepository.findAll());
    }

    @PostMapping("/venues")
    public ResponseEntity<Venue> createVenue(@Valid @RequestBody VenueRequest request,
                                           @RequestParam Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new IllegalArgumentException("Only admins can register venues.");
        }

        Venue venue = new Venue(request.getName(), request.getLocation(), request.getCapacity());
        return ResponseEntity.ok(venueRepository.save(venue));
    }

    @DeleteMapping("/venues/{venueId}")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long venueId,
                                          @RequestParam Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found."));

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            throw new IllegalArgumentException("Only admins can delete venues.");
        }

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found."));

        boolean hasLinkedEvents = eventRepository.findAll().stream()
                .anyMatch(event -> event.getVenue() != null && event.getVenue().getId().equals(venueId));

        if (hasLinkedEvents) {
            throw new IllegalArgumentException("Cannot delete this venue because it is connected to existing events.");
        }

        venueRepository.delete(venue);
        return ResponseEntity.noContent().build();
    }
}
