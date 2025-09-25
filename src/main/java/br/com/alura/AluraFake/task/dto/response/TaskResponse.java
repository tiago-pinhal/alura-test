package br.com.alura.AluraFake.task.dto.response;

import br.com.alura.AluraFake.task.Type;
import java.time.LocalDateTime;
import java.util.List;

public class TaskResponse {

    private Long id;
    private Long courseId;
    private String statement;
    private Integer order;
    private Type type;
    private LocalDateTime createdAt;
    private List<TaskOptionResponse> options;

    // Constructors
    public TaskResponse() {}

    public TaskResponse(Long id, Long courseId, String statement, Integer order,
                        Type type, LocalDateTime createdAt, List<TaskOptionResponse> options) {
        this.id = id;
        this.courseId = courseId;
        this.statement = statement;
        this.order = order;
        this.type = type;
        this.createdAt = createdAt;
        this.options = options;
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<TaskOptionResponse> getOptions() { return options; }
    public void setOptions(List<TaskOptionResponse> options) { this.options = options; }
}
