package com.mycompany.myapp.web.rest.errors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mycompany.myapp.config.AsyncSyncConfiguration;
import com.mycompany.myapp.config.JacksonConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

/**
 * Integration tests for the {@link ExceptionTranslator} controller advice.
 */
@WithMockUser
@WebMvcTest({ ExceptionTranslatorTestController.class })
@Import({ ExceptionTranslator.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@ActiveProfiles("test")
class ExceptionTranslatorIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testMethodArgumentNotValid() throws Exception {
        mockMvc
            .perform(
                post("/api/exception-translator-test/method-argument")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"test\": null}")
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void testMissingServletRequestPart() throws Exception {
        mockMvc
            .perform(get("/api/exception-translator-test/missing-servlet-request-part"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void testMissingServletRequestParameter() throws Exception {
        mockMvc
            .perform(get("/api/exception-translator-test/missing-servlet-request-parameter"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void testAccessDenied() throws Exception {
        mockMvc
            .perform(get("/api/exception-translator-test/access-denied"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void testUnauthorized() throws Exception {
        mockMvc
            .perform(get("/api/exception-translator-test/unauthorized"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void testInternalServerError() throws Exception {
        mockMvc
            .perform(get("/api/exception-translator-test/internal-server-error"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void testResponseStatusException() throws Exception {
        mockMvc
            .perform(get("/api/exception-translator-test/response-status"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void testConcurrencyFailure() throws Exception {
        mockMvc
            .perform(get("/api/exception-translator-test/concurrency-failure"))
            .andExpect(status().isConflict())
            .andExpect(content().contentType("application/problem+json"));
    }
}
