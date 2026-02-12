package com.upi.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.upi.model.UpiId;
import com.upi.repository.UpiIdRepository;
import com.users.data.Users;
import com.users.repo.UsersRepository;

@Service
public class UPIIdService {
    @Autowired
    private UpiIdRepository upiIdRepository;
    @Autowired
    private UsersRepository userRepository;

    public Map<String, Object> createUpiId(String userId, String upiId) {
        Map<String, Object> response = new HashMap<>();
        Users user = userRepository.findById(Long.parseLong(userId)).orElse(null);
        if (user == null) {
            response.put("status", "failed");
            response.put("message", "User not found");
            return response;
        }
        if (upiIdRepository.findByUpiId(upiId) != null) {
            response.put("status", "failed");
            response.put("message", "UPI ID already exists");
            return response;
        }
        UpiId newUpiId = new UpiId();
        newUpiId.setUser(user);
        newUpiId.setUpiId(upiId);
        upiIdRepository.save(newUpiId);
        response.put("upiId", upiId);
        response.put("status", "created");
        return response;
    }
}
