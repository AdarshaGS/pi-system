package com.users.service;

import org.springframework.stereotype.Service;

import com.users.data.Users;
import com.users.exception.UserNotFoundException;
import com.users.repo.UsersRepository;

@Service
public class UserReadServiceImpl implements UserReadService {

    private final UsersRepository usersRepository;

    public UserReadServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public Users getUserById(Long userId) {
        return this.usersRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

}
