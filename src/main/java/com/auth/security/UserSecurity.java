package com.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.users.repo.UsersRepository;
import lombok.extern.slf4j.Slf4j;

@Component("userSecurity")
@Slf4j
public class UserSecurity {

    @Autowired
    private UsersRepository usersRepository;

    public boolean hasUserId(Long userId) {
        if (userId == null) {
            log.warn("UserSecurity.hasUserId called with null userId");
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("UserSecurity.hasUserId: No authenticated user found");
            return false;
        }

        String email = authentication.getName();
        if (email == null || "anonymousUser".equals(email)) {
            log.warn("UserSecurity.hasUserId: Anonymous user or null email");
            return false;
        }

        boolean hasAccess = usersRepository.findByEmail(email)
                .map(user -> {
                    boolean matches = user.getId().equals(userId);
                    if (!matches) {
                        log.warn("UserSecurity: User {} (ID: {}) attempted to access resource for userId: {}", 
                            email, user.getId(), userId);
                    }
                    return matches;
                })
                .orElse(false);

        return hasAccess;
    }
}
