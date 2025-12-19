package com.users.repo;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.users.data.Users;
import com.users.exception.UserNotFoundException;

@Service
public class UsersRepositoryWrapper {

    private final UsersRepository usersRepository;

    public UsersRepositoryWrapper(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public Users findOneWithNotFoundDetection(String email) {
        Optional<Users> user = usersRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new UserNotFoundException(email);
        }
        return user.get();
    }

    // save user
    public Users save(Users user) {
        return this.usersRepository.save(user);
    }

    public void delete(Users users) {
        this.usersRepository.delete(users);
    }
}
