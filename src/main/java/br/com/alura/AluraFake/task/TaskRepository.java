package br.com.alura.AluraFake.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Check if statement already exists for a course
    boolean existsByCourseIdAndStatement(Long courseId, String statement);

    List<Task> findByCourseId(Long courseId);

    // Find tasks by course ID with order greater than or equal to given order
    @Query("SELECT t FROM Task t WHERE t.courseId = :courseId AND t.order >= :order ORDER BY t.order ASC")
    List<Task> findByCourseIdAndOrderGreaterThanEqualOrder(@Param("courseId") Long courseId, @Param("order") Integer order);

    // Find maximum order for a course
    @Query("SELECT COALESCE(MAX(t.order), 0) FROM Task t WHERE t.courseId = :courseId")
    Integer findMaxOrderByCourseId(@Param("courseId") Long courseId);

    // Count tasks by course ID
    int countByCourseId(Long courseId);

    // Count tasks by course ID and type
    int countByCourseIdAndType(Long courseId, Type type);

    // Find task with specific order in a course
    Optional<Task> findByCourseIdAndOrder(Long courseId, Integer order);

    // Update order for tasks (increment order by 1)
    @Modifying
    @Query("UPDATE Task t SET t.order = t.order + 1 WHERE t.courseId = :courseId AND t.order >= :fromOrder")
    void incrementOrderFromPosition(@Param("courseId") Long courseId, @Param("fromOrder") Integer fromOrder);

    // Check if there's a gap in sequence (for validation)
    @Query("SELECT CASE WHEN COUNT(t) = :expectedCount THEN true ELSE false END " +
            "FROM Task t WHERE t.courseId = :courseId AND t.order BETWEEN 1 AND :maxOrder")
    boolean hasSequentialOrdering(@Param("courseId") Long courseId, @Param("maxOrder") Integer maxOrder, @Param("expectedCount") Long expectedCount);
}