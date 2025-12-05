package com.users.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.users.data.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {
    
}
