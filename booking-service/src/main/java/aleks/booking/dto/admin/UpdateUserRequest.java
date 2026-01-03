package aleks.booking.dto.admin;

import aleks.booking.domain.enums.Role;

public record UpdateUserRequest(
        String password,
        Role role
) {}

