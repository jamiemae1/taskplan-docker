package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.TaskplanDockerApp;
import com.mycompany.myapp.config.AsyncSyncConfiguration;
import com.mycompany.myapp.config.JacksonConfiguration;
import com.mycompany.myapp.domain.Authority;
import com.mycompany.myapp.domain.Task;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.TaskPriority;
import com.mycompany.myapp.repository.AuthorityRepository;
import com.mycompany.myapp.repository.TaskRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.TaskService;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Extended Integration tests for the {@link TaskResource} REST controller.
 */
@SpringBootTest(classes = { TaskplanDockerApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@AutoConfigureMockMvc
@WithMockUser(value = TaskResourceExtendedIT.TEST_USER_LOGIN)
@TestPropertySource(properties = { "spring.profiles.active=test" })
class TaskResourceExtendedIT {

    static final String TEST_USER_LOGIN = "test-user";

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTaskMockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private CacheManager cacheManager;

    private User testUser;
    private Task task;

    @BeforeEach
    public void initTest() {
        // Clean up all tasks and users before each test
        taskRepository.deleteAll();
        userRepository.deleteAll();
        authorityRepository.deleteAll();

        // Clear user caches to prevent stale data issues
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).clear();
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).clear();

        // Ensure ROLE_USER authority exists
        Authority userAuthority = new Authority();
        userAuthority.setName("ROLE_USER");
        userAuthority = authorityRepository.saveAndFlush(userAuthority);

        // Create and persist a fresh user
        testUser = new User();
        testUser.setLogin(TEST_USER_LOGIN);
        testUser.setPassword(TestUtil.generateEncodedTestPassword());
        testUser.setActivated(true);
        testUser.setEmail("testuser@localhost");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setLangKey("en");
        Set<Authority> authorities = new HashSet<>();
        authorities.add(userAuthority);
        testUser.setAuthorities(authorities);
        testUser = userRepository.saveAndFlush(testUser);

        // Ensure the user is properly persisted and flush all changes
        userRepository.flush();
        authorityRepository.flush();
        em.flush();
        em.clear(); // Clear the entity manager to ensure fresh data

        // Verify the user was created
        testUser = userRepository
            .findOneByLogin(TEST_USER_LOGIN)
            .orElseThrow(() -> new RuntimeException("Test user not found after creation"));
        System.out.println("Created test user with ID: " + testUser.getId() + " and login: " + testUser.getLogin());

        task = new Task()
            .description("Test Task")
            .dueDate(LocalDate.now())
            .priority(TaskPriority.HIGH)
            .completed(false)
            .createdDate(Instant.now())
            .user(testUser);
    }

    @AfterEach
    public void tearDown() {
        // Clear security context to ensure clean state
        SecurityContextHolder.clearContext();
    }

    @Test
    @Transactional
    void getAllTasksWithCompletedFilter() throws Exception {
        // Clean up any existing tasks first
        taskRepository.deleteAll();

        // Initialize the database with two tasks
        Task completedTask = new Task().description("Completed Task").priority(TaskPriority.MEDIUM).completed(true).user(testUser);
        taskRepository.saveAndFlush(completedTask);

        Task incompleteTask = new Task().description("Incomplete Task").priority(TaskPriority.MEDIUM).completed(false).user(testUser);
        taskRepository.saveAndFlush(incompleteTask);

        // Get completed tasks
        restTaskMockMvc
            .perform(get("/api/tasks/filter?completed=true"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].description").value(hasItem("Completed Task")))
            .andExpect(jsonPath("$.[*].completed").value(hasItem(true)));

        // Get incomplete tasks
        restTaskMockMvc
            .perform(get("/api/tasks/filter?completed=false"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].description").value(hasItem("Incomplete Task")))
            .andExpect(jsonPath("$.[*].completed").value(hasItem(false)));
    }

    @Test
    @Transactional
    void toggleTaskCompletion() throws Exception {
        // Initialize the database
        task = taskRepository.saveAndFlush(task);

        // Toggle completion status
        restTaskMockMvc
            .perform(patch("/api/tasks/{id}/toggle-completion", task.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(true));

        // Verify the task is updated in the database
        Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(updatedTask.getCompleted()).isTrue();

        // Toggle back
        restTaskMockMvc
            .perform(patch("/api/tasks/{id}/toggle-completion", task.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    @Transactional
    void toggleTaskCompletionNonExistingTask() throws Exception {
        // Try to toggle a non-existing task
        restTaskMockMvc.perform(patch("/api/tasks/{id}/toggle-completion", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void createTaskWithInvalidDescription() throws Exception {
        task.setDescription(""); // Empty description should fail validation

        restTaskMockMvc
            .perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(task)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void getAllTasksForCurrentUserOnly() throws Exception {
        // Clean up any existing tasks first
        taskRepository.deleteAll();

        // Create a task for test user
        Task userTask = new Task().description("User Task").priority(TaskPriority.MEDIUM).completed(false).user(testUser);
        taskRepository.saveAndFlush(userTask);

        // Create a task for another user
        User otherUser = new User();
        otherUser.setLogin("other-user");
        otherUser.setPassword(TestUtil.generateEncodedTestPassword());
        otherUser.setActivated(true);
        otherUser.setEmail("other@localhost");
        otherUser = userRepository.saveAndFlush(otherUser);

        Task otherUserTask = new Task().description("Other User Task").priority(TaskPriority.MEDIUM).completed(false).user(otherUser);
        taskRepository.saveAndFlush(otherUserTask);

        // Get tasks for current user only
        restTaskMockMvc
            .perform(get("/api/tasks?currentUserOnly=true"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].description").value(hasItem("User Task")))
            .andExpect(jsonPath("$.[*].description").value(not(hasItem("Other User Task"))));
    }

    @Test
    @Transactional
    void updateTaskWithInvalidPriority() throws Exception {
        // Initialize the database
        task = taskRepository.saveAndFlush(task);

        // Update the task with an invalid priority
        String taskJson = String.format("{\"id\":%d,\"description\":\"Test Task\",\"priority\":\"INVALID_PRIORITY\"}", task.getId());

        restTaskMockMvc
            .perform(put("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(taskJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void partialUpdateTaskDescription() throws Exception {
        // Initialize the database
        task = taskRepository.saveAndFlush(task);

        // Update the task with a new description
        String partialTaskJson = String.format("{\"id\":%d,\"description\":\"Updated Description\"}", task.getId());

        restTaskMockMvc
            .perform(patch("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(partialTaskJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Updated Description"))
            .andExpect(jsonPath("$.dueDate").value(task.getDueDate().toString()))
            .andExpect(jsonPath("$.priority").value(task.getPriority().toString()))
            .andExpect(jsonPath("$.completed").value(task.getCompleted()));
    }

    @Test
    @Transactional
    void searchTasksByDescription() throws Exception {
        // Clean up any existing tasks first
        taskRepository.deleteAll();

        // Initialize the database with tasks
        Task task1 = new Task().description("Important meeting with client").priority(TaskPriority.HIGH).completed(false).user(testUser);
        taskRepository.saveAndFlush(task1);

        Task task2 = new Task().description("Review project documentation").priority(TaskPriority.MEDIUM).completed(false).user(testUser);
        taskRepository.saveAndFlush(task2);

        // Search for tasks containing "meeting"
        restTaskMockMvc
            .perform(get("/api/tasks/search?query=meeting"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].description").value(hasItem("Important meeting with client")));

        // Search for tasks containing "documentation"
        restTaskMockMvc
            .perform(get("/api/tasks/search?query=documentation"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].description").value(hasItem("Review project documentation")));
    }

    @Test
    @Transactional
    void filterTasksByPriority() throws Exception {
        // Clean up any existing tasks first
        taskRepository.deleteAll();

        // Initialize the database with tasks of different priorities
        Task highPriorityTask = new Task().description("High priority task").priority(TaskPriority.HIGH).completed(false).user(testUser);
        taskRepository.saveAndFlush(highPriorityTask);

        Task lowPriorityTask = new Task().description("Low priority task").priority(TaskPriority.LOW).completed(false).user(testUser);
        taskRepository.saveAndFlush(lowPriorityTask);

        // Filter tasks by HIGH priority
        restTaskMockMvc
            .perform(get("/api/tasks/filter?priority=HIGH"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].description").value(hasItem("High priority task")))
            .andExpect(jsonPath("$.[*].priority").value(hasItem("HIGH")));

        // Filter tasks by LOW priority
        restTaskMockMvc
            .perform(get("/api/tasks/filter?priority=LOW"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].description").value(hasItem("Low priority task")))
            .andExpect(jsonPath("$.[*].priority").value(hasItem("LOW")));
    }

    @Test
    @Transactional
    void filterTasksByDateRange() throws Exception {
        // Clean up any existing tasks first
        taskRepository.deleteAll();

        // Initialize the database with tasks having different due dates
        Task futureTask = new Task()
            .description("Future task")
            .dueDate(LocalDate.now().plusDays(5))
            .priority(TaskPriority.MEDIUM)
            .completed(false)
            .user(testUser);
        taskRepository.saveAndFlush(futureTask);

        Task pastTask = new Task()
            .description("Past task")
            .dueDate(LocalDate.now().minusDays(5))
            .priority(TaskPriority.MEDIUM)
            .completed(false)
            .user(testUser);
        taskRepository.saveAndFlush(pastTask);

        // Filter tasks by date range (today to future)
        restTaskMockMvc
            .perform(
                get("/api/tasks/filter")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(10).toString())
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].description").value(hasItem("Future task")));

        // Filter tasks by date range (past to today)
        restTaskMockMvc
            .perform(
                get("/api/tasks/filter")
                    .param("startDate", LocalDate.now().minusDays(10).toString())
                    .param("endDate", LocalDate.now().toString())
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].description").value(hasItem("Past task")));
    }

    @Test
    @Transactional
    void createTaskWithFutureDueDate() throws Exception {
        Task futureTask = new Task()
            .description("Future task")
            .dueDate(LocalDate.now().plusDays(30))
            .priority(TaskPriority.MEDIUM)
            .completed(false);
        // Don't set user - let the service set it from security context

        restTaskMockMvc
            .perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(futureTask)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.dueDate").value(futureTask.getDueDate().toString()));
    }

    @Test
    @Transactional
    void createTaskWithPastDueDate() throws Exception {
        Task pastTask = new Task()
            .description("Past task")
            .dueDate(LocalDate.now().minusDays(1))
            .priority(TaskPriority.MEDIUM)
            .completed(false);
        // Don't set user - let the service set it from security context

        restTaskMockMvc
            .perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(pastTask)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.dueDate").value(pastTask.getDueDate().toString()));
    }

    @Test
    @Transactional
    void updateTaskWithNullFields() throws Exception {
        // Initialize the database
        task = taskRepository.saveAndFlush(task);

        // Update the task with null fields
        String taskJson = String.format("{\"id\":%d,\"description\":\"Test Task\",\"dueDate\":null,\"priority\":null}", task.getId());

        restTaskMockMvc
            .perform(put("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(taskJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void createTaskWithInvalidPriorityValue() throws Exception {
        String invalidJson = objectMapper.writeValueAsString(task).replace("\"HIGH\"", "\"INVALID_PRIORITY\"");

        restTaskMockMvc
            .perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void createTaskWithoutUser() throws Exception {
        task.setUser(null);

        restTaskMockMvc
            .perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(task)))
            .andExpect(status().isCreated()); // Should succeed since TaskService sets user from security context
    }

    @Test
    @Transactional
    void createTaskWithInvalidDueDateFormat() throws Exception {
        String invalidJson = objectMapper.writeValueAsString(task).replace(task.getDueDate().toString(), "invalid-date");

        restTaskMockMvc
            .perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void updateTaskWithInvalidUser() throws Exception {
        taskRepository.saveAndFlush(task);

        // Create another user
        User invalidUser = new User();
        invalidUser.setLogin("invalid-user");
        invalidUser.setPassword(TestUtil.generateEncodedTestPassword());
        invalidUser.setActivated(true);
        invalidUser.setEmail("invalid@localhost");
        userRepository.saveAndFlush(invalidUser);

        // Try to update task with different user
        task.setUser(invalidUser);

        restTaskMockMvc
            .perform(
                put("/api/tasks/{id}", task.getId()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(task))
            )
            .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void searchTasksByDescriptionPartialMatch() throws Exception {
        // Initialize the database with tasks
        Task task1 = new Task().description("Important meeting").priority(TaskPriority.HIGH).user(testUser);
        taskRepository.saveAndFlush(task1);

        Task task2 = new Task().description("Meeting notes").priority(TaskPriority.MEDIUM).user(testUser);
        taskRepository.saveAndFlush(task2);

        Task task3 = new Task().description("Call client").priority(TaskPriority.LOW).user(testUser);
        taskRepository.saveAndFlush(task3);

        // Search for "meeting" - should return 2 tasks
        restTaskMockMvc
            .perform(get("/api/tasks/search?query=meeting"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$.[*].description").value(containsInAnyOrder("Important meeting", "Meeting notes")));

        // Search for "client" - should return 1 task
        restTaskMockMvc
            .perform(get("/api/tasks/search?query=client"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$.[0].description").value("Call client"));
    }

    @Test
    @Transactional
    void filterTasksByDateRangeAndPriority() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate nextWeek = today.plusWeeks(1);

        // Create tasks with different dates and priorities
        Task task1 = new Task().description("Today high priority").dueDate(today).priority(TaskPriority.HIGH).user(testUser);
        taskRepository.saveAndFlush(task1);

        Task task2 = new Task().description("Tomorrow medium priority").dueDate(tomorrow).priority(TaskPriority.MEDIUM).user(testUser);
        taskRepository.saveAndFlush(task2);

        Task task3 = new Task().description("Next week low priority").dueDate(nextWeek).priority(TaskPriority.LOW).user(testUser);
        taskRepository.saveAndFlush(task3);

        // Filter by date range and high priority
        restTaskMockMvc
            .perform(
                get("/api/tasks/filter")
                    .param("fromDate", today.toString())
                    .param("toDate", tomorrow.toString())
                    .param("priority", TaskPriority.HIGH.toString())
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$.[0].description").value("Today high priority"));

        // Filter by date range only
        restTaskMockMvc
            .perform(get("/api/tasks/filter").param("fromDate", today.toString()).param("toDate", nextWeek.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.length()").value(3));

        // Filter by priority only
        restTaskMockMvc
            .perform(get("/api/tasks/filter").param("priority", TaskPriority.MEDIUM.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$.[0].description").value("Tomorrow medium priority"));
    }

    @Test
    @Transactional
    void filterTasksWithInvalidDateRange() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate toDate = LocalDate.now(); // toDate before fromDate

        restTaskMockMvc
            .perform(get("/api/tasks/filter").param("fromDate", fromDate.toString()).param("toDate", toDate.toString()))
            .andExpect(status().isOk());
    }
}
