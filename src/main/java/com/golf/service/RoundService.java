package com.golf.service;

import com.golf.dto.CreateRoundRequest;
import com.golf.dto.RoundResponse;
import com.golf.entity.Course;
import com.golf.entity.Round;
import com.golf.model.User;
import com.golf.repository.CourseRepository;
import com.golf.repository.RoundRepository;
import com.golf.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoundService {

    private final RoundRepository roundRepo;
    private final CourseRepository courseRepo;
    private final UserRepository userRepo;

    public RoundService(RoundRepository roundRepo, CourseRepository courseRepo, UserRepository userRepo) {
        this.roundRepo = roundRepo;
        this.courseRepo = courseRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public RoundResponse createRound(CreateRoundRequest req, UUID userId) {
        Course course = courseRepo.findById(req.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + req.getCourseId()));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Round r = new Round();
        r.setCourse(course);
        r.setUser(user);
        r.setDatePlayed(req.getDate());
        r.setHoles(req.getHoles().shortValue());
        r.setTotalScore(req.getScore() == null ? null : req.getScore().shortValue());
        r.setNotes(req.getNotes());

        Round saved = roundRepo.save(r);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RoundResponse> getRoundsForUser(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return roundRepo.findByUserOrderByDatePlayedDesc(user)
                .stream().map(this::toResponse).toList();
    }

    private RoundResponse toResponse(Round r) {
        return new RoundResponse(
          r.getId(),
          r.getCourse().getId(),
          r.getCourse().getName(),
          r.getDatePlayed(),
          (int) r.getHoles(),
          r.getTotalScore() == null ? null : (int) r.getTotalScore(),
          r.getNotes()
        );
    }
}
