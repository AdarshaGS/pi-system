package com.users.service;

import org.springframework.stereotype.Service;

import com.users.data.Users;
import com.users.repo.UsersRepository;

@Service
public class UserWriteServiceImpl implements UserWriteService {

    private final UsersRepository usersRepository;

    public UserWriteServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public Users createUser(Users user) {
        Users newUser = Users.builder()
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .build();

        return this.usersRepository.save(newUser);
    }

    @Override
    public Users updateUser(Long userId, Users user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    @Override
    public Users deleteUser(Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }
    
}
