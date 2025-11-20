package com.golf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.golf.dto.SignUpRequest;
import com.golf.dto.LoginRequest;
import com.golf.dto.UserResponse;
import com.golf.entity.User;
import com.golf.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "*"})
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
            savedUser.getEmail()
        );
        
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
            user.getEmail()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody SignUpRequest request) {
        // Alias for signup - some frontends might use /register instead
        return signup(request);
    }
}