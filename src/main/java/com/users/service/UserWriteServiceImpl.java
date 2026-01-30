package com.users.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.data.ForgotPasswordRequest;
import com.auth.data.Role;
import com.users.data.Users;
import com.users.repo.UsersRepositoryWrapper;

@Service
public class UserWriteServiceImpl implements UserWriteService {

    private final UsersRepositoryWrapper usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.auth.repo.RoleRepository roleRepository;

    public UserWriteServiceImpl(UsersRepositoryWrapper usersRepository,
            PasswordEncoder passwordEncoder,
            com.auth.repo.RoleRepository roleRepository) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public Users createUser(Users user) {
        Role userRole = roleRepository.findByName("ROLE_USER_READ_ONLY")
                .orElseThrow(() -> new RuntimeException(
                        "Error: Role 'ROLE_USER_READ_ONLY' is not found in database. " +
                                "Please ensure Flyway migrations have run successfully. " +
                                "Check if V24__Implement_RBAC.sql migration has been executed."));
        validateUserEmail(user.getEmail());
        validatePassword(user.getPassword());
        Users newUser = Users.builder()
                .name(user.getName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .password(passwordEncoder.encode(user.getPassword()))
                .roles(java.util.Collections.singleton(userRole))
                .build();

        return this.usersRepository.save(newUser);
    }

    private void validateUserEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }

    @Override
    @Transactional
    public void updateUserPassword(ForgotPasswordRequest forgotPasswordRequest) {

        Users user = this.usersRepository.findOneWithNotFoundDetection(forgotPasswordRequest.getEmail());
        user.setPassword(passwordEncoder.encode(forgotPasswordRequest.getPassword()));
        this.usersRepository.save(user);
    }

    @Transactional
    public void deleteUser(String email) {
        Users user = this.usersRepository.findOneWithNotFoundDetection(email);
        this.usersRepository.delete(user);
    }

    @Transactional
    public void updateUser(Users user) {
        this.usersRepository.save(user);
    }

}
