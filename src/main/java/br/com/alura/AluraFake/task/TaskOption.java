package br.com.alura.AluraFake.task;

import jakarta.persistence.*;

@Entity
@Table(name = "task_option")
public class TaskOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "option", nullable = false, columnDefinition = "TEXT")
    private String option;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    // Constructors
    public TaskOption() {}

    public TaskOption(String option, Boolean isCorrect) {
        this.option = option;
        this.isCorrect = isCorrect;
    }

    public TaskOption(String option, Boolean isCorrect, Task task) {
        this.option = option;
        this.isCorrect = isCorrect;
        this.task = task;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOption() { return option; }
    public void setOption(String option) { this.option = option; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
}