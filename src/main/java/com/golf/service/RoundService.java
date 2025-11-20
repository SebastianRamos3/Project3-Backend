package com.golf.service;

import com.golf.entity.Round;
import com.golf.entity.HoleScore;
import com.golf.entity.User;
import com.golf.entity.Course;
import com.golf.repository.RoundRepository;
import com.golf.repository.UserRepository;
import com.golf.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoundService {
    
    @Autowired
    private RoundRepository roundRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Transactional
    public Round createRound(UUID userId, Long courseId, LocalDate datePlayed, 
                             List<HoleScore> holeScores, String notes) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        Round round = new Round();
        round.setUser(user);
        round.setCourse(course);
        round.setDatePlayed(datePlayed != null ? datePlayed : LocalDate.now());
        round.setNotes(notes);
        
        // Calculate total strokes
        int totalStrokes = holeScores.stream()
            .mapToInt(HoleScore::getStrokes)
            .sum();
        round.setTotalStrokes(totalStrokes);
        
        // Link hole scores to round
        for (HoleScore holeScore : holeScores) {
            holeScore.setRound(round);
        }
        round.setHoleScores(holeScores);
        
        return roundRepository.save(round);
    }
    
    public List<Round> getUserRounds(UUID userId) {
        return roundRepository.findByUserIdOrderByDatePlayedDesc(userId);
    }
    
    public List<Round> getCourseRounds(Long courseId) {
        return roundRepository.findByCourseIdOrderByDatePlayedDesc(courseId);
    }
    
    public Optional<Round> getRoundById(Long roundId) {
        return roundRepository.findById(roundId);
    }
    
    public void deleteRound(Long roundId) {
        roundRepository.deleteById(roundId);
    }
}
