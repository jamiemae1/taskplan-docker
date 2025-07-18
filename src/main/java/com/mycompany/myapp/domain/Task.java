package com.mycompany.myapp.domain;

import com.mycompany.myapp.domain.enumeration.TaskPriority;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Task.
 */
@Entity
@Table(name = "task")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 255)
    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private TaskPriority priority;

    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Task id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public Task description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return this.dueDate;
    }

    public Task dueDate(LocalDate dueDate) {
        this.setDueDate(dueDate);
        return this;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public TaskPriority getPriority() {
        return this.priority;
    }

    public Task priority(TaskPriority priority) {
        this.setPriority(priority);
        return this;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Boolean getCompleted() {
        return this.completed;
    }

    public Task completed(Boolean completed) {
        this.setCompleted(completed);
        return this;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed != null ? completed : false;
    }

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Task createdDate(Instant createdDate) {
        this.setCreatedDate(createdDate);
        return this;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public Task lastModifiedDate(Instant lastModifiedDate) {
        this.setLastModifiedDate(lastModifiedDate);
        return this;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Task user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @PrePersist
    protected void onCreate() {
        this.createdDate = Instant.now();
        this.lastModifiedDate = this.createdDate;
        if (this.completed == null) {
            this.completed = false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Task)) {
            return false;
        }
        Task task = (Task) o;
        return id != null && id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return (
            "Task{" +
            "id=" +
            id +
            ", description='" +
            description +
            "'" +
            ", dueDate='" +
            dueDate +
            "'" +
            ", priority=" +
            priority +
            ", completed=" +
            completed +
            ", createdDate='" +
            createdDate +
            "'" +
            ", lastModifiedDate='" +
            lastModifiedDate +
            "'" +
            ", user=" +
            (user != null ? user.getLogin() : "null") +
            "}"
        );
    }
}
