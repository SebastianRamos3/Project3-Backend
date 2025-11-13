package com.golf.entity;

import com.golf.model.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "rounds")
public class Round {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course; // com.golf.entity.Course (Long PK)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "uuid")
    private User user;     // com.golf.model.User (UUID PK)

    @Column(name = "date_played", nullable = false)
    private LocalDate datePlayed;

    @Column(nullable = false)
    private Short holes;

    @Column(name = "total_score")
    private Short totalScore;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // getters/setters
    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getDatePlayed() { return datePlayed; }
    public void setDatePlayed(LocalDate datePlayed) { this.datePlayed = datePlayed; }
    public Short getHoles() { return holes; }
    public void setHoles(Short holes) { this.holes = holes; }
    public Short getTotalScore() { return totalScore; }
    public void setTotalScore(Short totalScore) { this.totalScore = totalScore; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
