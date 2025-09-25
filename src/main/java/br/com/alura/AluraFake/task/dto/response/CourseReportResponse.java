package br.com.alura.AluraFake.task.dto.response;

import br.com.alura.AluraFake.course.Status;
import java.time.LocalDateTime;

public class CourseReportResponse {

    private Long id;
    private String title;
    private Status status;
    private LocalDateTime publishedAt;
    private Integer taskCount;

    // Constructors
    public CourseReportResponse() {}

    public CourseReportResponse(Long id, String title, Status status,
                                LocalDateTime publishedAt, Integer taskCount) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.publishedAt = publishedAt;
        this.taskCount = taskCount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public Integer getTaskCount() { return taskCount; }
    public void setTaskCount(Integer taskCount) { this.taskCount = taskCount; }
}