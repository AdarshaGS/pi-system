package com.upi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.upi.dto.UPIIdRequest;
import com.upi.dto.UpiIdCreationResponse;
import com.upi.exception.UpiIdAlreadyExisitsException;
import com.upi.model.UpiId;
import com.upi.repository.UpiIdRepository;
import com.users.data.Users;
import com.users.exception.UserNotFoundException;
import com.users.repo.UsersRepository;

import java.util.List;

@Service
@Transactional
public class UPIIdService {
    private final UpiIdRepository upiIdRepository;
    private final UsersRepository userRepository;

    // Using constructor injection is preferred over @Autowired on fields
    public UPIIdService(UpiIdRepository upiIdRepository, UsersRepository userRepository) {
        this.upiIdRepository = upiIdRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new UPI ID for a given user.
     * @param userId The ID of the user.
     * @param request The request containing the desired UPI ID.
     * @return A response indicating the status of the UPI ID creation.
     * @throws UserNotFoundException if the specified user does not exist.
     * @throws UpiIdAlreadyExisitsException if the requested UPI ID already exists.
     */
    public UpiIdCreationResponse createUpiId(String userId, UPIIdRequest request) {
        String upiId = request.getUpiId();
        
        Users user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));
        
        validateDuplicateUpiId(upiId);

        UpiId newUpiId = UpiId.builder().upiId(upiId).user(user).build();
        upiIdRepository.save(newUpiId); // newUpiId will never be null here, so the check is redundant
        
        return UpiIdCreationResponse.builder()
                .upiId(upiId)
                .status("created")
                .message("UPI ID created successfully.")
                .build();
    }

    /**
     * Validates if a UPI ID already exists in the system.
     * @param upiId The UPI ID to validate.
     * @throws UpiIdAlreadyExisitsException if the UPI ID already exists.
     */
    private void validateDuplicateUpiId(String upiId) {
        if (upiIdRepository.findByUpiId(upiId).isPresent()) {
            throw new UpiIdAlreadyExisitsException("UPI ID already exists");
        }
    }

    public List<UpiId> getUpiIds(String userId) {
        return upiIdRepository.findByUserId(Long.parseLong(userId));
    }
}
