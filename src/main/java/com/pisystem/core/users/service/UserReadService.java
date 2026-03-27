package com.pisystem.core.users.service;

import com.pisystem.core.users.data.Users;
import java.util.List;

public interface UserReadService {

    Users getUserById(Long userId);
    
    List<Users> getAllUsers();
    
}
