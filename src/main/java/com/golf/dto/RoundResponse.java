package com.golf.dto;

import java.time.LocalDate;

public class RoundResponse {
    private Long id;
    private Long courseId;
    private String courseName;
    private LocalDate date;
    private Integer holes;
    private Integer score;
    private String notes;

    public RoundResponse() {}

    public RoundResponse(Long id, Long courseId, String courseName, LocalDate date,
                         Integer holes, Integer score, String notes) {
        this.id = id;
        this.courseId = courseId;
        this.courseName = courseName;
        this.date = date;
        this.holes = holes;
        this.score = score;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public LocalDate getDate() { return date; }
    public Integer getHoles() { return holes; }
    public Integer getScore() { return score; }
    public String getNotes() { return notes; }

    public void setId(Long id) { this.id = id; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setHoles(Integer holes) { this.holes = holes; }
    public void setScore(Integer score) { this.score = score; }
    public void setNotes(String notes) { this.notes = notes; }
}
