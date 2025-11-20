package com.golf.repository;

import com.golf.entity.HoleScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoleScoreRepository extends JpaRepository<HoleScore, Long> {
}