package br.com.alura.AluraFake.task.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskOptionRequest {

    @NotNull(message = "Option text cannot be null")
    @Size(min = 4, max = 80, message = "Option must be between 4 and 80 characters")
    private String option;

    @NotNull(message = "IsCorrect cannot be null")
    private Boolean isCorrect;

    // Constructors
    public TaskOptionRequest() {}

    public TaskOptionRequest(String option, Boolean isCorrect) {
        this.option = option;
        this.isCorrect = isCorrect;
    }

    // Getters and Setters
    public String getOption() { return option; }
    public void setOption(String option) { this.option = option; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
}