package com.sivalabs.bookstore.users.web;

import static org.springframework.http.HttpStatus.CREATED;

import com.sivalabs.bookstore.users.domain.CreateUserCmd;
import com.sivalabs.bookstore.users.domain.JwtTokenHelper;
import com.sivalabs.bookstore.users.domain.Role;
import com.sivalabs.bookstore.users.domain.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class UserRestController {
    private static final Logger log = LoggerFactory.getLogger(UserRestController.class);
    private final AuthenticationManager authManager;
    private final UserService userService;
    private final JwtTokenHelper jwtTokenHelper;

    UserRestController(AuthenticationManager authManager, UserService userService, JwtTokenHelper jwtTokenHelper) {
        this.authManager = authManager;
        this.userService = userService;
        this.jwtTokenHelper = jwtTokenHelper;
    }

    @PostMapping("/api/login")
    LoginResponse login(@RequestBody @Valid LoginRequest req) {
        log.info("Login request for email: {}", req.email());
        var user = userService
                .findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        var authentication = new UsernamePasswordAuthenticationToken(req.email(), req.password());
        authManager.authenticate(authentication);
        var jwtToken = jwtTokenHelper.generateToken(user);
        return new LoginResponse(
                jwtToken.token(),
                jwtToken.expiresAt(),
                user.name(),
                user.email(),
                user.role().name());
    }

    public record LoginRequest(
            @NotEmpty(message = "Email is required") @Email(message = "Invalid email address") String email,
            @NotEmpty(message = "Password is required") String password) {}

    public record LoginResponse(String token, Instant expiresAt, String name, String email, String role) {}

    @PostMapping("/api/users")
    ResponseEntity<RegistrationResponse> createUser(@RequestBody @Valid RegistrationRequest req) {
        log.info("Registration request for email: {}", req.email());
        var cmd = new CreateUserCmd(req.name(), req.email(), req.password(), Role.ROLE_USER);
        userService.createUser(cmd);
        var response = new RegistrationResponse(req.name(), req.email(), Role.ROLE_USER);
        return ResponseEntity.status(CREATED.value()).body(response);
    }

    public record RegistrationRequest(
            @NotBlank(message = "Name is required") String name,
            @NotBlank(message = "Email is required") @Email(message = "Invalid email address") String email,
            @NotBlank(message = "Password is required") String password) {}

    public record RegistrationResponse(String name, String email, Role role) {}
}
