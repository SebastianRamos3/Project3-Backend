package com.golf.repository;

import com.golf.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
    List<Round> findByUserIdOrderByDatePlayedDesc(UUID userId);  // Changed to UUID
    List<Round> findByCourseIdOrderByDatePlayedDesc(Long courseId);
}