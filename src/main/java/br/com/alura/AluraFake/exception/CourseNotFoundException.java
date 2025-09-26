package br.com.alura.AluraFake.exception;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(String message) {
        super(message);
    }

    public CourseNotFoundException(Long courseId) {
        super("Course not found with id: " + courseId);
    }
}