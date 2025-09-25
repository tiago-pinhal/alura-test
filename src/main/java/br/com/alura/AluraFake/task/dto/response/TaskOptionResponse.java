package br.com.alura.AluraFake.task.dto.response;

public class TaskOptionResponse {

    private Long id;
    private String option;
    private Boolean isCorrect;

    // Constructors
    public TaskOptionResponse() {}

    public TaskOptionResponse(Long id, String option, Boolean isCorrect) {
        this.id = id;
        this.option = option;
        this.isCorrect = isCorrect;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOption() { return option; }
    public void setOption(String option) { this.option = option; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
}
