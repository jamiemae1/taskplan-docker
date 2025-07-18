package com.mycompany.myapp.web.rest.errors;

import com.mycompany.myapp.domain.Task;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;

@RestController
@RequestMapping("/api/error")
public class TestController {

    @GetMapping("/bad-request")
    public ResponseEntity<Void> testBadRequestAlert() {
        throw new BadRequestAlertException("error.test", "test", "test");
    }

    @PostMapping("/validation")
    public ResponseEntity<Task> testValidation(@Valid @RequestBody Task task) {
        return ResponseEntity.ok(task);
    }

    @GetMapping("/not-found")
    public ResponseEntity<Void> testNotFound() {
        throw new TaskNotFoundException("error.tasknotfound");
    }

    @GetMapping("/concurrency")
    public ResponseEntity<Void> testConcurrency() {
        throw new TaskConcurrencyException("error.taskconcurrency");
    }
}
