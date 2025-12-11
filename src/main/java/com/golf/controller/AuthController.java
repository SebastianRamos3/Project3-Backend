package com.golf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.golf.dto.SignUpRequest;
import com.golf.dto.LoginRequest;
import com.golf.dto.GoogleSignInRequest;
import com.golf.dto.UserResponse;
import com.golf.entity.User;
import com.golf.repository.UserRepository;
import com.golf.service.GoogleAuthService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173", "*" })
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GoogleAuthService googleAuthService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email already registered");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Create new user
        User user = new User();

        // Set name - use provided name, or use email prefix if name is empty
        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
            user.setUsername(request.getName());
        } else {
            // Use email prefix as name if no name provided
            String nameFromEmail = request.getEmail().split("@")[0];
            user.setName(nameFromEmail);
            user.setUsername(nameFromEmail);
        }

        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encrypt password
        user.setProvider("LOCAL"); // Set provider for local signup

        User savedUser = userRepository.save(user);

        UserResponse response = new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Use password encoder to verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody SignUpRequest request) {
        // Alias for signup - some frontends might use /register instead
        return signup(request);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Handle regular authentication (email-based)
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-by-email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName() != null ? user.getName()
                        : (user.getFirstName() != null
                                ? user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "")
                                : user.getEmail()),
                user.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<?> signInWithGoogle(@RequestBody GoogleSignInRequest request) {
        if (request.getIdToken() == null || request.getIdToken().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "ID token is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Verify the Google ID token
        GoogleAuthService.GoogleUserInfo googleUser = googleAuthService.verifyIdToken(request.getIdToken());

        if (googleUser == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid Google ID token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Check if user already exists
        User user = userRepository.findByEmail(googleUser.getEmail()).orElse(null);

        if (user == null) {
            // Create new user
            user = new User();
            user.setEmail(googleUser.getEmail());
            user.setName(googleUser.getName());
            user.setUsername(googleUser.getEmail().split("@")[0]);

            if (googleUser.getGivenName() != null) {
                user.setFirstName(googleUser.getGivenName());
            }
            if (googleUser.getFamilyName() != null) {
                user.setLastName(googleUser.getFamilyName());
            }
            if (googleUser.getPicture() != null) {
                user.setProfilePictureUrl(googleUser.getPicture());
            }
            if (googleUser.getGoogleId() != null) {
                user.setGoogleId(googleUser.getGoogleId());
            }

            user.setProvider("GOOGLE");
            // Google OAuth users don't need a password - leave it null

            user = userRepository.save(user);
        } else {
            // Update existing user if needed (e.g., profile picture)
            if (googleUser.getPicture() != null && !googleUser.getPicture().equals(user.getProfilePictureUrl())) {
                user.setProfilePictureUrl(googleUser.getPicture());
            }
            if (googleUser.getName() != null && !googleUser.getName().equals(user.getName())) {
                user.setName(googleUser.getName());
            }
            user = userRepository.save(user);
        }

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail());

        return ResponseEntity.ok(response);
    }

}