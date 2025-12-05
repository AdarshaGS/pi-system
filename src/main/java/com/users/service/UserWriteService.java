package com.users.service;

import com.users.data.Users;

public interface UserWriteService {
    Users createUser(Users user);

    Users updateUser(Long userId, Users user);

    Users deleteUser(Long userId);
}
