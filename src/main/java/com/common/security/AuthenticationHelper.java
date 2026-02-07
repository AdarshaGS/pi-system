package com.common.security;

import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.users.data.Users;
import com.users.exception.UserNotFoundException;
import com.users.repo.UsersRepository;

/**
 * Helper class for authentication and authorization operations.
 * Provides utility methods to retrieve current user information and validate
 * access permissions.
 */
@Component
public class AuthenticationHelper {

    private final UsersRepository usersRepository;

    public AuthenticationHelper(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    /**
     * Gets the ID of the currently authenticated user.
     * 
     * @return the user ID of the authenticated user, or null if not authenticated
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        return usersRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
    }

    /**
     * Validates that the requested user ID matches the currently authenticated
     * user, or that the current user is an admin.
     * Throws AccessDeniedException if they don't match and user is not admin.
     * 
     * @param requestedUserId the user ID being requested
     * @throws AccessDeniedException if the requested user ID doesn't match the
     *                               authenticated user and user is not admin
     */
    public void validateUserAccess(Long requestedUserId) {
        // Admins can access any user's data
        if (isAdmin()) {
            return;
        }

        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        if (!requestedUserId.equals(currentUserId)) {
            throw new AccessDeniedException("You can only access your own data");
        }
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Checks if the current user has the ADMIN role.
     * 
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Validates that the current user has the ADMIN role.
     * Throws AccessDeniedException if they don't.
     */
    public void validateAdminAccess() {
        if (!isAdmin()) {
            throw new AccessDeniedException("Access denied. Admin role required.");
        }
    }

    // get USERS object
    public Users getUser(final Long id) {
        final Optional<Users> user = usersRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new UserNotFoundException();
        }
    }
}
