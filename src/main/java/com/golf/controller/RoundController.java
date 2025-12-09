package com.golf.controller;

import com.golf.dto.CreateRoundRequest;
import com.golf.dto.HoleScoreDTO;
import com.golf.dto.RoundResponse;
import com.golf.entity.Round;
import com.golf.entity.HoleScore;
import com.golf.service.RoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rounds")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "*"})
public class RoundController {
    
    @Autowired
    private RoundService roundService;
    
    @PostMapping
    public ResponseEntity<RoundResponse> createRound(@RequestBody CreateRoundRequest request) {
        
        // Get userId from the request body instead of query param
        UUID userId = request.getUserId();
        
        // Convert DTOs to entities
        List<HoleScore> holeScores = request.getHoleScores().stream()
            .map(dto -> {
                HoleScore hs = new HoleScore();
                hs.setHoleNumber(dto.getHoleNumber());
                hs.setStrokes(dto.getStrokes());
                hs.setPar(dto.getPar());
                return hs;
            })
            .collect(Collectors.toList());
        
        Round round = roundService.createRound(
            userId,
            request.getCourseId(),
            request.getDatePlayed(),
            holeScores,
            request.getNotes()
        );
        
        RoundResponse response = mapToResponse(round);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RoundResponse>> getUserRounds(@PathVariable UUID userId) {
        List<Round> rounds = roundService.getUserRounds(userId);
        List<RoundResponse> responses = rounds.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RoundResponse> getRound(@PathVariable Long id) {
        return roundService.getRoundById(id)
            .map(round -> ResponseEntity.ok(mapToResponse(round)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRound(@PathVariable Long id) {
        roundService.deleteRound(id);
        return ResponseEntity.noContent().build();
    }
    
    private RoundResponse mapToResponse(Round round) {
        RoundResponse response = new RoundResponse();
        response.setId(round.getId());
        response.setUserId(round.getUser().getId());
        response.setUserName(round.getUser().getName());
        response.setCourseId(round.getCourse().getId());
        response.setCourseName(round.getCourse().getName());
        response.setDatePlayed(round.getDatePlayed());
        response.setTotalStrokes(round.getTotalStrokes());
        response.setNotes(round.getNotes());
        
        List<HoleScoreDTO> holeScoreDTOs = round.getHoleScores().stream()
            .map(hs -> {
                HoleScoreDTO dto = new HoleScoreDTO();
                dto.setHoleNumber(hs.getHoleNumber());
                dto.setStrokes(hs.getStrokes());
                dto.setPar(hs.getPar());
                return dto;
            })
            .collect(Collectors.toList());
        response.setHoleScores(holeScoreDTOs);
        
        return response;
    }
}
