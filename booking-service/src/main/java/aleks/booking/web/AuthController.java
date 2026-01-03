package aleks.booking.web;

import aleks.booking.dto.*;
import aleks.booking.security.JwtService;
import aleks.booking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class AuthController {

    private final UserService usersService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid RegisterRequest req) {
        var user = usersService.registerUser(req.username(), req.password());
        return new AuthResponse(jwtService.issueToken(user.getUsername(), user.getRole()));
    }

    @PostMapping("/auth")
    public AuthResponse auth(@RequestBody @Valid AuthRequest req) {
        var user = usersService.authenticate(req.username(), req.password());
        return new AuthResponse(jwtService.issueToken(user.getUsername(), user.getRole()));
    }
}
