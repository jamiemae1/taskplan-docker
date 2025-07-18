package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.Task;
import com.mycompany.myapp.domain.enumeration.TaskPriority;
import com.mycompany.myapp.repository.TaskRepository;
import com.mycompany.myapp.service.TaskService;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import com.mycompany.myapp.web.rest.errors.TaskConcurrencyException;
import com.mycompany.myapp.web.rest.errors.TaskNotFoundException;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api")
public class TaskResource {

    private final Logger log = LoggerFactory.getLogger(TaskResource.class);

    private static final String ENTITY_NAME = "task";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TaskService taskService;
    private final TaskRepository taskRepository;

    public TaskResource(TaskService taskService, TaskRepository taskRepository) {
        this.taskService = taskService;
        this.taskRepository = taskRepository;
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) throws URISyntaxException {
        log.debug("REST request to save Task : {}", task);
        if (task.getId() != null) {
            throw new BadRequestAlertException("A new task cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Task result = taskService.createTask(task);
        return ResponseEntity.created(new URI("/api/tasks/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @PutMapping("/tasks")
    public ResponseEntity<Task> updateTask(@Valid @RequestBody Task task) throws URISyntaxException {
        log.debug("REST request to update Task : {}", task);
        if (task.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        if (!taskRepository.existsById(task.getId())) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        try {
            Task result = taskService.updateTask(task);
            return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, task.getId().toString()))
                .body(result);
        } catch (Exception e) {
            throw new TaskConcurrencyException("Task was modified by another user");
        }
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody Task task)
        throws URISyntaxException {
        log.debug("REST request to update Task : {}, {}", id, task);
        if (task.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, task.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with id " + id);
        }

        try {
            Task result = taskService.updateTask(task);
            return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, task.getId().toString()))
                .body(result);
        } catch (Exception e) {
            throw new TaskConcurrencyException("Task was modified by another user");
        }
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getAllTasks(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(required = false) Boolean currentUserOnly
    ) {
        log.debug("REST request to get all Tasks");
        if (currentUserOnly != null && currentUserOnly) {
            // Return only current user's tasks
            List<Task> tasks = taskService.getAllTasksForCurrentUser();
            return ResponseEntity.ok().body(tasks);
        } else {
            // Return paginated tasks (existing behavior)
            Page<Task> page = taskService.getAllTasks(pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        }
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        log.debug("REST request to get Task : {}", id);
        Optional<Task> task = taskService.getTask(id);
        if (task.isEmpty()) {
            throw new TaskNotFoundException("Task not found with id " + id);
        }
        return ResponseEntity.ok(task.orElseThrow(() -> new TaskNotFoundException("Task not found with id " + id)));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.debug("REST request to delete Task : {}", id);
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with id " + id);
        }
        taskService.deleteTask(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @PutMapping("/tasks/{id}/toggle")
    public ResponseEntity<Task> toggleTaskCompletion(@PathVariable Long id) {
        log.debug("REST request to toggle Task completion : {}", id);
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with id " + id);
        }
        Task result = taskService.toggleTaskCompletion(id);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/tasks/filter")
    public ResponseEntity<List<Task>> getFilteredTasks(
        @RequestParam(required = false) TaskPriority priority,
        @RequestParam(required = false) Boolean completed,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate
    ) {
        log.debug("REST request to get filtered Tasks");
        List<Task> tasks = taskService.getFilteredTasks(priority, completed, startDate, endDate);
        return ResponseEntity.ok().body(tasks);
    }

    @GetMapping("/tasks/search")
    public ResponseEntity<List<Task>> searchTasks(@RequestParam String query) {
        log.debug("REST request to search Tasks with query: {}", query);
        List<Task> tasks = taskService.searchTasks(query);
        return ResponseEntity.ok().body(tasks);
    }

    @PatchMapping("/tasks")
    public ResponseEntity<Task> partialUpdateTask(@RequestBody Task task) {
        log.debug("REST request to partially update Task : {}", task);
        if (task.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        Task result = taskService.partialUpdateTask(task);
        return ResponseEntity.ok().body(result);
    }

    @PatchMapping("/tasks/{id}/toggle-completion")
    public ResponseEntity<Task> toggleTaskCompletionPatch(@PathVariable Long id) {
        log.debug("REST request to toggle Task completion : {}", id);
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with id " + id);
        }
        Task result = taskService.toggleTaskCompletion(id);
        return ResponseEntity.ok().body(result);
    }
}
