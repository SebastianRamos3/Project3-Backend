package com.golf.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class CreateRoundRequest {
    @NotNull
    private Long courseId;

    @NotNull
    private LocalDate date;

    @NotNull
    @Min(1) @Max(36)
    private Integer holes;

    private Integer score;   
    private String notes;  

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Integer getHoles() { return holes; }
    public void setHoles(Integer holes) { this.holes = holes; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
