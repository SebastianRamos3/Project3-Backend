package com.golf.service;

import com.golf.entity.Course;
import com.golf.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setName("Pebble Beach");
        testCourse.setCity("Pebble Beach");
        testCourse.setState("CA");
        testCourse.setCountry("USA");
        testCourse.setNumHoles(18);
    }

    @Test
    void getAllCourses_ShouldReturnAllCourses() {
        // Arrange
        Course course2 = new Course();
        course2.setId(2L);
        course2.setName("Augusta National");
        List<Course> courses = Arrays.asList(testCourse, course2);
        when(courseRepository.findAll()).thenReturn(courses);

        // Act
        List<Course> result = courseService.getAllCourses();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Pebble Beach", result.get(0).getName());
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    void getCourseById_WhenCourseExists_ShouldReturnCourse() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // Act
        Optional<Course> result = courseService.getCourseById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Pebble Beach", result.get().getName());
        verify(courseRepository, times(1)).findById(1L);
    }

    @Test
    void getCourseById_WhenCourseDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Course> result = courseService.getCourseById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(courseRepository, times(1)).findById(999L);
    }

    @Test
    void getCourseByExternalId_WhenCourseExists_ShouldReturnCourse() {
        // Arrange
        testCourse.setExternalId(12345L);
        when(courseRepository.findByExternalId(12345L)).thenReturn(Optional.of(testCourse));

        // Act
        Optional<Course> result = courseService.getCourseByExternalId(12345L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(12345L, result.get().getExternalId());
        verify(courseRepository, times(1)).findByExternalId(12345L);
    }

    @Test
    void searchCoursesByName_ShouldReturnMatchingCourses() {
        // Arrange
        Course course2 = new Course();
        course2.setId(2L);
        course2.setName("Pebble Beach Links");
        List<Course> courses = Arrays.asList(testCourse, course2);
        when(courseRepository.findByNameContainingIgnoreCase("pebble")).thenReturn(courses);

        // Act
        List<Course> result = courseService.searchCoursesByName("pebble");

        // Assert
        assertEquals(2, result.size());
        verify(courseRepository, times(1)).findByNameContainingIgnoreCase("pebble");
    }

    @Test
    void getCoursesByCity_ShouldReturnCoursesInCity() {
        // Arrange
        List<Course> courses = Arrays.asList(testCourse);
        when(courseRepository.findByCity("Pebble Beach")).thenReturn(courses);

        // Act
        List<Course> result = courseService.getCoursesByCity("Pebble Beach");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Pebble Beach", result.get(0).getCity());
        verify(courseRepository, times(1)).findByCity("Pebble Beach");
    }

    @Test
    void getCoursesByState_ShouldReturnCoursesInState() {
        // Arrange
        List<Course> courses = Arrays.asList(testCourse);
        when(courseRepository.findByState("CA")).thenReturn(courses);

        // Act
        List<Course> result = courseService.getCoursesByState("CA");

        // Assert
        assertEquals(1, result.size());
        assertEquals("CA", result.get(0).getState());
        verify(courseRepository, times(1)).findByState("CA");
    }

    @Test
    void saveCourse_ShouldReturnSavedCourse() {
        // Arrange
        Course newCourse = new Course();
        newCourse.setName("New Course");
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // Act
        Course result = courseService.saveCourse(newCourse);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(courseRepository, times(1)).save(newCourse);
    }

    @Test
    void updateCourse_WhenCourseExists_ShouldUpdateAndReturnCourse() {
        // Arrange
        Course updatedDetails = new Course();
        updatedDetails.setName("Updated Course Name");
        updatedDetails.setCity("New City");
        updatedDetails.setState("NY");
        updatedDetails.setCountry("USA");
        updatedDetails.setZipCode("12345");
        updatedDetails.setLatitude(40.7128);
        updatedDetails.setLongitude(-74.0060);
        updatedDetails.setPhoneNumber("555-1234");
        updatedDetails.setWebsite("https://example.com");
        updatedDetails.setNumHoles(18);
        updatedDetails.setDescription("Updated description");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // Act
        Course result = courseService.updateCourse(1L, updatedDetails);

        // Assert
        assertNotNull(result);
        verify(courseRepository, times(1)).findById(1L);
        verify(courseRepository, times(1)).save(testCourse);
    }

    @Test
    void updateCourse_WhenCourseDoesNotExist_ShouldThrowException() {
        // Arrange
        Course updatedDetails = new Course();
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            courseService.updateCourse(999L, updatedDetails);
        });
        verify(courseRepository, times(1)).findById(999L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_ShouldCallRepositoryDelete() {
        // Arrange
        doNothing().when(courseRepository).deleteById(1L);

        // Act
        courseService.deleteCourse(1L);

        // Assert
        verify(courseRepository, times(1)).deleteById(1L);
    }

    @Test
    void getCoursesCount_ShouldReturnCount() {
        // Arrange
        when(courseRepository.count()).thenReturn(5L);

        // Act
        long result = courseService.getCoursesCount();

        // Assert
        assertEquals(5L, result);
        verify(courseRepository, times(1)).count();
    }
}
