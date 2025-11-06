package com.golf.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golf.entity.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@Service
public class GolfApiService {
    
    private static final String API_BASE_URL = "https://api.golfcourseapi.com/v1";
    private static final String API_KEY = "3YRM7OUXVN2QX3XCSK2YKOYMWA";
    
    @Autowired
    private CourseService courseService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    

    public List<Course> searchAndImportCourses(String searchQuery) {
        try {
            String url = API_BASE_URL + "/search?search_query=" + searchQuery;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Key " + API_KEY);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            // Parse JSON response
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode coursesNode = root.get("courses");
            
            List<Course> importedCourses = new ArrayList<>();
            
            if (coursesNode != null && coursesNode.isArray()) {
                for (JsonNode courseNode : coursesNode) {
                    Course course = parseCoursFromJson(courseNode);
                    
                    if (course.getName() == null) {
                        System.err.println("Skipping course with null name. Received JSON: " + courseNode.toString());
                        continue; 
                    }
                    
                    if (course.getExternalId() != null && 
                        courseService.getCourseByExternalId(course.getExternalId()).isPresent()) {
                        System.out.println("Course already exists: " + course.getName());
                        continue;
                    }
                    
                    Course savedCourse = courseService.saveCourse(course);
                    importedCourses.add(savedCourse);
                }
            }
            
            return importedCourses;
            
        } catch (Exception e) {
            System.err.println("Error importing courses: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to import courses from API", e);
        }
    }
    
   
    public Course fetchCourseById(Long courseId) {
        try {
            String url = API_BASE_URL + "/courses/" + courseId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Key " + API_KEY);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            JsonNode courseNode = objectMapper.readTree(response.getBody());
            return parseCoursFromJson(courseNode);
            
        } catch (Exception e) {
            System.err.println("Error fetching course: " + e.getMessage());
            throw new RuntimeException("Failed to fetch course from API", e);
        }
    }
    

    private Course parseCoursFromJson(JsonNode node) {
        Course course = new Course();
        
        // Map fields from API response to Course entity
        if (node.has("id")) {
            course.setExternalId(node.get("id").asLong());
        }
        
        if (node.has("course_name")) {
            course.setName(node.get("course_name").asText());
        }
        
        if (node.has("club_name")) {
            course.setClubName(node.get("club_name").asText());
        }
        
        if (node.has("city")) {
            course.setCity(node.get("city").asText());
        }
        
        if (node.has("state_or_province")) {
            course.setState(node.get("state_or_province").asText());
        }
        
        if (node.has("country")) {
            course.setCountry(node.get("country").asText());
        }
        
        if (node.has("zip_code")) {
            course.setZipCode(node.get("zip_code").asText());
        }
        
        if (node.has("latitude")) {
            course.setLatitude(node.get("latitude").asDouble());
        }
        
        if (node.has("longitude")) {
            course.setLongitude(node.get("longitude").asDouble());
        }
        
        if (node.has("phone_number")) {
            course.setPhoneNumber(node.get("phone_number").asText());
        }
        
        if (node.has("website")) {
            course.setWebsite(node.get("website").asText());
        }
        
        if (node.has("num_holes")) {
            course.setNumHoles(node.get("num_holes").asInt());
        }
        
        if (node.has("description")) {
            course.setDescription(node.get("description").asText());
        }
        
        return course;
    }
    
    // Health check for external API
   
    public boolean checkApiHealth() {
        try {
            String url = API_BASE_URL + "/healthcheck";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Key " + API_KEY);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            return false;
        }
    }
}