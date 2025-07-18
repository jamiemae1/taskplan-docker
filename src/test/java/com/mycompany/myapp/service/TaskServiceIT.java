package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;

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
import java.time.LocalDate;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(classes = { TaskplanDockerApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@TestPropertySource(properties = { "spring.profiles.active=test" })
class TaskServiceIT {

    private static final String DEFAULT_LOGIN = "johndoe";
    private static final String DEFAULT_DESCRIPTION = "Task 1";
    private static final LocalDate DEFAULT_DUE_DATE = LocalDate.now();
    private static final TaskPriority DEFAULT_PRIORITY = TaskPriority.HIGH;
    private static final boolean DEFAULT_COMPLETED = false;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

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
        user.setEmail("johndoe@localhost");
        user.setFirstName("john");
        user.setLastName("doe");
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
    void testCreateTask() {
        Task result = taskService.createTask(task);
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(result.getUser().getLogin()).isEqualTo(DEFAULT_LOGIN);
    }

    @Test
    @Transactional
    void testUpdateTask() {
        Task savedTask = taskService.createTask(task);
        savedTask.setDescription("Updated Task");
        Task result = taskService.updateTask(savedTask);
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Updated Task");
    }

    @Test
    @Transactional
    void testGetAllTasks() {
        taskService.createTask(task);
        Page<Task> result = taskService.getAllTasks(PageRequest.of(0, 10));
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void testGetFilteredTasks() {
        taskService.createTask(task);
        List<Task> result = taskService.getFilteredTasks(
            DEFAULT_PRIORITY,
            DEFAULT_COMPLETED,
            DEFAULT_DUE_DATE.minusDays(1),
            DEFAULT_DUE_DATE.plusDays(1)
        );
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void testToggleTaskCompletion() {
        Task savedTask = taskService.createTask(task);
        assertThat(savedTask.getCompleted()).isFalse();

        Task toggledTask = taskService.toggleTaskCompletion(savedTask.getId());
        assertThat(toggledTask).isNotNull();
        assertThat(toggledTask.getCompleted()).isTrue();

        toggledTask = taskService.toggleTaskCompletion(savedTask.getId());
        assertThat(toggledTask).isNotNull();
        assertThat(toggledTask.getCompleted()).isFalse();
    }

    @Test
    @Transactional
    void testDeleteTask() {
        Task savedTask = taskService.createTask(task);
        taskService.deleteTask(savedTask.getId());
        assertThat(taskRepository.findById(savedTask.getId())).isEmpty();
    }
}
