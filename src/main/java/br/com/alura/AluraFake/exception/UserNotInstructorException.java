package br.com.alura.AluraFake.exception;

public class UserNotInstructorException extends RuntimeException {
    public UserNotInstructorException(String message) {
        super(message);
    }

    public UserNotInstructorException() {
        super("User is not an instructor");
    }
}
