package com.eventvenue.config;

import com.eventvenue.entity.User;
import com.eventvenue.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DefaultAdminInitializer implements ApplicationRunner {
    private final UserRepository userRepository;

    public DefaultAdminInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        userRepository.findByUsername("admin")
                .ifPresentOrElse(
                        user -> {
                            if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                                user.setRole("ADMIN");
                                userRepository.save(user);
                            }
                        },
                        () -> {
                            User admin = new User();
                            admin.setName("Default Admin");
                            admin.setUsername("admin");
                            admin.setEmail("admin@eventhub.local");
                            admin.setPassword("admin");
                            admin.setRole("ADMIN");
                            userRepository.save(admin);
                        }
                );
    }
}
