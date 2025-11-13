package com.golf.repository;

import com.golf.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByExternalId(Long externalId);
    List<Course> findByNameContainingIgnoreCase(String name);
    List<Course> findByCity(String city);
    List<Course> findByState(String state);
}