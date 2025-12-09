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
    private static final String API_KEY = "U2JKSS32ZWVIWMLF3WFZH3DLEM";
    
    @Autowired
    private CourseService courseService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Search and import courses - TWO STEP PROCESS:
     * 1. Search to get course IDs
     * 2. Fetch each course by ID to get full details
     */
    public List<Course> searchAndImportCourses(String searchQuery) {
        try {
            // Step 1: Search for courses (gets basic info + IDs)
            String searchUrl = API_BASE_URL + "/search?search_query=" + searchQuery;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Key " + API_KEY);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, String.class);
            
            System.out.println("Search API Response: " + searchResponse.getBody());
            
            JsonNode root = objectMapper.readTree(searchResponse.getBody());
            JsonNode coursesNode = root.get("courses");
            
            List<Course> importedCourses = new ArrayList<>();
            
            if (coursesNode != null && coursesNode.isArray()) {
                for (JsonNode courseNode : coursesNode) {
                    if (!courseNode.has("id")) {
                        System.err.println("Course missing ID, skipping");
                        continue;
                    }
                    
                    Long externalId = courseNode.get("id").asLong();
                    
                    // Check if already exists
                    if (courseService.getCourseByExternalId(externalId).isPresent()) {
                        System.out.println("Course already exists with ID: " + externalId);
                        continue;
                    }
                    
                    // Step 2: Fetch full course details by ID
                    try {
                        Course fullCourse = fetchCourseById(externalId);
                        
                        if (fullCourse.getName() == null) {
                            System.err.println("Course has null name, skipping");
                            continue;
                        }
                        
                        Course savedCourse = courseService.saveCourse(fullCourse);
                        importedCourses.add(savedCourse);
                        System.out.println("✅ Imported: " + savedCourse.getName() + " (ID: " + savedCourse.getExternalId() + ")");
                        
                    } catch (Exception e) {
                        System.err.println("Failed to fetch course " + externalId + ": " + e.getMessage());
                        // Continue with next course
                    }
                }
            }
            
            System.out.println("Total imported: " + importedCourses.size() + " courses");
            return importedCourses;
            
        } catch (Exception e) {
            System.err.println("Error importing courses: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to import courses from API", e);
        }
    }
    
    /**
     * Fetch a single course by ID from external API
     * This gets the FULL course details including location
     */
    public Course fetchCourseById(Long courseId) {
        try {
            String url = API_BASE_URL + "/courses/" + courseId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Key " + API_KEY);
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            System.out.println("Course detail response for ID " + courseId + ": " + response.getBody());
            
            JsonNode root = objectMapper.readTree(response.getBody());
            
            // ✅ FIX: Unwrap the "course" object
            JsonNode courseNode = root.get("course");
            if (courseNode == null) {
                throw new RuntimeException("No course data found in response");
            }
            
            return parseCoursFromJson(courseNode);
            
        } catch (Exception e) {
            System.err.println("Error fetching course " + courseId + ": " + e.getMessage());
            throw new RuntimeException("Failed to fetch course from API", e);
        }
    }
    
    /**
     * Parse course from JSON - handles the nested location object
     */
    private Course parseCoursFromJson(JsonNode node) {
        Course course = new Course();
        
        // External ID
        if (node.has("id")) {
            course.setExternalId(node.get("id").asLong());
        }
        
        // Course name and club name
        if (node.has("course_name") && !node.get("course_name").isNull()) {
            course.setName(node.get("course_name").asText());
        }
        
        if (node.has("club_name") && !node.get("club_name").isNull()) {
            course.setClubName(node.get("club_name").asText());
        }
        
        // Location is nested in a "location" object
        if (node.has("location")) {
            JsonNode location = node.get("location");
            
            if (location.has("city") && !location.get("city").isNull()) {
                course.setCity(location.get("city").asText());
            }
            
            if (location.has("state") && !location.get("state").isNull()) {
                course.setState(location.get("state").asText());
            }
            
            if (location.has("country") && !location.get("country").isNull()) {
                course.setCountry(location.get("country").asText());
            }
            
            if (location.has("latitude") && !location.get("latitude").isNull()) {
                course.setLatitude(location.get("latitude").asDouble());
            }
            
            if (location.has("longitude") && !location.get("longitude").isNull()) {
                course.setLongitude(location.get("longitude").asDouble());
            }
        }
        
        // Direct fields
        if (node.has("phone_number") && !node.get("phone_number").isNull()) {
            course.setPhoneNumber(node.get("phone_number").asText());
        }
        
        if (node.has("website") && !node.get("website").isNull()) {
            course.setWebsite(node.get("website").asText());
        }
        
        // Calculate number of holes from tees data
        if (node.has("tees")) {
            JsonNode tees = node.get("tees");
            
            // Try male tees first
            if (tees.has("male") && tees.get("male").isArray() && tees.get("male").size() > 0) {
                JsonNode firstTee = tees.get("male").get(0);
                if (firstTee.has("holes") && firstTee.get("holes").isArray()) {
                    course.setNumHoles(firstTee.get("holes").size());
                }
            } 
            // Try female tees if male not available
            else if (tees.has("female") && tees.get("female").isArray() && tees.get("female").size() > 0) {
                JsonNode firstTee = tees.get("female").get(0);
                if (firstTee.has("holes") && firstTee.get("holes").isArray()) {
                    course.setNumHoles(firstTee.get("holes").size());
                }
            }
        }
        
        // Description
        if (node.has("description") && !node.get("description").isNull()) {
            course.setDescription(node.get("description").asText());
        }
        
        return course;
    }
    
    // Health check
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