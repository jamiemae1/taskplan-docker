package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.TaskplanDockerApp;
import com.mycompany.myapp.config.AsyncSyncConfiguration;
import com.mycompany.myapp.config.EmbeddedSQL;
import com.mycompany.myapp.config.JacksonConfiguration;
import com.mycompany.myapp.domain.Task;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.TaskPriority;
import com.mycompany.myapp.repository.TaskRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.TaskService;
import com.mycompany.myapp.web.rest.TestUtil;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = { TaskplanDockerApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = { "spring.profiles.active=test" })
class TaskResourceTest {

    private static final String DEFAULT_LOGIN = "test-user";
    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final LocalDate DEFAULT_DUE_DATE = LocalDate.ofEpochDay(0L);
    private static final TaskPriority DEFAULT_PRIORITY = TaskPriority.LOW;
    private static final Boolean DEFAULT_COMPLETED = false;

    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";
    private static final LocalDate UPDATED_DUE_DATE = LocalDate.now();
    private static final TaskPriority UPDATED_PRIORITY = TaskPriority.HIGH;
    private static final Boolean UPDATED_COMPLETED = true;

    private static final String ENTITY_API_URL = "/api/tasks";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_API_URL_FILTER = ENTITY_API_URL + "/filter";

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTaskMockMvc;

    private Task task;

    private User user;

