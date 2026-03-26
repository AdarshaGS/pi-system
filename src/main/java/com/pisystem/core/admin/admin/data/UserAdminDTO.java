package com.admin.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

/**
 * DTO for admin view of user information (excludes sensitive data like password)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAdminDTO {
    private Long id;
    private String name;
    private String email;
    private String mobileNumber;
    private Set<String> roles;
}
