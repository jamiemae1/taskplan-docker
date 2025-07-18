package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Task;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.TaskPriority;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Task entity.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("select task from Task task where task.user.login = ?#{principal.username}")
    Page<Task> findByUserIsCurrentUser(Pageable pageable);

    @Query("select task from Task task where task.user.login = ?#{principal.username} and task.completed = ?1")
    Page<Task> findByUserIsCurrentUserAndCompleted(boolean completed, Pageable pageable);

    @Query("select task from Task task left join fetch task.user")
    Page<Task> findAllWithEagerRelationships(Pageable pageable);

    @Query("select task from Task task left join fetch task.user where task.id =:id")
    Optional<Task> findOneWithEagerRelationships(@Param("id") Long id);

    Page<Task> findAllByPriority(TaskPriority priority, Pageable pageable);

    Page<Task> findAllByOrderByDueDateAsc(Pageable pageable);

    Page<Task> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

    Page<Task> findByDueDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Task> findByUserAndCompleted(User user, boolean completed, Pageable pageable);

    /**
     * Find all tasks for a given user.
     *
     * @param user the user
     * @param pageable the pagination information
     * @return the list of tasks
     */
    Page<Task> findByUser(User user, Pageable pageable);

    /**
     * Find all tasks for a given user.
     *
     * @param user the user
     * @return the list of tasks
     */
    List<Task> findByUser(User user);

    /**
     * Find all tasks for a given user login.
     *
     * @param login the user's login
     * @return the list of tasks
     */
    List<Task> findByUserLogin(String login);

    Page<Task> findByUserLogin(String login, Pageable pageable);

    List<Task> findByUserLoginAndPriorityAndCompletedAndDueDateBetween(
        String login,
        TaskPriority priority,
        Boolean completed,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Find tasks by user login and description containing the given query (case insensitive).
     *
     * @param login the user's login
     * @param query the search query
     * @return the list of tasks
     */
    List<Task> findByUserLoginAndDescriptionContainingIgnoreCase(String login, String query);

    /**
     * Delete all tasks for a given user login.
     *
     * @param login the user's login
     */
    void deleteAllByUserLogin(String login);
}
