package com.golf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golf.entity.Course;
import com.golf.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void getAllCourses_ShouldReturn200AndListOfCourses() throws Exception {
        // Arrange
        Course course2 = new Course();
        course2.setId(2L);
        course2.setName("Augusta National");
        List<Course> courses = Arrays.asList(testCourse, course2);
        when(courseService.getAllCourses()).thenReturn(courses);

        // Act & Assert
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Pebble Beach"))
                .andExpect(jsonPath("$[1].name").value("Augusta National"));

        verify(courseService, times(1)).getAllCourses();
    }

    @Test
    void getCourseById_WhenCourseExists_ShouldReturn200AndCourse() throws Exception {
        // Arrange
        when(courseService.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // Act & Assert
        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Pebble Beach"))
                .andExpect(jsonPath("$.city").value("Pebble Beach"));

        verify(courseService, times(1)).getCourseById(1L);
    }

    @Test
    void getCourseById_WhenCourseDoesNotExist_ShouldReturn404() throws Exception {
        // Arrange
        when(courseService.getCourseById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/courses/999"))
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourseById(999L);
    }

    @Test
    void getCourseByExternalId_WhenCourseExists_ShouldReturn200AndCourse() throws Exception {
        // Arrange
        testCourse.setExternalId(12345L);
        when(courseService.getCourseByExternalId(12345L)).thenReturn(Optional.of(testCourse));

        // Act & Assert
        mockMvc.perform(get("/api/courses/external/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(12345));

        verify(courseService, times(1)).getCourseByExternalId(12345L);
    }

    @Test
    void searchCourses_ShouldReturn200AndMatchingCourses() throws Exception {
        // Arrange
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.searchCoursesByName("pebble")).thenReturn(courses);

        // Act & Assert
        mockMvc.perform(get("/api/courses/search")
                .param("name", "pebble"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Pebble Beach"));

        verify(courseService, times(1)).searchCoursesByName("pebble");
    }

    @Test
    void getCoursesByCity_ShouldReturn200AndCoursesInCity() throws Exception {
        // Arrange
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.getCoursesByCity("Pebble Beach")).thenReturn(courses);

        // Act & Assert
        mockMvc.perform(get("/api/courses/city/Pebble Beach"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].city").value("Pebble Beach"));

        verify(courseService, times(1)).getCoursesByCity("Pebble Beach");
    }

    @Test
    void getCoursesByState_ShouldReturn200AndCoursesInState() throws Exception {
        // Arrange
        List<Course> courses = Arrays.asList(testCourse);
        when(courseService.getCoursesByState("CA")).thenReturn(courses);

        // Act & Assert
        mockMvc.perform(get("/api/courses/state/CA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].state").value("CA"));

        verify(courseService, times(1)).getCoursesByState("CA");
    }

    @Test
    void createCourse_ShouldReturn201AndCreatedCourse() throws Exception {
        // Arrange
        Course newCourse = new Course();
        newCourse.setName("New Course");
        when(courseService.saveCourse(any(Course.class))).thenReturn(testCourse);

        // Act & Assert
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCourse)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Pebble Beach"));

        verify(courseService, times(1)).saveCourse(any(Course.class));
    }

    @Test
    void updateCourse_WhenCourseExists_ShouldReturn200AndUpdatedCourse() throws Exception {
        // Arrange
        Course updatedCourse = new Course();
        updatedCourse.setName("Updated Course");
        when(courseService.updateCourse(eq(1L), any(Course.class))).thenReturn(testCourse);

        // Act & Assert
        mockMvc.perform(put("/api/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCourse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(courseService, times(1)).updateCourse(eq(1L), any(Course.class));
    }

    @Test
    void updateCourse_WhenCourseDoesNotExist_ShouldReturn404() throws Exception {
        // Arrange
        Course updatedCourse = new Course();
        when(courseService.updateCourse(eq(999L), any(Course.class)))
                .thenThrow(new RuntimeException("Course not found with id: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/courses/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCourse)))
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).updateCourse(eq(999L), any(Course.class));
    }

    @Test
    void deleteCourse_ShouldReturn204() throws Exception {
        // Arrange
        doNothing().when(courseService).deleteCourse(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/courses/1"))
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).deleteCourse(1L);
    }

    @Test
    void getCoursesCount_ShouldReturn200AndCount() throws Exception {
        // Arrange
        when(courseService.getCoursesCount()).thenReturn(5L);

        // Act & Assert
        mockMvc.perform(get("/api/courses/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(courseService, times(1)).getCoursesCount();
    }
}
