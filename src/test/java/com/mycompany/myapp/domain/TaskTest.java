package com.mycompany.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.domain.enumeration.TaskPriority;
import com.mycompany.myapp.web.rest.TestUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskTest {

    private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validatorFactory.getValidator();

    private Task task1;
    private Task task2;

    @BeforeEach
    public void setup() {
        task1 = new Task().id(1L).description("Task 1").dueDate(LocalDate.of(2025, 7, 10)).priority(TaskPriority.HIGH).completed(false);

        task2 = new Task().id(1L).description("Task 1").dueDate(LocalDate.of(2025, 7, 10)).priority(TaskPriority.HIGH).completed(false);
    }

    @Test
    void testTaskEquals() {
        assertThat(task1).isEqualTo(task2);
        assertThat(task1.hashCode()).isEqualTo(task2.hashCode());
    }

    @Test
    void testTaskValidation() {
        Task task = new Task();
        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertThat(violations).hasSize(3); // description (NotNull, NotBlank), priority (NotNull)

        // Verify specific violations
        assertThat(violations)
            .extracting(ConstraintViolation::getPropertyPath)
            .extracting(Path::toString)
            .containsOnly("description", "description", "priority");

        assertThat(violations)
            .extracting(ConstraintViolation::getMessage)
            .containsOnly("must not be blank", "must not be null", "must not be null");
    }
}
