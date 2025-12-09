package com.golf.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreateRoundRequest {
    private UUID userId;
    private Long courseId;
    private LocalDate datePlayed;
    private List<HoleScoreDTO> holeScores;
    private String notes;

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public LocalDate getDatePlayed() {
        return datePlayed;
    }

    public void setDatePlayed(LocalDate datePlayed) {
        this.datePlayed = datePlayed;
    }

    public List<HoleScoreDTO> getHoleScores() {
        return holeScores;
    }

    public void setHoleScores(List<HoleScoreDTO> holeScores) {
        this.holeScores = holeScores;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
