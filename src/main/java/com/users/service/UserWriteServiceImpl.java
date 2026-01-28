package com.users.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public Users createUser(Users user) {
        Role userRole = roleRepository.findByName("ROLE_USER_READ_ONLY")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        Users newUser = Users.builder()
                .name(user.getName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .password(passwordEncoder.encode(user.getPassword()))
                .roles(java.util.Collections.singleton(userRole))
                .build();

        return this.usersRepository.save(newUser);
    }

    @Override
    public void updateUserPassword(ForgotPasswordRequest forgotPasswordRequest) {

        Users user = this.usersRepository.findOneWithNotFoundDetection(forgotPasswordRequest.getEmail());
        user.setPassword(passwordEncoder.encode(forgotPasswordRequest.getPassword()));
        this.usersRepository.save(user);
    }

    public void deleteUser(String email) {
        Users user = this.usersRepository.findOneWithNotFoundDetection(email);
        this.usersRepository.delete(user);
    }

    public void updateUser(Users user) {
        this.usersRepository.save(user);
    }

}
