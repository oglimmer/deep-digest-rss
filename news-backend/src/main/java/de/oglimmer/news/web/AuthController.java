package de.oglimmer.news.web;

import de.oglimmer.news.db.User;
import de.oglimmer.news.service.UserService;
import de.oglimmer.news.web.dto.AuthResponse;
import de.oglimmer.news.web.dto.AuthenticateUserDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    public static final String AUTH = "auth";

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthenticateUserDto authenticateUserDto) {
        User user = userService.authenticateUser(authenticateUserDto.getEmail(), authenticateUserDto.getPassword());
        if (user != null) {
            AuthResponse authResponse = new AuthResponse();
            authResponse.setAuthToken(user.getAuthToken());
            return ResponseEntity.ok(authResponse);
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody AuthenticateUserDto authenticateUserDto) {
        userService.registerUser(authenticateUserDto.getEmail(), authenticateUserDto.getPassword());
        return ResponseEntity.ok().build();
    }
}
