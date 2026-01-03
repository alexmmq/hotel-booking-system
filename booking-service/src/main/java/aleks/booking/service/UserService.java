package aleks.booking.service;

import aleks.booking.domain.UserEntity;
import aleks.booking.domain.enums.Role;
import aleks.booking.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Transactional
    public UserEntity registerUser(String username, String rawPassword) {
        if (userRepo.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "USERNAME_TAKEN");
        }
        var u = new UserEntity(null, username, encoder.encode(rawPassword), Role.USER);
        return userRepo.save(u);
    }

    public UserEntity authenticate(String username, String rawPassword) {
        var u = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS"));
        if (!encoder.matches(rawPassword, u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS");
        }
        return u;
    }

    @Transactional
    public UserEntity adminCreate(String username, String rawPassword, Role role) {
        if (userRepo.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "USERNAME_TAKEN");
        }
        return userRepo.save(new UserEntity(null, username, encoder.encode(rawPassword), role));
    }

    @Transactional
    public UserEntity adminUpdate(Long id, String maybePassword, Role maybeRole) {
        var u = userRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (maybePassword != null && !maybePassword.isBlank()) {
            u.setPasswordHash(encoder.encode(maybePassword));
        }
        if (maybeRole != null) {
            u.setRole(maybeRole);
        }
        return u;
    }

    @Transactional
    public void adminDelete(Long id) {
        userRepo.deleteById(id);
    }
}

