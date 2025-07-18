package com.mycompany.myapp.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mycompany.myapp.domain.Task;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.TaskPriority;
import com.mycompany.myapp.repository.TaskRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.TaskService;
import com.mycompany.myapp.service.UserService;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class TaskManagementStepDefs {

    private static final String DEFAULT_LOGIN = "test-user";

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private Task task;
    private User user;
    private List<Task> tasks;
    private Page<Task> taskPage;
    private Exception lastException;

    @Before
    public void setup() {
        // Check if user already exists, if not create it
        Optional<User> existingUser = userRepository.findOneByLogin(DEFAULT_LOGIN);
        if (existingUser.isPresent()) {
            user = existingUser.orElseThrow(() -> new RuntimeException("User should exist"));
        } else {
            user = new User();
            user.setLogin(DEFAULT_LOGIN);
            user.setPassword(RandomStringUtils.randomAlphanumeric(60));
            user.setActivated(true);
            user.setEmail("test@example.com");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setLangKey("en");
            user = userRepository.saveAndFlush(user);
        }

        // Set up security context
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            DEFAULT_LOGIN,
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        tasks = new ArrayList<>();
        lastException = null;
    }

    @Given("a task with description {string}")
    public void aTaskWithDescription(String description) {
        task = new Task();
        task.setDescription(description);
        task.setDueDate(LocalDate.now());
        task.setPriority(TaskPriority.MEDIUM);
        task.setCompleted(false);
        // Don't set user - let the service set it from security context
        task = taskService.createTask(task);
    }

    @Given("a task with priority {string}")
    public void aTaskWithPriority(String priority) {
        task = new Task();
        task.setDescription("Test Task");
        task.setDueDate(LocalDate.now());
        task.setPriority(TaskPriority.valueOf(priority));
        task.setCompleted(false);
        // Don't set user - let the service set it from security context
        task = taskService.createTask(task);
    }

    @Given("a task with due date {string}")
    public void aTaskWithDueDate(String dueDate) {
        task = new Task();
        task.setDescription("Test Task");
        task.setDueDate(LocalDate.parse(dueDate));
        task.setPriority(TaskPriority.MEDIUM);
        task.setCompleted(false);
        // Don't set user - let the service set it from security context
        task = taskService.createTask(task);
    }

    @When("I update the task description to {string}")
    public void iUpdateTheTaskDescriptionTo(String description) {
        task.setDescription(description);
        task = taskService.updateTask(task);
    }

    @When("I update the task priority to {string}")
    public void iUpdateTheTaskPriorityTo(String priority) {
        task.setPriority(TaskPriority.valueOf(priority));
        task = taskService.updateTask(task);
    }

    @When("I update the task due date to {string}")
    public void iUpdateTheTaskDueDateTo(String dueDate) {
        task.setDueDate(LocalDate.parse(dueDate));
        task = taskService.updateTask(task);
    }

    @When("I toggle the task completion status")
    public void iToggleTheTaskCompletionStatus() {
        task = taskService.toggleTaskCompletion(task.getId());
    }

    @When("I delete the task")
    public void iDeleteTheTask() {
        taskService.deleteTask(task.getId());
    }

    @When("I get all tasks")
    public void iGetAllTasks() {
        taskPage = taskService.getAllTasks(PageRequest.of(0, 10));
    }

    @When("I get filtered tasks with priority {string} and completed {string}")
    public void iGetFilteredTasks(String priority, String completed) {
        tasks = taskService.getFilteredTasks(
            TaskPriority.valueOf(priority),
            Boolean.parseBoolean(completed),
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(1)
        );
    }

    @Then("the task description should be {string}")
    public void theTaskDescriptionShouldBe(String description) {
        assertThat(task.getDescription()).isEqualTo(description);
    }

    @Then("the task priority should be {string}")
    public void theTaskPriorityShouldBe(String priority) {
        assertThat(task.getPriority()).isEqualTo(TaskPriority.valueOf(priority));
    }

    @Then("the task due date should be {string}")
    public void theTaskDueDateShouldBe(String dueDate) {
        assertThat(task.getDueDate()).isEqualTo(LocalDate.parse(dueDate));
    }

    @Then("the task should be completed")
    public void theTaskShouldBeCompleted() {
        assertThat(task.getCompleted()).isTrue();
    }

    @Then("the task should not be completed")
    public void theTaskShouldNotBeCompleted() {
        assertThat(task.getCompleted()).isFalse();
    }

    @Then("the task should not exist")
    public void theTaskShouldNotExist() {
        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }

    @Then("I should get {int} tasks")
    public void iShouldGetTasks(int count) {
        if (taskPage != null) {
            assertThat(taskPage.getContent()).hasSize(count);
        } else {
            assertThat(tasks).hasSize(count);
        }
    }

    @Then("the task list should contain a task with description {string}")
    public void theTaskListShouldContainATaskWithDescription(String description) {
        if (taskPage != null) {
            assertThat(taskPage.getContent().stream().anyMatch(t -> t.getDescription().equals(description))).isTrue();
        } else {
            assertThat(tasks.stream().anyMatch(t -> t.getDescription().equals(description))).isTrue();
        }
    }

    @Then("I should get an error {string}")
    public void iShouldGetAnError(String errorMessage) {
        assertThat(lastException).isInstanceOf(BadRequestAlertException.class);
        assertThat(((BadRequestAlertException) lastException).getMessage()).isEqualTo(errorMessage);
    }
}
