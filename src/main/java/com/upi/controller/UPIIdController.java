package com.upi.controller;

import com.common.security.AuthenticationHelper;
import com.upi.dto.UPIIdRequest;
import com.upi.service.UPIIdService;
import com.users.data.Users;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/upi/ids")
public class UPIIdController {

    @Autowired
    private UPIIdService upiIdService;

    @Autowired
    private AuthenticationHelper authenticationHelper;

    @PostMapping
    public ResponseEntity<?> createUpiId(@RequestBody UPIIdRequest request) {
        Users currentUser = authenticationHelper.getUser(authenticationHelper.getCurrentUserId());
        Map<String, Object> result = upiIdService.createUpiId(String.valueOf(currentUser.getId()), request);
        return ResponseEntity.ok(result);
    }
}
