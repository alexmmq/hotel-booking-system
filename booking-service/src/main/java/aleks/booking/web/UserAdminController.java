package aleks.booking.web;

import aleks.booking.domain.UserEntity;
import aleks.booking.dto.admin.CreateUserRequest;
import aleks.booking.dto.admin.UpdateUserRequest;
import aleks.booking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserAdminController {

    private final UserService usersService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserEntity create(@RequestBody @Valid CreateUserRequest req) {
        return usersService.adminCreate(req.username(), req.password(), req.role());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserEntity update(@PathVariable Long id, @RequestBody UpdateUserRequest req) {
        return usersService.adminUpdate(id, req.password(), req.role());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        usersService.adminDelete(id);
    }
}
