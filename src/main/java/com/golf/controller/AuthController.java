package com.golf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.golf.dto.SignUpRequest;
import com.golf.dto.LoginRequest;
import com.golf.dto.UserResponse;
import com.golf.entity.User;
import com.golf.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173", "*" })
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        // Handle OAuth2 authentication
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            String email = (String) oauthToken.getPrincipal().getAttributes().get("email");

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
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");

        if (idToken == null || idToken.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "ID token is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        try {
            // Verify token with Google and get user info
            // Using Google's tokeninfo endpoint for simplicity
            String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(tokenInfoUrl))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(httpRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid Google token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Parse the response JSON
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenInfo = (Map<String, Object>) mapper.readValue(response.body(), Map.class);

            String email = (String) tokenInfo.get("email");
            String googleId = (String) tokenInfo.get("sub");
            String firstName = (String) tokenInfo.get("given_name");
            String lastName = (String) tokenInfo.get("family_name");
            String picture = (String) tokenInfo.get("picture");

            if (email == null || googleId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid token response from Google");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Find or create user
            User user = userRepository.findByGoogleId(googleId)
                    .orElseGet(() -> {
                        User newUser = userRepository.findByEmail(email).orElse(new User());
                        newUser.setEmail(email);
                        return newUser;
                    });

            user.setEmail(email);
            user.setGoogleId(googleId);
            user.setProvider("google");
            if (firstName != null) {
                user.setFirstName(firstName);
            }
            if (lastName != null) {
                user.setLastName(lastName);
            }
            if (picture != null) {
                user.setProfilePictureUrl(picture);
            }
            if (user.getUsername() == null) {
                // Generate username from email
                String baseUsername = email.split("@")[0];
                String username = baseUsername;
                int counter = 1;
                while (userRepository.existsByUsername(username)) {
                    username = baseUsername + counter;
                    counter++;
                }
                user.setUsername(username);
            }
            if (user.getName() == null) {
                if (firstName != null && lastName != null) {
                    user.setName(firstName + " " + lastName);
                } else if (firstName != null) {
                    user.setName(firstName);
                } else {
                    user.setName(email.split("@")[0]);
                }
            }

            userRepository.save(user);

            UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail());

            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to verify Google token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}