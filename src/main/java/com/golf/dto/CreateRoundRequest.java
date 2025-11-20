package com.golf.dto;

import java.time.LocalDate;
import java.util.List;

public class CreateRoundRequest {
    private Long courseId;
    private LocalDate datePlayed;
    private List<HoleScoreDTO> holeScores;
    private String notes;
    
    // Getters and Setters
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
