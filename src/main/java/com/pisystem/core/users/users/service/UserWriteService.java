package com.users.service;

import com.auth.data.ForgotPasswordRequest;
import com.users.data.Users;

public interface UserWriteService {
    Users createUser(Users user);

    void updateUserPassword(ForgotPasswordRequest forgotPasswordRequest);

    void deleteUser(String email);

    void updateUser(Users user);

}
