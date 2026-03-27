package com.pisystem.core.users.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pisystem.core.users.data.Users;



public interface UsersRepository extends JpaRepository<Users, Long>, JpaSpecificationExecutor<Users> {

    Optional<Users> findByEmail(String email);
    
    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<Users> findByEmailWithRoles(@Param("email") String email);
}
