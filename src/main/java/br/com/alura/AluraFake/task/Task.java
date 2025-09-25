package br.com.alura.AluraFake.task;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "statement"})
)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "statement", nullable = false, columnDefinition = "TEXT")
    private String statement;

    @Column(name = "task_order", nullable = false)
    private Integer order;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type type;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskOption> options = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // Constructors
    public Task() {}

    public Task(Long courseId, String statement, Integer order, Type type) {
        this.courseId = courseId;
        this.statement = statement;
        this.order = order;
        this.type = type;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }

    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public List<TaskOption> getOptions() { return options; }
    public void setOptions(List<TaskOption> options) { this.options = options; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
