package com.pisystem.core.users.service;

import com.pisystem.core.auth.data.ForgotPasswordRequest;
import com.pisystem.core.users.data.Users;

public interface UserWriteService {
    Users createUser(Users user);

    void updateUserPassword(ForgotPasswordRequest forgotPasswordRequest);

    void deleteUser(String email);

    void updateUser(Users user);

}
