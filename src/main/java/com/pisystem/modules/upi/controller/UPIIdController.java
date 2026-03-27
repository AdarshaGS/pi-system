package com.pisystem.modules.upi.controller;

import com.pisystem.shared.security.AuthenticationHelper;
import com.pisystem.modules.upi.dto.UPIIdRequest;
import com.pisystem.modules.upi.dto.UpiIdCreationResponse;
import com.pisystem.modules.upi.service.UPIIdService;
import com.pisystem.core.users.data.Users;

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
        UpiIdCreationResponse result = upiIdService.createUpiId(String.valueOf(currentUser.getId()), request);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<?> getUpiIds() {
        Users currentUser = authenticationHelper.getUser(authenticationHelper.getCurrentUserId());
        return ResponseEntity.ok(upiIdService.getUpiIds(String.valueOf(currentUser.getId())));
    }
}
