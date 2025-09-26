package br.com.alura.AluraFake.exception;

public class InstructorNotFoundException extends RuntimeException {
    public InstructorNotFoundException(String message) {
        super(message);
    }

    public InstructorNotFoundException(Long instructorId) {
        super("Instructor not found with id: " + instructorId);
    }
}
