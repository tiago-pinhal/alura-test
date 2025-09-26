package br.com.alura.AluraFake.exception;

public class InvalidCourseStateException extends RuntimeException {
    public InvalidCourseStateException(String message) {
        super(message);
    }
}
