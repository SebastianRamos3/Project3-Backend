package com.golf.controller;

import com.golf.entity.Course;
import com.golf.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    // GET all courses
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }
    
    // GET course by ID
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // GET course by external ID (from golf API)
    @GetMapping("/external/{externalId}")
    public ResponseEntity<Course> getCourseByExternalId(@PathVariable Long externalId) {
        return courseService.getCourseByExternalId(externalId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // SEARCH courses by name
    @GetMapping("/search")
    public ResponseEntity<List<Course>> searchCourses(@RequestParam String name) {
        List<Course> courses = courseService.searchCoursesByName(name);
        return ResponseEntity.ok(courses);
    }
    
    // GET courses by city
    @GetMapping("/city/{city}")
    public ResponseEntity<List<Course>> getCoursesByCity(@PathVariable String city) {
        List<Course> courses = courseService.getCoursesByCity(city);
        return ResponseEntity.ok(courses);
    }
    
    // GET courses by state
    @GetMapping("/state/{state}")
    public ResponseEntity<List<Course>> getCoursesByState(@PathVariable String state) {
        List<Course> courses = courseService.getCoursesByState(state);
        return ResponseEntity.ok(courses);
    }
    
    // POST create new course
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Course savedCourse = courseService.saveCourse(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCourse);
    }
    
    // PUT update course
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course courseDetails) {
        try {
            Course updatedCourse = courseService.updateCourse(id, courseDetails);
            return ResponseEntity.ok(updatedCourse);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // DELETE course
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
    
    // GET courses count
    @GetMapping("/count")
    public ResponseEntity<Long> getCoursesCount() {
        long count = courseService.getCoursesCount();
        return ResponseEntity.ok(count);
    }
}