package com.upi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.upi.dto.PinRequest;
import com.upi.dto.PinResponse;
import com.upi.exception.InvalidPinFoundException;
import com.upi.model.UpiPin;
import com.upi.repository.UpiPinRepository;
import com.users.data.Users;
import com.users.repo.UsersRepository;
import com.users.exception.UserNotFoundException;

@Service
@Transactional
public class UPIPinService {
    @Autowired
    private UpiPinRepository upiPinRepository;
    @Autowired
    private UsersRepository userRepository;

    public PinResponse createPin(PinRequest request) {
        Users user = userRepository
                .findById(Long.parseLong(request.getUserId()))
                .orElseThrow(UserNotFoundException::new);

        if (upiPinRepository.findByUserId(user.getId()) != null) {
            return PinResponse.builder()
                    .status("failed")
                    .message("PIN already exists")
                    .build();
        }

        if (request.getPin() == null || !request.getPin().matches("\\d{4,6}")) {
            throw new InvalidPinFoundException("PIN must be 4 to 6 digits");
        }

        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new InvalidPinFoundException();
        }

        String hashed = BCrypt.hashpw(request.getPin(), BCrypt.gensalt());

        UpiPin pin = new UpiPin();
        pin.setUser(user);
        pin.setPinHash(hashed);

        upiPinRepository.save(pin);

        return PinResponse.builder()
                .status("created")
                .message("PIN created successfully")
                .build();
    }

    public PinResponse changePin(PinRequest request) {
        String userId = request.getUserId();
        String oldPin = request.getOldPin();
        String newPin = request.getPin();

        Users user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(UserNotFoundException::new);

        if (newPin == null || !newPin.matches("\\d{4,6}")) {
            throw new InvalidPinFoundException("PIN must be 4 to 6 digits");
        }
        if (!newPin.equals(request.getConfirmPin())) {
            throw new InvalidPinFoundException("New PIN and Confirm PIN do not match");
        }

        UpiPin pin = upiPinRepository.findByUserId(user.getId());
        if (pin == null || !BCrypt.checkpw(oldPin, pin.getPinHash())) {
            throw new InvalidPinFoundException("Invalid old PIN");
        }
        if (oldPin.equals(newPin)) {
            throw new InvalidPinFoundException("New PIN cannot be the same as the old PIN");
        }

        String hashed = BCrypt.hashpw(newPin, BCrypt.gensalt());
        pin.setPinHash(hashed);
        upiPinRepository.save(pin);
        return PinResponse.builder()
                .status("changed")
                .message("PIN changed successfully")
                .build();
    }

    public PinResponse resetPin(PinRequest request) {
        String userId = request.getUserId();
        String newPin = request.getPin();

        Users user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(UserNotFoundException::new);

        if (newPin == null || !newPin.matches("\\d{4,6}")) {
            throw new InvalidPinFoundException("PIN must be 4 to 6 digits");
        }
        if (!newPin.equals(request.getConfirmPin())) {
            throw new InvalidPinFoundException("New PIN and Confirm PIN do not match");
        }

        UpiPin pin = upiPinRepository.findByUserId(user.getId());
        if (pin == null) {
            return PinResponse.builder()
                    .status("failed")
                    .message("PIN not found")
                    .build();
        }
        String hashed = BCrypt.hashpw(newPin, BCrypt.gensalt());
        pin.setPinHash(hashed);
        upiPinRepository.save(pin);
        return PinResponse.builder()
                .status("reset")
                .message("PIN reset successfully")
                .build();
    }

    public boolean verifyPin(PinRequest request) {
        String userId = request.getUserId();
        String pin = request.getPin();

        if (pin == null || !pin.matches("\\d{4,6}")) {
            throw new InvalidPinFoundException("PIN must be 4 to 6 digits");
        }

        Users user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(UserNotFoundException::new);

        UpiPin upiPin = upiPinRepository.findByUserId(user.getId());

        return upiPin != null && BCrypt.checkpw(pin, upiPin.getPinHash());
    }
}