    private void setupSecurityContext(String userLogin) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(userLogin, "password", authorities));
        SecurityContextHolder.setContext(securityContext);
    }

    private User createAndSaveUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(RandomStringUtils.randomAlphanumeric(60));
        user.setActivated(true);
        user.setEmail(login + "@localhost");
        user.setFirstName("john");
        user.setLastName("doe");
        user.setLangKey("en");
        return userRepository.saveAndFlush(user);
    }

    public Task createEntity() {
        Task task = new Task()
            .description(DEFAULT_DESCRIPTION)
            .dueDate(DEFAULT_DUE_DATE)
            .priority(DEFAULT_PRIORITY)
            .completed(DEFAULT_COMPLETED)
            .createdDate(Instant.now())
            .lastModifiedDate(Instant.now())
            .user(user);
        return task;
    }

    public Task createUpdatedEntity() {
        Task task = new Task()
            .description(UPDATED_DESCRIPTION)
            .dueDate(UPDATED_DUE_DATE)
            .priority(UPDATED_PRIORITY)
            .completed(UPDATED_COMPLETED)
            .createdDate(Instant.now())
            .lastModifiedDate(Instant.now())
            .user(user);
        return task;
    }

    @BeforeEach
    public void initTest() {
        // Create and save test user
        user = createAndSaveUser(DEFAULT_LOGIN);

        // Set up security context with the same user
        setupSecurityContext(DEFAULT_LOGIN);

        // Create test task with the saved user
        task = createEntity();
    }

    @AfterEach
    public void cleanupTest() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Transactional
    void createTask() throws Exception {
        int databaseSizeBeforeCreate = taskRepository.findAll().size();
        // Create the Task
        restTaskMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(task)))
            .andExpect(status().isCreated());

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeCreate + 1);
        Task testTask = taskList.get(taskList.size() - 1);
        assertThat(testTask.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTask.getDueDate()).isEqualTo(DEFAULT_DUE_DATE);
        assertThat(testTask.getPriority()).isEqualTo(DEFAULT_PRIORITY);
        assertThat(testTask.getCompleted()).isEqualTo(DEFAULT_COMPLETED);
        assertThat(testTask.getUser().getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(testTask.getCreatedDate()).isNotNull();
        assertThat(testTask.getLastModifiedDate()).isNotNull();
    }

    @Test
    @Transactional
    void createTaskWithExistingId() throws Exception {
        task.setId(1L);

        int databaseSizeBeforeCreate = taskRepository.findAll().size();

        restTaskMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(task)))
            .andExpect(status().isBadRequest());

        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDescriptionIsRequired() throws Exception {
        int databaseSizeBeforeTest = taskRepository.findAll().size();
        task.setDescription(null);

        restTaskMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(task)))
            .andExpect(status().isBadRequest());

        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTasks() throws Exception {
        // Initialize the database
        taskRepository.saveAndFlush(task);

        // Get all the tasks
        restTaskMockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(task.getId().intValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].dueDate").value(hasItem(DEFAULT_DUE_DATE.toString())))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY.toString())))
            .andExpect(jsonPath("$.[*].completed").value(hasItem(DEFAULT_COMPLETED.booleanValue())));
    }

    @Test
    @Transactional
    void getAllTasksWithPagination() throws Exception {
        // Initialize the database
        taskRepository.saveAndFlush(task);

        // Get all tasks with pagination
        restTaskMockMvc
            .perform(get(ENTITY_API_URL).param("page", "0").param("size", "2").param("sort", "description,asc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").exists())
            .andExpect(jsonPath("$.[*].description").exists())
            .andExpect(jsonPath("$.[*].priority").exists())
            .andExpect(jsonPath("$.[*].completed").exists());
    }

    @Test
    @Transactional
    void getAllTasksWithSorting() throws Exception {
        // Initialize the database
        taskRepository.saveAndFlush(task);

        // Get all tasks with sorting
        restTaskMockMvc
            .perform(get(ENTITY_API_URL).param("sort", "priority,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").exists())
            .andExpect(jsonPath("$.[*].description").exists())
            .andExpect(jsonPath("$.[*].priority").exists())
            .andExpect(jsonPath("$.[*].completed").exists());
    }

    @Test
    @Transactional
    void getTask() throws Exception {
        // Initialize the database
        taskRepository.saveAndFlush(task);

        // Get the task
        restTaskMockMvc
            .perform(get(ENTITY_API_URL_ID, task.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(task.getId().intValue()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.dueDate").value(DEFAULT_DUE_DATE.toString()))
            .andExpect(jsonPath("$.priority").value(DEFAULT_PRIORITY.toString()))
            .andExpect(jsonPath("$.completed").value(DEFAULT_COMPLETED.booleanValue()));
    }

    @Test
    @Transactional
    void getNonExistingTask() throws Exception {
        // Get the task
        restTaskMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTask() throws Exception {
        // Initialize the database
        taskRepository.saveAndFlush(task);

        int databaseSizeBeforeUpdate = taskRepository.findAll().size();

        // Update the task
        Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        // Keep the lastModifiedDate from the database
        Instant originalLastModifiedDate = updatedTask.getLastModifiedDate();
        updatedTask
            .description(UPDATED_DESCRIPTION)
            .dueDate(UPDATED_DUE_DATE)
            .priority(UPDATED_PRIORITY)
            .completed(UPDATED_COMPLETED)
            .lastModifiedDate(originalLastModifiedDate); // Set the original lastModifiedDate

        restTaskMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedTask.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedTask))
            )
            .andExpect(status().isOk());

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
        Task testTask = taskList.get(taskList.size() - 1);
        assertThat(testTask.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testTask.getDueDate()).isEqualTo(UPDATED_DUE_DATE);
        assertThat(testTask.getPriority()).isEqualTo(UPDATED_PRIORITY);
        assertThat(testTask.getCompleted()).isEqualTo(UPDATED_COMPLETED);
        assertThat(testTask.getUser().getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(testTask.getLastModifiedDate()).isAfter(originalLastModifiedDate);
    }

    @Test
    @Transactional
    void putNonExistingTask() throws Exception {
        int databaseSizeBeforeUpdate = taskRepository.findAll().size();
        task.setId(count.incrementAndGet());

        restTaskMockMvc
            .perform(
                put(ENTITY_API_URL_ID, task.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(task))
            )
            .andExpect(status().isNotFound());

        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTask() throws Exception {
        int databaseSizeBeforeUpdate = taskRepository.findAll().size();
        task.setId(count.incrementAndGet());

        restTaskMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(task))
            )
            .andExpect(status().isBadRequest());

        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTask() throws Exception {
        int databaseSizeBeforeUpdate = taskRepository.findAll().size();
        task.setId(count.incrementAndGet());

        restTaskMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(task)))
            .andExpect(status().isBadRequest());

        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTask() throws Exception {
        // Initialize the database
        taskRepository.saveAndFlush(task);

        int databaseSizeBeforeDelete = taskRepository.findAll().size();

        // Delete the task
        restTaskMockMvc.perform(delete(ENTITY_API_URL_ID, task.getId())).andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    void getFilteredTasks() throws Exception {
        // Initialize the database
        taskRepository.saveAndFlush(task);

        // Get filtered tasks
        restTaskMockMvc
            .perform(
                get(ENTITY_API_URL_FILTER)
                    .param("priority", DEFAULT_PRIORITY.toString())
                    .param("completed", String.valueOf(DEFAULT_COMPLETED))
                    .param("startDate", DEFAULT_DUE_DATE.toString())
                    .param("endDate", DEFAULT_DUE_DATE.plusDays(7).toString())
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(task.getId().intValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].dueDate").value(hasItem(DEFAULT_DUE_DATE.toString())))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY.toString())))
            .andExpect(jsonPath("$.[*].completed").value(hasItem(DEFAULT_COMPLETED.booleanValue())));
    }

    @Test
    @Transactional
    void toggleTaskCompletion() throws Exception {
        // Initialize the database
        taskRepository.saveAndFlush(task);

        // Toggle task completion
        restTaskMockMvc
            .perform(put(ENTITY_API_URL_ID + "/toggle", task.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(task.getId().intValue()))
            .andExpect(jsonPath("$.completed").value(true));

        // Verify the Task in the database
        Task testTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(testTask.getCompleted()).isTrue();
        assertThat(testTask.getLastModifiedDate()).isNotNull();
    }

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
}
