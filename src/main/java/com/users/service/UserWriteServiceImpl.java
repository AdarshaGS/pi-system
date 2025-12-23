package com.users.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth.data.ForgotPasswordRequest;
import com.users.data.Users;
import com.users.repo.UsersRepositoryWrapper;

@Service
public class UserWriteServiceImpl implements UserWriteService {

    private final UsersRepositoryWrapper usersRepository;
    private final PasswordEncoder passwordEncoder;

    public UserWriteServiceImpl(UsersRepositoryWrapper usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Users createUser(Users user) {
        Users newUser = Users.builder()
                .name(user.getName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .password(passwordEncoder.encode(user.getPassword()))
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
