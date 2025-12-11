package com.golf.service;

import com.golf.entity.*;
import com.golf.repository.RoundRepository;
import com.golf.repository.UserRepository;
import com.golf.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoundServiceTest {

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private RoundService roundService;

    private User testUser;
    private Course testCourse;
    private Round testRound;
    private List<HoleScore> testHoleScores;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setName("Test Course");
        testCourse.setNumHoles(18);

        testRound = new Round();
        testRound.setId(1L);
        testRound.setUser(testUser);
        testRound.setCourse(testCourse);
        testRound.setDatePlayed(LocalDate.now());
        testRound.setTotalStrokes(72);

        testHoleScores = new ArrayList<>();
        for (int i = 1; i <= 18; i++) {
            HoleScore holeScore = new HoleScore();
            holeScore.setHoleNumber(i);
            holeScore.setStrokes(4);
            holeScore.setPar(4);
            testHoleScores.add(holeScore);
        }
        testRound.setHoleScores(testHoleScores);
    }

    @Test
    void createRound_WhenValidData_ShouldCreateAndReturnRound() {
        // Arrange
        LocalDate datePlayed = LocalDate.of(2024, 1, 15);
        String notes = "Great round!";

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(testCourse.getId())).thenReturn(Optional.of(testCourse));
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> {
            Round round = invocation.getArgument(0);
            round.setId(1L);
            return round;
        });

        // Act
        Round result = roundService.createRound(
                testUser.getId(),
                testCourse.getId(),
                datePlayed,
                testHoleScores,
                notes);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testCourse, result.getCourse());
        assertEquals(datePlayed, result.getDatePlayed());
        assertEquals(notes, result.getNotes());
        assertEquals(72, result.getTotalStrokes()); // 18 holes * 4 strokes
        assertEquals(18, result.getHoleScores().size());

        // Verify all hole scores are linked to the round
        result.getHoleScores().forEach(hs -> assertEquals(result, hs.getRound()));

        verify(userRepository, times(1)).findById(testUser.getId());
        verify(courseRepository, times(1)).findById(testCourse.getId());
        verify(roundRepository, times(1)).save(any(Round.class));
    }

    @Test
    void createRound_WhenDateIsNull_ShouldUseCurrentDate() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(testCourse.getId())).thenReturn(Optional.of(testCourse));
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> {
            Round round = invocation.getArgument(0);
            round.setId(1L);
            return round;
        });

        // Act
        Round result = roundService.createRound(
                testUser.getId(),
                testCourse.getId(),
                null,
                testHoleScores,
                null);

        // Assert
        assertNotNull(result.getDatePlayed());
        verify(roundRepository, times(1)).save(any(Round.class));
    }

    @Test
    void createRound_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            roundService.createRound(
                    testUser.getId(),
                    testCourse.getId(),
                    LocalDate.now(),
                    testHoleScores,
                    "Notes");
        });
        verify(userRepository, times(1)).findById(testUser.getId());
        verify(courseRepository, never()).findById(any());
        verify(roundRepository, never()).save(any(Round.class));
    }

    @Test
    void createRound_WhenCourseNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(testCourse.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            roundService.createRound(
                    testUser.getId(),
                    testCourse.getId(),
                    LocalDate.now(),
                    testHoleScores,
                    "Notes");
        });
        verify(userRepository, times(1)).findById(testUser.getId());
        verify(courseRepository, times(1)).findById(testCourse.getId());
        verify(roundRepository, never()).save(any(Round.class));
    }

    @Test
    void createRound_ShouldCalculateTotalStrokesCorrectly() {
        // Arrange
        List<HoleScore> scores = Arrays.asList(
                createHoleScore(1, 3, 4),
                createHoleScore(2, 4, 4),
                createHoleScore(3, 5, 4));

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(testCourse.getId())).thenReturn(Optional.of(testCourse));
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> {
            Round round = invocation.getArgument(0);
            round.setId(1L);
            return round;
        });

        // Act
        Round result = roundService.createRound(
                testUser.getId(),
                testCourse.getId(),
                LocalDate.now(),
                scores,
                null);

        // Assert
        assertEquals(12, result.getTotalStrokes()); // 3 + 4 + 5
    }

    @Test
    void getUserRounds_ShouldReturnRoundsForUser() {
        // Arrange
        List<Round> rounds = Arrays.asList(testRound);
        when(roundRepository.findByUserIdOrderByDatePlayedDesc(testUser.getId())).thenReturn(rounds);

        // Act
        List<Round> result = roundService.getUserRounds(testUser.getId());

        // Assert
        assertEquals(1, result.size());
        assertEquals(testRound, result.get(0));
        verify(roundRepository, times(1)).findByUserIdOrderByDatePlayedDesc(testUser.getId());
    }

    @Test
    void getCourseRounds_ShouldReturnRoundsForCourse() {
        // Arrange
        List<Round> rounds = Arrays.asList(testRound);
        when(roundRepository.findByCourseIdOrderByDatePlayedDesc(testCourse.getId())).thenReturn(rounds);

        // Act
        List<Round> result = roundService.getCourseRounds(testCourse.getId());

        // Assert
        assertEquals(1, result.size());
        assertEquals(testRound, result.get(0));
        verify(roundRepository, times(1)).findByCourseIdOrderByDatePlayedDesc(testCourse.getId());
    }

    @Test
    void getRoundById_WhenRoundExists_ShouldReturnRound() {
        // Arrange
        when(roundRepository.findById(1L)).thenReturn(Optional.of(testRound));

        // Act
        Optional<Round> result = roundService.getRoundById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRound, result.get());
        verify(roundRepository, times(1)).findById(1L);
    }

    @Test
    void getRoundById_WhenRoundDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(roundRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Round> result = roundService.getRoundById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(roundRepository, times(1)).findById(999L);
    }

    @Test
    void deleteRound_ShouldCallRepositoryDelete() {
        // Arrange
        doNothing().when(roundRepository).deleteById(1L);

        // Act
        roundService.deleteRound(1L);

        // Assert
        verify(roundRepository, times(1)).deleteById(1L);
    }

    private HoleScore createHoleScore(int holeNumber, int strokes, int par) {
        HoleScore holeScore = new HoleScore();
        holeScore.setHoleNumber(holeNumber);
        holeScore.setStrokes(strokes);
        holeScore.setPar(par);
        return holeScore;
    }
}
