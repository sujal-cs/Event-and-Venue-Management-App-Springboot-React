package com.eventvenue.config;

import com.eventvenue.entity.User;
import com.eventvenue.entity.Venue;
import com.eventvenue.repository.UserRepository;
import com.eventvenue.repository.VenueRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(UserRepository userRepository, VenueRepository venueRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User("Admin User", "admin@college.test", "admin123", "ADMIN"));
                userRepository.save(new User("Participant User", "participant@college.test", "participant123", "USER"));
            }

            if (venueRepository.count() == 0) {
                venueRepository.save(new Venue("Main Hall", "North Campus", 250));
                venueRepository.save(new Venue("Auditorium", "Science Block", 180));
                venueRepository.save(new Venue("Conference Room", "Administration Building", 80));
            }
        };
    }
}
