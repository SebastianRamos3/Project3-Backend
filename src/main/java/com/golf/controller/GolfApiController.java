package com.golf.controller;

import com.golf.entity.Course;
import com.golf.service.GolfApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/golf-api")
public class GolfApiController {
    
    @Autowired
    private GolfApiService golfApiService;
    
  
    @GetMapping("/import")
    public ResponseEntity<Map<String, Object>> importCourses(@RequestParam String search) {
        try {
            List<Course> courses = golfApiService.searchAndImportCourses(search);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imported " + courses.size() + " courses");
            response.put("courses", courses);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to import courses: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    

    @GetMapping("/fetch/{courseId}")
    public ResponseEntity<Course> fetchCourse(@PathVariable Long courseId) {
        try {
            Course course = golfApiService.fetchCourseById(courseId);
            return ResponseEntity.ok(course);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
 
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkApiHealth() {
        boolean isHealthy = golfApiService.checkApiHealth();
        
        Map<String, Object> response = new HashMap<>();
        response.put("external_api_status", isHealthy ? "UP" : "DOWN");
        response.put("message", isHealthy ? "External Golf API is reachable" : "External Golf API is unreachable");
        
        return ResponseEntity.ok(response);
    }
}