package com.golf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golf.dto.CreateRoundRequest;
import com.golf.dto.HoleScoreDTO;
import com.golf.entity.*;
import com.golf.service.RoundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoundController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoundService roundService;

    private Round testRound;
    private User testUser;
    private Course testCourse;
    private CreateRoundRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setName("Test Course");

        testRound = new Round();
        testRound.setId(1L);
        testRound.setUser(testUser);
        testRound.setCourse(testCourse);
        testRound.setDatePlayed(LocalDate.of(2024, 1, 15));
        testRound.setTotalStrokes(72);
        testRound.setNotes("Great round!");

        List<HoleScore> holeScores = new ArrayList<>();
        for (int i = 1; i <= 18; i++) {
            HoleScore hs = new HoleScore();
            hs.setHoleNumber(i);
            hs.setStrokes(4);
            hs.setPar(4);
            holeScores.add(hs);
        }
        testRound.setHoleScores(holeScores);

        createRequest = new CreateRoundRequest();
        createRequest.setUserId(testUser.getId());
        createRequest.setCourseId(1L);
        createRequest.setDatePlayed(LocalDate.of(2024, 1, 15));
        createRequest.setNotes("Great round!");

        List<HoleScoreDTO> holeScoreDTOs = new ArrayList<>();
        for (int i = 1; i <= 18; i++) {
            HoleScoreDTO dto = new HoleScoreDTO();
            dto.setHoleNumber(i);
            dto.setStrokes(4);
            dto.setPar(4);
            holeScoreDTOs.add(dto);
        }
        createRequest.setHoleScores(holeScoreDTOs);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createRound_ShouldReturn201AndCreatedRound() throws Exception {
        // Arrange
        when(roundService.createRound(
                any(UUID.class),
                any(Long.class),
                any(LocalDate.class),
                any(List.class),
                any(String.class))).thenReturn(testRound);

        // Act & Assert
        mockMvc.perform(post("/api/rounds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.totalStrokes").value(72))
                .andExpect(jsonPath("$.notes").value("Great round!"))
                .andExpect(jsonPath("$.holeScores").isArray())
                .andExpect(jsonPath("$.holeScores.length()").value(18));

        verify(roundService, times(1)).createRound(
                eq(testUser.getId()),
                eq(1L),
                eq(LocalDate.of(2024, 1, 15)),
                any(List.class),
                eq("Great round!"));
    }

    @Test
    void getUserRounds_ShouldReturn200AndListOfRounds() throws Exception {
        // Arrange
        List<Round> rounds = Arrays.asList(testRound);
        when(roundService.getUserRounds(testUser.getId())).thenReturn(rounds);

        // Act & Assert
        mockMvc.perform(get("/api/rounds/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].courseName").value("Test Course"));

        verify(roundService, times(1)).getUserRounds(testUser.getId());
    }

    @Test
    void getRound_WhenRoundExists_ShouldReturn200AndRound() throws Exception {
        // Arrange
        when(roundService.getRoundById(1L)).thenReturn(Optional.of(testRound));

        // Act & Assert
        mockMvc.perform(get("/api/rounds/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.totalStrokes").value(72))
                .andExpect(jsonPath("$.holeScores").isArray());

        verify(roundService, times(1)).getRoundById(1L);
    }

    @Test
    void getRound_WhenRoundDoesNotExist_ShouldReturn404() throws Exception {
        // Arrange
        when(roundService.getRoundById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/rounds/999"))
                .andExpect(status().isNotFound());

        verify(roundService, times(1)).getRoundById(999L);
    }

    @Test
    void deleteRound_ShouldReturn204() throws Exception {
        // Arrange
        doNothing().when(roundService).deleteRound(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/rounds/1"))
                .andExpect(status().isNoContent());

        verify(roundService, times(1)).deleteRound(1L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createRound_ShouldMapHoleScoresCorrectly() throws Exception {
        // Arrange
        when(roundService.createRound(
                any(UUID.class),
                any(Long.class),
                any(LocalDate.class),
                any(List.class),
                any(String.class))).thenReturn(testRound);

        // Act & Assert
        mockMvc.perform(post("/api/rounds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.holeScores[0].holeNumber").value(1))
                .andExpect(jsonPath("$.holeScores[0].strokes").value(4))
                .andExpect(jsonPath("$.holeScores[0].par").value(4));

        verify(roundService, times(1)).createRound(
                any(UUID.class),
                any(Long.class),
                any(LocalDate.class),
                any(List.class),
                any(String.class));
    }
}
