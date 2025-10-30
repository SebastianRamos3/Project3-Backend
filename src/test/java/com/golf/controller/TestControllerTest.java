package com.golf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestController.class)
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthCheck_ShouldReturn200AndCorrectResponse() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").value("Golf API is running!"));
    }

    @Test
    void healthCheck_ShouldReturnValidJsonStructure() throws Exception {
        String response = mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> responseMap = objectMapper.readValue(response, Map.class);

        assert responseMap.containsKey("status");
        assert responseMap.containsKey("message");
        assert responseMap.get("status").equals("UP");
        assert responseMap.get("message").equals("Golf API is running!");
    }

    @Test
    void healthCheck_ShouldHandleMultipleRequests() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"))
                    .andExpect(jsonPath("$.message").value("Golf API is running!"));
        }
    }
}
