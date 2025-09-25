package br.com.alura.AluraFake.task.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class OpenTextTaskRequest {

    @NotNull(message = "Course ID cannot be null")
    private Long courseId;

    @NotNull(message = "Statement cannot be null")
    @Size(min = 4, max = 255, message = "Statement must be between 4 and 255 characters")
    private String statement;

    @NotNull(message = "Order cannot be null")
    @Positive(message = "Order must be a positive number")
    private Integer order;

    // Constructors
    public OpenTextTaskRequest() {}

    public OpenTextTaskRequest(Long courseId, String statement, Integer order) {
        this.courseId = courseId;
        this.statement = statement;
        this.order = order;
    }

    // Getters and Setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }

    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
}