package br.com.alura.AluraFake.task.dto.response;

import br.com.alura.AluraFake.course.Status;
import java.time.LocalDateTime;

public class PublishCourseResponse {

    private Long id;
    private String title;
    private Status status;
    private LocalDateTime publishedAt;

    // Constructors
    public PublishCourseResponse() {
    }

    public PublishCourseResponse(Long id, String title, Status status, LocalDateTime publishedAt) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.publishedAt = publishedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}