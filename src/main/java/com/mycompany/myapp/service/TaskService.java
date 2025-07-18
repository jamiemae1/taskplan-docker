package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Task;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.TaskPriority;
import com.mycompany.myapp.repository.TaskRepository;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import com.mycompany.myapp.web.rest.errors.TaskConcurrencyException;
import com.mycompany.myapp.web.rest.errors.TaskNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

    private final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    private User getCurrentUser() {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Current user not found", "task", "usernotfound"));
        return userService
            .getUserWithAuthoritiesByLogin(currentUserLogin)
            .orElseThrow(() -> new BadRequestAlertException("Current user not found", "task", "usernotfound"));
    }

    private void validateTaskOwnership(Task task) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Current user not found", "task", "usernotfound"));
        if (!task.getUser().getLogin().equals(currentUserLogin)) {
            throw new BadRequestAlertException("Permission denied", "task", "permissiondenied");
        }
    }

    public Task createTask(Task task) {
        log.debug("Request to create Task : {}", task);
        User currentUser = getCurrentUser();
        task.setUser(currentUser);
        task.setCreatedDate(Instant.now());
        task.setLastModifiedDate(task.getCreatedDate());
        if (task.getCompleted() == null) {
            task.setCompleted(false);
        }
        return taskRepository.save(task);
    }

    public Task updateTask(Task task) {
        log.debug("Request to update Task : {}", task);
        Task existingTask = taskRepository.findById(task.getId()).orElseThrow(() -> new TaskNotFoundException());
        validateTaskOwnership(existingTask);

        // Check for concurrent modification
        if (task.getLastModifiedDate() == null || !existingTask.getLastModifiedDate().equals(task.getLastModifiedDate())) {
            throw new TaskConcurrencyException("Task was modified by another user");
        }

        // Preserve the original user and created date
        task.setUser(existingTask.getUser());
        task.setCreatedDate(existingTask.getCreatedDate());
        task.setLastModifiedDate(Instant.now());

        return taskRepository.save(task);
    }

    public Optional<Task> getTask(Long id) {
        log.debug("Request to get Task : {}", id);
        Optional<Task> task = taskRepository.findOneWithEagerRelationships(id);
        if (task.isPresent()) {
            validateTaskOwnership(task.orElseThrow(() -> new TaskNotFoundException()));
        }
        return task;
    }

    public Page<Task> getAllTasks(Pageable pageable) {
        log.debug("Request to get all Tasks");
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Current user not found", "task", "usernotfound"));
        return taskRepository.findByUserLogin(currentUserLogin, pageable);
    }

    public List<Task> getAllTasksForCurrentUser() {
        log.debug("Request to get all Tasks for current user");
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Current user not found", "task", "usernotfound"));
        return taskRepository.findByUserLogin(currentUserLogin);
    }

    public List<Task> getFilteredTasks(TaskPriority priority, Boolean completed, LocalDate startDate, LocalDate endDate) {
        log.debug("Request to get filtered Tasks");
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Current user not found", "task", "usernotfound"));
        if (priority != null && completed != null && startDate != null && endDate != null) {
            return taskRepository.findByUserLoginAndPriorityAndCompletedAndDueDateBetween(
                currentUserLogin,
                priority,
                completed,
                startDate,
                endDate
            );
        } else if (priority != null && completed != null) {
            return taskRepository
                .findByUserLogin(currentUserLogin)
                .stream()
                .filter(t -> t.getPriority() == priority && t.getCompleted().equals(completed))
                .toList();
        } else if (priority != null) {
            return taskRepository.findByUserLogin(currentUserLogin).stream().filter(t -> t.getPriority() == priority).toList();
        } else if (completed != null) {
            return taskRepository.findByUserLogin(currentUserLogin).stream().filter(t -> t.getCompleted().equals(completed)).toList();
        } else if (startDate != null && endDate != null) {
            return taskRepository
                .findByUserLogin(currentUserLogin)
                .stream()
                .filter(t -> t.getDueDate() != null && !t.getDueDate().isBefore(startDate) && !t.getDueDate().isAfter(endDate))
                .toList();
        } else {
            return taskRepository.findByUserLogin(currentUserLogin);
        }
    }

    public Task toggleTaskCompletion(Long id) {
        log.debug("Request to toggle Task completion : {}", id);
        Task task = taskRepository.findById(id).orElseThrow(TaskNotFoundException::new);
        validateTaskOwnership(task);
        task.setCompleted(!task.getCompleted());
        task.setLastModifiedDate(Instant.now());
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        log.debug("Request to delete Task : {}", id);
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException());
        validateTaskOwnership(task);
        taskRepository.deleteById(id);
    }

    public List<Task> searchTasks(String query) {
        log.debug("Request to search Tasks with query: {}", query);
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("Current user not found", "task", "usernotfound"));
        return taskRepository.findByUserLoginAndDescriptionContainingIgnoreCase(currentUserLogin, query);
    }

    public Task partialUpdateTask(Task task) {
        log.debug("Request to partially update Task : {}", task);
        Task existingTask = taskRepository.findById(task.getId()).orElseThrow(() -> new TaskNotFoundException());
        validateTaskOwnership(existingTask);

        // Update only non-null fields
        if (task.getDescription() != null) {
            existingTask.setDescription(task.getDescription());
        }
        if (task.getDueDate() != null) {
            existingTask.setDueDate(task.getDueDate());
        }
        if (task.getPriority() != null) {
            existingTask.setPriority(task.getPriority());
        }
        if (task.getCompleted() != null) {
            existingTask.setCompleted(task.getCompleted());
        }

        existingTask.setLastModifiedDate(Instant.now());
        return taskRepository.save(existingTask);
    }
}
