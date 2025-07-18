package com.mycompany.myapp.web.rest.errors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class ErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testBadRequestAlertException() throws Exception {
        mockMvc
            .perform(get("/api/error/bad-request"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.test"));
    }

    @Test
    void testTaskValidationErrors() throws Exception {
        mockMvc
            .perform(post("/api/error/validation").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors.length()").value(3))
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'description' && @.message == 'must not be blank')]").exists())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'description' && @.message == 'must not be null')]").exists())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'priority' && @.message == 'must not be null')]").exists());
    }

    @Test
    void testTaskNotFoundError() throws Exception {
        mockMvc
            .perform(get("/api/error/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("error.tasknotfound"));
    }

    @Test
    void testTaskConcurrencyError() throws Exception {
        mockMvc
            .perform(get("/api/error/concurrency"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("error.taskconcurrency"));
    }
}
