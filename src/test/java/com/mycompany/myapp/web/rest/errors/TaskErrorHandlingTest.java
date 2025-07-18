package com.mycompany.myapp.web.rest.errors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.domain.Task;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.TaskPriority;
import com.mycompany.myapp.repository.TaskRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.web.rest.TestUtil;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class TaskErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new User();
        testUser.setLogin("testuser");
        // Generate a proper 60-character password - exactly 60 characters
        testUser.setPassword("$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1");
        testUser.setActivated(true);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setLangKey("en");
        testUser = userRepository.saveAndFlush(testUser);
    }

    @Test
    @Transactional
    void testInvalidTaskUpdate() throws Exception {
        mockMvc
            .perform(put("/api/tasks/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors").isNotEmpty())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'description')]").exists())
            .andExpect(jsonPath("$.fieldErrors[?(@.field == 'priority')]").exists());
    }

    @Test
    @Transactional
    void testTaskNotFound() throws Exception {
        mockMvc
            .perform(get("/api/tasks/99999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("error.tasknotfound"));
    }

    @Test
    @Transactional
    void testTaskConcurrency() throws Exception {
        // First create a task in the database
        Task existingTask = new Task();
        existingTask.setDescription("Test Task");
        existingTask.setPriority(TaskPriority.LOW);
        existingTask.setCompleted(false);
        existingTask.setUser(testUser);
        existingTask.setCreatedDate(Instant.now());
        existingTask.setLastModifiedDate(Instant.now());
        existingTask = taskRepository.saveAndFlush(existingTask);

        // Now try to update with an old lastModifiedDate to trigger concurrency exception
        Task updateTask = new Task();
        updateTask.setId(existingTask.getId());
        updateTask.setDescription("Updated Task");
        updateTask.setPriority(TaskPriority.HIGH);
        updateTask.setCompleted(true);
        updateTask.setUser(testUser);
        updateTask.setLastModifiedDate(Instant.now().minusSeconds(60)); // Old timestamp

        mockMvc
            .perform(
                put("/api/tasks/" + existingTask.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updateTask))
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("error.taskconcurrency"));
    }
}
