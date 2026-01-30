package com.api.config;

import com.auth.data.Role;
import com.auth.repo.RoleRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Initialize test data after application context is ready
 */
@Component
@Profile("test")
public class TestDataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final RoleRepository roleRepository;

    public TestDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Insert roles if they don't exist
        if (roleRepository.findByName("ROLE_USER_READ_ONLY").isEmpty()) {
            Role userRole = Role.builder()
                    .name("ROLE_USER_READ_ONLY")
                    .build();
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .build();
            roleRepository.save(adminRole);
        }

        if (roleRepository.findByName("ROLE_SUPER_ADMIN").isEmpty()) {
            Role superAdminRole = Role.builder()
                    .name("ROLE_SUPER_ADMIN")
                    .build();
            roleRepository.save(superAdminRole);
        }
    }
}
