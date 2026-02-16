package com.upi.service;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.upi.dto.PinRequest;
import com.upi.dto.PinResponse;
import com.upi.exception.InvalidPinFoundException;
import com.upi.exception.PinExistsException;
import com.upi.model.UpiPin;
import com.upi.repository.UpiPinRepository;
import com.users.data.Users;
import com.users.repo.UsersRepository;
import com.users.exception.UserNotFoundException;

@Service
@Transactional
public class UPIPinService {
    private static final String PIN_PATTERN = "\\d{4,6}";

    private final UpiPinRepository upiPinRepository;
    private final UsersRepository userRepository;

    public UPIPinService(UpiPinRepository upiPinRepository, UsersRepository userRepository) {
        this.upiPinRepository = upiPinRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new UPI PIN for a user.
     * @param request The request containing user ID, new PIN, and confirmation PIN.
     * @return A response indicating the status of the PIN creation.
     * @throws UserNotFoundException if the user does not exist.
     * @throws PinExistsException if a PIN already exists for the user.
     * @throws InvalidPinFoundException if the PIN format is invalid or PINs do not match.
     */
    public PinResponse createPin(PinRequest request) {
        Users user = userRepository
                .findById(Long.parseLong(request.getUserId()))
                .orElseThrow(UserNotFoundException::new);

        if (upiPinRepository.findByUserId(user.getId()).isPresent()) {
            throw new PinExistsException();
        }

        if (request.getPin() == null || !request.getPin().matches(PIN_PATTERN)) {
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

    /**
     * Changes an existing UPI PIN for a user.
     * @param request The request containing user ID, old PIN, new PIN, and confirmation PIN.
     * @return A response indicating the status of the PIN change.
     * @throws UserNotFoundException if the user does not exist.
     * @throws InvalidPinFoundException if the new PIN format is invalid, PINs do not match, old PIN is invalid, or new PIN is same as old PIN.
     */
    public PinResponse changePin(PinRequest request) {
        String userId = request.getUserId();
        String oldPin = request.getOldPin();
        String newPin = request.getPin();

        Users user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(UserNotFoundException::new);

        if (newPin == null || !newPin.matches(PIN_PATTERN)) {
            throw new InvalidPinFoundException("PIN must be 4 to 6 digits");
        }
        if (!newPin.equals(request.getConfirmPin())) {
            throw new InvalidPinFoundException("New PIN and Confirm PIN do not match");
        }

        UpiPin pin = upiPinRepository.findByUserId(user.getId()).orElse(null);
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

    /**
     * Resets a UPI PIN for a user. This typically implies the user has forgotten the old PIN.
     * @param request The request containing user ID, new PIN, and confirmation PIN.
     * @return A response indicating the status of the PIN reset.
     * @throws UserNotFoundException if the user does not exist.
     * @throws InvalidPinFoundException if the new PIN format is invalid or PINs do not match.
     */
    public PinResponse resetPin(PinRequest request) {
        String userId = request.getUserId();
        String newPin = request.getPin();

        Users user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(UserNotFoundException::new);

        if (newPin == null || !newPin.matches(PIN_PATTERN)) {
            throw new InvalidPinFoundException("PIN must be 4 to 6 digits");
        }
        if (!newPin.equals(request.getConfirmPin())) {
            throw new InvalidPinFoundException("New PIN and Confirm PIN do not match");
        }

        UpiPin pin = upiPinRepository.findByUserId(user.getId()).orElse(null);
        if (pin == null) { // If no PIN exists, it's effectively a create operation or an error depending on business logic
            // For reset, we assume a PIN should exist. If not, it's an invalid state for a reset.
            throw new InvalidPinFoundException("No existing PIN found to reset for user ID " + userId);
        }
        String hashed = BCrypt.hashpw(newPin, BCrypt.gensalt());
        pin.setPinHash(hashed);
        upiPinRepository.save(pin);
        return PinResponse.builder()
                .status("reset")
                .message("PIN reset successfully")
                .build();
    }

    /**
     * Verifies if the provided PIN matches the stored hashed PIN for a user.
     * @param request The request containing user ID and PIN to verify.
     * @return True if the PIN is valid, false otherwise.
     * @throws UserNotFoundException if the user does not exist.
     * @throws InvalidPinFoundException if the PIN format is invalid.
     */
    public boolean verifyPin(PinRequest request) {
        String userId = request.getUserId();
        String pin = request.getPin();

        if (pin == null || !pin.matches(PIN_PATTERN)) {
            throw new InvalidPinFoundException("PIN must be 4 to 6 digits");
        }

        Users user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(UserNotFoundException::new);

        UpiPin upiPin = upiPinRepository.findByUserId(user.getId()).orElse(null);

        return upiPin != null && BCrypt.checkpw(pin, upiPin.getPinHash());
    }
}
