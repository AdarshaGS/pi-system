package com.upi.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.upi.dto.UPIIdRequest;
import com.upi.exception.UpiIdAlreadyExisitsException;
import com.upi.model.UpiId;
import com.upi.repository.UpiIdRepository;
import com.users.data.Users;
import com.users.exception.UserNotFoundException;
import com.users.repo.UsersRepository;

@Service
public class UPIIdService {
    private final UpiIdRepository upiIdRepository;
    private final UsersRepository userRepository;

    public UPIIdService(UpiIdRepository upiIdRepository, UsersRepository userRepository) {
        this.upiIdRepository = upiIdRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> createUpiId(String userId, UPIIdRequest request) {
        Map<String, Object> response = new HashMap<>();
        String upiId = request.getUpiId();
        Users user = userRepository.findById(Long.parseLong(userId)).orElse(null);
        if (user == null) {
            throw new UserNotFoundException();
        }
        validateDuplicateUpiId(upiId);

        UpiId newUpiId = UpiId.builder().upiId(upiId).user(user).build();
        if (newUpiId!=null) {
            upiIdRepository.save(newUpiId);   
        }
        response.put("upiId", upiId);
        response.put("status", "created");
        return response;
    }

    private void validateDuplicateUpiId(String upiId) {
        if (upiIdRepository.findByUpiId(upiId) != null) {
            throw new UpiIdAlreadyExisitsException("UPI ID already exists");
        }
    }
}
