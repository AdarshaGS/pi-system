package com.users.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.users.data.Users;
import com.users.service.UserReadService;
import com.users.service.UserWriteService;

@RestController
@RequestMapping("/api/users")
public class UsersAPIResource {
    
    private final UserWriteService userWriteService;
    private final UserReadService userReadService;

    public UsersAPIResource(UserWriteService userWriteService, UserReadService userReadService) {
        this.userWriteService = userWriteService;
        this.userReadService = userReadService;
    }

    // create user
    @PostMapping("/create")
    public Users createUser(@RequestBody Users user) {
        return userWriteService.createUser(user);
    }

    // // update user
    // @PutMapping("/update")


    // // delete user
    // @PostMapping("/delete")
    // get user details
    @GetMapping("/{userId}")
    public Users getUserDetails(@PathVariable Long userId) {
        return userReadService.getUserById(userId);
    }
}
