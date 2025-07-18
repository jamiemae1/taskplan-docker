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
import com.mycompany.myapp.domain.Authority;
import com.mycompany.myapp.domain.Task;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.TaskPriority;
import com.mycompany.myapp.repository.AuthorityRepository;
import com.mycompany.myapp.repository.TaskRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.TaskService;
import com.mycompany.myapp.service.UserService;
import com.mycompany.myapp.web.rest.errors.ExceptionTranslator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(classes = { TaskplanDockerApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@AutoConfigureMockMvc
@TestPropertySource(properties = { "spring.profiles.active=test" })
class TaskResourceIT {

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

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private CacheManager cacheManager;

    private Task task;
    private User user;

    @BeforeEach
    public void setup() {
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
        user = new User();
        user.setLogin(DEFAULT_LOGIN);
        user.setPassword(RandomStringUtils.randomAlphanumeric(60));
        user.setActivated(true);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setLangKey("en");
        Set<Authority> authorities = new HashSet<>();
        authorities.add(userAuthority);
        user.setAuthorities(authorities);
        user = userRepository.saveAndFlush(user);

        // Ensure the user is properly persisted and flush all changes
        userRepository.flush();

        // Verify the user was created
        User savedUser = userRepository
            .findOneByLogin(DEFAULT_LOGIN)
            .orElseThrow(() -> new RuntimeException("User not found after creation"));
        System.out.println("Created user with ID: " + savedUser.getId() + " and login: " + savedUser.getLogin());

        // Create a new task (user will be set automatically by the service)
        task = new Task();
        task.setDescription(DEFAULT_DESCRIPTION);
        task.setDueDate(DEFAULT_DUE_DATE);
        task.setPriority(DEFAULT_PRIORITY);
        task.setCompleted(DEFAULT_COMPLETED);
        // Don't set user - let the service set it from security context

        // Set up security context with the same login as the created user
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            savedUser.getLogin(), // Use the actual user login from the created user
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Transactional
    void createTask() throws Exception {
        int databaseSizeBeforeCreate = taskRepository.findAll().size();

        mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(task)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.priority").value(DEFAULT_PRIORITY.toString()))
            .andExpect(jsonPath("$.completed").value(DEFAULT_COMPLETED));

        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeCreate + 1);
        Task testTask = taskList.get(taskList.size() - 1);
        assertThat(testTask.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testTask.getPriority()).isEqualTo(DEFAULT_PRIORITY);
        assertThat(testTask.getCompleted()).isEqualTo(DEFAULT_COMPLETED);
        assertThat(testTask.getUser().getLogin()).isEqualTo(DEFAULT_LOGIN);
    }

    @Test
    void createTaskWithExistingId() throws Exception {
        task.setId(1L);
        // Clear the user reference to avoid foreign key constraint issues
        task.setUser(null);

        mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(task)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void checkDescriptionIsRequired() throws Exception {
        task.setDescription(null);

        mockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(task)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getAllTasks() throws Exception {
        task = taskService.createTask(task);

        mockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(task.getId().intValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY.toString())))
            .andExpect(jsonPath("$.[*].completed").value(hasItem(DEFAULT_COMPLETED)));
    }

    @Test
    void getTask() throws Exception {
        task = taskService.createTask(task);

        mockMvc
            .perform(get(ENTITY_API_URL_ID, task.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(task.getId().intValue()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.priority").value(DEFAULT_PRIORITY.toString()))
            .andExpect(jsonPath("$.completed").value(DEFAULT_COMPLETED));
    }

    @Test
    void getNonExistingTask() throws Exception {
        mockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void updateTask() throws Exception {
        task = taskService.createTask(task);

        Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        updatedTask.setDescription(UPDATED_DESCRIPTION);
        updatedTask.setDueDate(UPDATED_DUE_DATE);
        updatedTask.setPriority(UPDATED_PRIORITY);
        updatedTask.setCompleted(UPDATED_COMPLETED);

        mockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedTask.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedTask))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value(UPDATED_DESCRIPTION))
            .andExpect(jsonPath("$.priority").value(UPDATED_PRIORITY.toString()))
            .andExpect(jsonPath("$.completed").value(UPDATED_COMPLETED));
    }

    @Test
    void updateNonExistingTask() throws Exception {
        task.setId(Long.MAX_VALUE);

        mockMvc
            .perform(
                put(ENTITY_API_URL_ID, task.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(task))
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask() throws Exception {
        task = taskService.createTask(task);

        int databaseSizeBeforeDelete = taskRepository.findAll().size();

        mockMvc.perform(delete(ENTITY_API_URL_ID, task.getId())).andExpect(status().isNoContent());

        List<Task> taskList = taskRepository.findAll();
        assertThat(taskList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    void toggleTaskCompletion() throws Exception {
        task = taskService.createTask(task);

        mockMvc
            .perform(put(ENTITY_API_URL_ID + "/toggle", task.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(true));

        Task toggledTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(toggledTask.getCompleted()).isTrue();
    }

    @Test
    void getFilteredTasks() throws Exception {
        task = taskService.createTask(task);

        mockMvc
            .perform(
                get(ENTITY_API_URL + "/filter")
                    .param("priority", DEFAULT_PRIORITY.toString())
                    .param("completed", DEFAULT_COMPLETED.toString())
                    .param("startDate", DEFAULT_DUE_DATE.toString())
                    .param("endDate", DEFAULT_DUE_DATE.plusDays(1).toString())
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(task.getId().intValue())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY.toString())))
            .andExpect(jsonPath("$.[*].completed").value(hasItem(DEFAULT_COMPLETED)));
    }
}
