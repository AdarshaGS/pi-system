package com.upi.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.upi.model.UpiPin;
import com.upi.repository.UpiPinRepository;
import com.users.data.Users;
import com.users.repo.UsersRepository;

@Service
public class UPIPinService {
    @Autowired
    private UpiPinRepository upiPinRepository;
    @Autowired
    private UsersRepository userRepository;

    public Map<String, Object> createPin(String userId, String newPin) {
        Map<String, Object> response = new HashMap<>();
        Users user = userRepository.findById(Long.parseLong(userId)).orElse(null);
        if (user == null) {
            response.put("status", "failed");
            response.put("message", "User not found");
            return response;
        }
        if (upiPinRepository.findByUserId(user.getId()) != null) {
            response.put("status", "failed");
            response.put("message", "PIN already exists");
            return response;
        }
        UpiPin pin = new UpiPin();
        pin.setUser(user);
        String hashed = BCrypt.hashpw(newPin, BCrypt.gensalt());
        pin.setPinHash(hashed);
        upiPinRepository.save(pin);
        response.put("status", "created");
        return response;
    }

    public Map<String, Object> changePin(String userId, String oldPin, String newPin) {
        Map<String, Object> response = new HashMap<>();
        Users user = userRepository.findById(Long.parseLong(userId)).orElse(null);
        if (user == null) {
            response.put("status", "failed");
            response.put("message", "User not found");
            return response;
        }
        UpiPin pin = upiPinRepository.findByUserId(user.getId());
        if (pin == null || !BCrypt.checkpw(oldPin, pin.getPinHash())) {
            response.put("status", "failed");
            response.put("message", "Invalid old PIN");
            return response;
        }
        String hashed = BCrypt.hashpw(newPin, BCrypt.gensalt());
        pin.setPinHash(hashed);
        upiPinRepository.save(pin);
        response.put("status", "changed");
        return response;
    }

    public Map<String, Object> resetPin(String userId, String newPin) {
        Map<String, Object> response = new HashMap<>();
        Users user = userRepository.findById(Long.parseLong(userId)).orElse(null);
        if (user == null) {
            response.put("status", "failed");
            response.put("message", "User not found");
            return response;
        }
        UpiPin pin = upiPinRepository.findByUserId(user.getId());
        if (pin == null) {
            response.put("status", "failed");
            response.put("message", "PIN not found");
            return response;
        }
        String hashed = BCrypt.hashpw(newPin, BCrypt.gensalt());
        pin.setPinHash(hashed);
        upiPinRepository.save(pin);
        response.put("status", "reset");
        return response;
    }
}
