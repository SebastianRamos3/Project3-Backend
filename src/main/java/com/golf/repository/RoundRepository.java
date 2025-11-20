package com.golf.repository;

import com.golf.entity.Round;
import com.golf.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoundRepository extends JpaRepository<Round, Long> {
    List<Round> findByUserOrderByDatePlayedDesc(User user);
}
