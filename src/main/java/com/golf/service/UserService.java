package com.golf.service;

import com.golf.entity.User;
import com.golf.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; 

    public User registerUser(User user) throws Exception {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new Exception("Username already exists: " + user.getUsername());
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }
}