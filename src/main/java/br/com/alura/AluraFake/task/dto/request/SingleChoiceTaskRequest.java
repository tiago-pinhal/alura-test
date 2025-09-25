package br.com.alura.AluraFake.task.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public class SingleChoiceTaskRequest {

    @NotNull(message = "Course ID cannot be null")
    private Long courseId;

    @NotNull(message = "Statement cannot be null")
    @Size(min = 4, max = 255, message = "Statement must be between 4 and 255 characters")
    private String statement;

    @NotNull(message = "Order cannot be null")
    @Positive(message = "Order must be a positive number")
    private Integer order;

    @NotNull(message = "Options cannot be null")
    @Size(min = 2, max = 5, message = "Single choice task must have between 2 and 5 options")
    @Valid
    private List<TaskOptionRequest> options;

    // Constructors
    public SingleChoiceTaskRequest() {}

    public SingleChoiceTaskRequest(Long courseId, String statement, Integer order, List<TaskOptionRequest> options) {
        this.courseId = courseId;
        this.statement = statement;
        this.order = order;
        this.options = options;
    }

    // Getters and Setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }

    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }

    public List<TaskOptionRequest> getOptions() { return options; }
    public void setOptions(List<TaskOptionRequest> options) { this.options = options; }
}
