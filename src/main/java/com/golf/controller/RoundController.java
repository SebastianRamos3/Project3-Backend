package com.golf.controller;

import com.golf.dto.CreateRoundRequest;
import com.golf.dto.RoundResponse;
import com.golf.model.User;
import com.golf.repository.UserRepository;
import com.golf.service.CurrentUser;
import com.golf.service.RoundService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class RoundController {

    private final RoundService roundService;
    private final CurrentUser currentUser;
    private final UserRepository userRepo;

    public RoundController(RoundService roundService, CurrentUser currentUser, UserRepository userRepo) {
        this.roundService = roundService;
        this.currentUser = currentUser;
        this.userRepo = userRepo;
    }

    @PostMapping
    public ResponseEntity<RoundResponse> create(@Valid @RequestBody CreateRoundRequest req,
                                                @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {
        String email = currentUser.email();
        if ((email == null || email.isBlank()) && emailHeader != null && !emailHeader.isBlank()) {
            email = emailHeader; // handy for local testing without auth
        }
        if (email == null || email.isBlank()) return ResponseEntity.status(401).build();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found for " + email));

        return ResponseEntity.ok(roundService.createRound(req, user.getId()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<RoundResponse>> myRounds(
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {
        String email = currentUser.email();
        if ((email == null || email.isBlank()) && emailHeader != null && !emailHeader.isBlank()) {
            email = emailHeader;
        }
        if (email == null || email.isBlank()) return ResponseEntity.status(401).build();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found for " + email));

        return ResponseEntity.ok(roundService.getRoundsForUser(user.getId()));
    }
}
