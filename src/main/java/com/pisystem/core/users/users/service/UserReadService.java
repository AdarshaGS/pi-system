package com.users.service;

import com.users.data.Users;
import java.util.List;

public interface UserReadService {

    Users getUserById(Long userId);
    
    List<Users> getAllUsers();
    
}
