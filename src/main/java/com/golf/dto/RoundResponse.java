package com.golf.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class RoundResponse {
    private Long id;
    private UUID userId;
    private String userName;
    private Long courseId;
    private String courseName;
    private LocalDate datePlayed;
    private Integer totalStrokes;
    private String notes;
    private List<HoleScoreDTO> holeScores;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public LocalDate getDatePlayed() {
        return datePlayed;
    }
    
    public void setDatePlayed(LocalDate datePlayed) {
        this.datePlayed = datePlayed;
    }
    
    public Integer getTotalStrokes() {
        return totalStrokes;
    }
    
    public void setTotalStrokes(Integer totalStrokes) {
        this.totalStrokes = totalStrokes;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<HoleScoreDTO> getHoleScores() {
        return holeScores;
    }
    
    public void setHoleScores(List<HoleScoreDTO> holeScores) {
        this.holeScores = holeScores;
    }
}