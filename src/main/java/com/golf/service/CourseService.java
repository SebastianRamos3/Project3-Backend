package com.golf.service;

import com.golf.entity.Course;
import com.golf.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
    
    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }
    
    public Optional<Course> getCourseByExternalId(Long externalId) {
        return courseRepository.findByExternalId(externalId);
    }
    
    public List<Course> searchCoursesByName(String name) {
        return courseRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Course> getCoursesByCity(String city) {
        return courseRepository.findByCity(city);
    }
    
    public List<Course> getCoursesByState(String state) {
        return courseRepository.findByState(state);
    }
    
    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }
    
    public Course updateCourse(Long id, Course courseDetails) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
        
        course.setName(courseDetails.getName());
        course.setClubName(courseDetails.getClubName());
        course.setCity(courseDetails.getCity());
        course.setState(courseDetails.getState()); 
    
        course.setCountry(courseDetails.getCountry());
        course.setZipCode(courseDetails.getZipCode());
        course.setLatitude(courseDetails.getLatitude());
        course.setLongitude(courseDetails.getLongitude());
        course.setPhoneNumber(courseDetails.getPhoneNumber());
        course.setWebsite(courseDetails.getWebsite());
        course.setNumHoles(courseDetails.getNumHoles());
        course.setDescription(courseDetails.getDescription());
        
        return courseRepository.save(course);
    }
    
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
    
    public long getCoursesCount() {
        return courseRepository.count();
    }
}