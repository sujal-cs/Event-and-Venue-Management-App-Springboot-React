package com.eventvenue.service;

import com.eventvenue.dto.EventRequest;
import com.eventvenue.dto.EventResponse;
import com.eventvenue.dto.LoginRequest;
import com.eventvenue.dto.RegisterRequest;
import com.eventvenue.entity.Event;
import com.eventvenue.entity.User;
import com.eventvenue.entity.Venue;
import com.eventvenue.exception.VenueConflictException;
import com.eventvenue.repository.EventRepository;
import com.eventvenue.repository.UserRepository;
import com.eventvenue.repository.VenueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void shouldRejectOverlappingEventsForSameVenue() {
        Venue venue = new Venue();
        venue.setId(1L);
        venue.setName("Main Hall");
        venue.setLocation("Campus Center");
        venue.setCapacity(200);

        User admin = new User();
        admin.setId(10L);
        admin.setName("System Admin");
        admin.setEmail("admin@college.edu");
        admin.setUsername("admin");
        admin.setRole("ADMIN");

        Event existing = new Event();
        existing.setId(99L);
        existing.setTitle("Orientation");
        existing.setDescription("Welcome event");
        existing.setStartDateTime(LocalDateTime.of(2026, 10, 15, 9, 0));
        existing.setEndDateTime(LocalDateTime.of(2026, 10, 15, 11, 0));
        existing.setVenue(venue);
        existing.setCreatedBy(admin);

        EventRequest request = new EventRequest();
        request.setTitle("Hackathon");
        request.setDescription("Student coding contest");
        request.setVenueId(1L);
        request.setStartDateTime(LocalDateTime.of(2026, 10, 15, 10, 30));
        request.setEndDateTime(LocalDateTime.of(2026, 10, 15, 12, 0));

        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(userRepository.findById(10L)).thenReturn(Optional.of(admin));
        when(eventRepository.findOverlappingEvents(1L, request.getStartDateTime(), request.getEndDateTime()))
                .thenReturn(List.of(existing));

        assertThrows(VenueConflictException.class, () -> eventService.createEvent(request, 10L));
    }

    @Test
    void shouldAllowLoginWithUsernameForDefaultAdmin() {
        UserService userService = new UserService(userRepository);
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setEmail("admin@eventhub.test");
        admin.setPassword("admin");
        admin.setRole("ADMIN");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        User loggedIn = userService.login(loginRequest);

        assertEquals("admin", loggedIn.getUsername());
        assertEquals("ADMIN", loggedIn.getRole());
    }

    @Test
    void shouldStoreUsernameWhenRegisteringUser() {
        UserService userService = new UserService(userRepository);
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("pass123");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(15L);
                    return user;
                });

        User saved = userService.register(request);

        assertEquals("alice", saved.getUsername());
        assertEquals("USER", saved.getRole());
    }

    @Test
    void shouldMarkEventAsRegisteredForCurrentUser() {
        User user = new User();
        user.setId(7L);
        user.setName("Alice");
        user.setRole("USER");

        Venue venue = new Venue();
        venue.setId(2L);
        venue.setName("Campus Hall");
        venue.setLocation("North Campus");
        venue.setCapacity(100);

        Event event = new Event();
        event.setId(3L);
        event.setTitle("Hack Night");
        event.setDescription("Build together");
        event.setVenue(venue);
        event.setCreatedBy(user);
        event.setStartDateTime(LocalDateTime.of(2026, 10, 15, 18, 0));
        event.setEndDateTime(LocalDateTime.of(2026, 10, 15, 20, 0));
        event.setRegisteredStudents(List.of(user));

        when(eventRepository.findAll()).thenReturn(List.of(event));

        var events = eventService.getAllEvents(7L);

        assertEquals(1, events.size());
        assertEquals(true, events.get(0).isUserRegistered());
    }

    @Test
    void shouldExposeAttendeeNamesForAdminView() {
        User first = new User();
        first.setId(1L);
        first.setName("Alice");
        first.setRole("USER");

        User second = new User();
        second.setId(2L);
        second.setName("Bob");
        second.setRole("USER");

        Venue venue = new Venue();
        venue.setId(5L);
        venue.setName("Sky Hall");
        venue.setLocation("North Campus");
        venue.setCapacity(80);

        Event event = new Event();
        event.setId(40L);
        event.setTitle("Speaker Session");
        event.setDescription("Guest talk");
        event.setVenue(venue);
        event.setCreatedBy(new User());
        event.setStartDateTime(LocalDateTime.of(2026, 11, 10, 14, 0));
        event.setEndDateTime(LocalDateTime.of(2026, 11, 10, 16, 0));
        event.setRegisteredStudents(List.of(first, second));

        when(eventRepository.findAll()).thenReturn(List.of(event));

        EventResponse response = eventService.getAllEvents().get(0);

        assertEquals(List.of("Alice", "Bob"), response.getAttendeeNames());
    }

    @Test
    void shouldDeleteEventForAdmin() {
        User admin = new User();
        admin.setId(10L);
        admin.setRole("ADMIN");

        Event event = new Event();
        event.setId(20L);
        event.setTitle("Workshop");
        event.setDescription("Topic" );
        event.setStartDateTime(LocalDateTime.of(2026, 10, 20, 9, 0));
        event.setEndDateTime(LocalDateTime.of(2026, 10, 20, 11, 0));
        event.setCreatedBy(admin);

        when(userRepository.findById(10L)).thenReturn(Optional.of(admin));
        when(eventRepository.findById(20L)).thenReturn(Optional.of(event));

        eventService.deleteEvent(20L, 10L);

        verify(eventRepository).delete(event);
    }

    @Test
    void shouldUpdateEventForAdmin() {
        User admin = new User();
        admin.setId(12L);
        admin.setRole("ADMIN");

        Venue venue = new Venue();
        venue.setId(3L);
        venue.setName("Innovation Lab");
        venue.setLocation("Main Tower");
        venue.setCapacity(40);

        Event event = new Event();
        event.setId(30L);
        event.setTitle("Old title");
        event.setDescription("Old description");
        event.setVenue(venue);
        event.setCreatedBy(admin);
        event.setStartDateTime(LocalDateTime.of(2026, 11, 1, 9, 0));
        event.setEndDateTime(LocalDateTime.of(2026, 11, 1, 10, 0));

        EventRequest updateRequest = new EventRequest();
        updateRequest.setTitle("New title");
        updateRequest.setDescription("New description");
        updateRequest.setVenueId(3L);
        updateRequest.setStartDateTime(LocalDateTime.of(2026, 11, 1, 9, 30));
        updateRequest.setEndDateTime(LocalDateTime.of(2026, 11, 1, 11, 0));

        when(userRepository.findById(12L)).thenReturn(Optional.of(admin));
        when(eventRepository.findById(30L)).thenReturn(Optional.of(event));
        when(venueRepository.findById(3L)).thenReturn(Optional.of(venue));
        when(eventRepository.findOverlappingEvents(3L, updateRequest.getStartDateTime(), updateRequest.getEndDateTime()))
                .thenReturn(List.of());
        when(eventRepository.save(event)).thenReturn(event);

        EventResponse response = eventService.updateEvent(30L, updateRequest, 12L);

        assertEquals("New title", response.getTitle());
        assertEquals("New description", response.getDescription());
    }
}
