package com.pisystem.core.users.service;

import org.springframework.stereotype.Service;

import com.pisystem.core.users.data.Users;
import com.pisystem.core.users.repo.UsersRepository;
import java.util.List;

@Service
public class UserReadServiceImpl implements UserReadService {

    private final UsersRepository usersRepository;

    public UserReadServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public Users getUserById(Long userId) {
        return this.usersRepository.findById(userId).get();
    }

    @Override
    public List<Users> getAllUsers() {
        return this.usersRepository.findAll();
    }

}
