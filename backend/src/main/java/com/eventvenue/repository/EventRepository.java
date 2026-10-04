package com.eventvenue.repository;

import com.eventvenue.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM Event e WHERE e.venue.id = :venueId AND e.startDateTime < :endDateTime AND e.endDateTime > :startDateTime")
    List<Event> findOverlappingEvents(
            @Param("venueId") Long venueId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
