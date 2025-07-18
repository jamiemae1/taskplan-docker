package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.mycompany.myapp.domain.Task;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.TaskPriority;
import com.mycompany.myapp.repository.TaskRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setLogin("test-user");
        user.setEmail("test@example.com");

        task = new Task();
        task.setId(1L);
        task.setDescription("Test Task");
        task.setPriority(TaskPriority.LOW);
        task.setCompleted(false);
        task.setUser(user);
        task.setCreatedDate(Instant.now());
        task.setLastModifiedDate(Instant.now());
    }

    @Test
    void getAllTasksTest() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Task> tasks = new ArrayList<>();
        tasks.add(task);
        Page<Task> page = new PageImpl<>(tasks);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("test-user"));
            when(taskRepository.findByUserLogin("test-user", pageable)).thenReturn(page);

            // When
            Page<Task> result = taskService.getAllTasks(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getDescription()).isEqualTo("Test Task");
        }
    }

    @Test
    void getFilteredTasksTest() {
        // Given
        List<Task> tasks = new ArrayList<>();
        tasks.add(task);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("test-user"));
            when(
                taskRepository.findByUserLoginAndPriorityAndCompletedAndDueDateBetween(
                    eq("test-user"),
                    eq(TaskPriority.LOW),
                    eq(false),
                    any(LocalDate.class),
                    any(LocalDate.class)
                )
            ).thenReturn(tasks);

            // When
            List<Task> result = taskService.getFilteredTasks(TaskPriority.LOW, false, LocalDate.now(), LocalDate.now().plusDays(7));

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDescription()).isEqualTo("Test Task");
        }
    }

    @Test
    void createTaskTest() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("test-user"));
            when(userService.getUserWithAuthoritiesByLogin("test-user")).thenReturn(Optional.of(user));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            Task result = taskService.createTask(task);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isEqualTo("Test Task");
            assertThat(result.getUser().getLogin()).isEqualTo("test-user");
            assertThat(result.getCreatedDate()).isNotNull();
            assertThat(result.getLastModifiedDate()).isNotNull();
        }
    }

    @Test
    void updateTaskTest() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("test-user"));
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            Task result = taskService.updateTask(task);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isEqualTo("Test Task");
            assertThat(result.getLastModifiedDate()).isNotNull();
        }
    }

    @Test
    void toggleTaskCompletionTest() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("test-user"));
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // When
            Task result = taskService.toggleTaskCompletion(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCompleted()).isTrue();
            assertThat(result.getLastModifiedDate()).isNotNull();
        }
    }
}
