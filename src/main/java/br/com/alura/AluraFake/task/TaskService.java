package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.dto.*;
import br.com.alura.AluraFake.task.dto.request.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.request.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.request.SingleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.request.TaskOptionRequest;
import br.com.alura.AluraFake.task.dto.response.TaskOptionResponse;
import br.com.alura.AluraFake.task.dto.response.TaskResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CourseRepository courseRepository;

    public TaskService(TaskRepository taskRepository, CourseRepository courseRepository) {
        this.taskRepository = taskRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public TaskResponse createOpenTextTask(OpenTextTaskRequest request) {
        validateBasicTaskRules(request.getCourseId(), request.getStatement(), request.getOrder());

        Task task = new Task(request.getCourseId(), request.getStatement(), request.getOrder(), Type.OPEN_TEXT);

        handleTaskOrdering(task);
        Task savedTask = taskRepository.save(task);

        return convertToTaskResponse(savedTask);
    }

    @Transactional
    public TaskResponse createSingleChoiceTask(SingleChoiceTaskRequest request) {
        validateBasicTaskRules(request.getCourseId(), request.getStatement(), request.getOrder());
        validateSingleChoiceRules(request.getOptions(), request.getStatement());

        Task task = new Task(request.getCourseId(), request.getStatement(), request.getOrder(), Type.SINGLE_CHOICE);

        handleTaskOrdering(task);
        Task savedTask = taskRepository.save(task);

        // Save options
        List<TaskOption> options = request.getOptions().stream()
                .map(optionRequest -> new TaskOption(optionRequest.getOption(), optionRequest.getIsCorrect(), savedTask))
                .collect(Collectors.toList());

        savedTask.setOptions(options);
        taskRepository.save(savedTask);

        return convertToTaskResponse(savedTask);
    }

    @Transactional
    public TaskResponse createMultipleChoiceTask(MultipleChoiceTaskRequest request) {
        validateBasicTaskRules(request.getCourseId(), request.getStatement(), request.getOrder());
        validateMultipleChoiceRules(request.getOptions(), request.getStatement());

        Task task = new Task(request.getCourseId(), request.getStatement(), request.getOrder(), Type.MULTIPLE_CHOICE);

        handleTaskOrdering(task);
        Task savedTask = taskRepository.save(task);

        // Save options
        List<TaskOption> options = request.getOptions().stream()
                .map(optionRequest -> new TaskOption(optionRequest.getOption(), optionRequest.getIsCorrect(), savedTask))
                .collect(Collectors.toList());

        savedTask.setOptions(options);
        taskRepository.save(savedTask);

        return convertToTaskResponse(savedTask);
    }

    private void validateBasicTaskRules(Long courseId, String statement, Integer order) {
       Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (!Status.BUILDING.equals(course.getStatus())) {
            throw new IllegalArgumentException("Cannot add tasks to course. Course status must be BUILDING");
        }

        // Check if statement already exists for this course
        if (taskRepository.existsByCourseIdAndStatement(courseId, statement)) {
            throw new IllegalArgumentException("Course already has a task with this statement");
        }

        validateSequentialOrdering(courseId, order);
    }

    private void validateSequentialOrdering(Long courseId, Integer newOrder) {
        Integer maxOrder = taskRepository.findMaxOrderByCourseId(courseId);

        // If trying to add with order greater than maxOrder + 1, it's invalid
        if (newOrder > maxOrder + 1) {
            throw new IllegalArgumentException(
                    String.format("Invalid order sequence. Course has tasks up to order %d, cannot add task with order %d",
                            maxOrder, newOrder)
            );
        }
    }

    private void validateSingleChoiceRules(List<TaskOptionRequest> options, String statement) {
        validateCommonOptionRules(options, statement);

        // Must have exactly one correct option
        long correctCount = options.stream().mapToLong(option -> option.getIsCorrect() ? 1 : 0).sum();
        if (correctCount != 1) {
            throw new IllegalArgumentException("Single choice task must have exactly one correct option");
        }
    }

    private void validateMultipleChoiceRules(List<TaskOptionRequest> options, String statement) {
        validateCommonOptionRules(options, statement);

        // Must have at least 2 correct options and at least 1 incorrect
        long correctCount = options.stream().mapToLong(option -> option.getIsCorrect() ? 1 : 0).sum();
        long incorrectCount = options.size() - correctCount;

        if (correctCount < 2) {
            throw new IllegalArgumentException("Multiple choice task must have at least 2 correct options");
        }
        if (incorrectCount < 1) {
            throw new IllegalArgumentException("Multiple choice task must have at least 1 incorrect option");
        }
    }

    private void validateCommonOptionRules(List<TaskOptionRequest> options, String statement) {
        // Check for duplicate options
        Set<String> uniqueOptions = new HashSet<>();
        for (TaskOptionRequest option : options) {
            if (!uniqueOptions.add(option.getOption().trim())) {
                throw new IllegalArgumentException("Options cannot be identical");
            }
        }

        // Check if any option equals the statement
        for (TaskOptionRequest option : options) {
            if (option.getOption().trim().equals(statement.trim())) {
                throw new IllegalArgumentException("Options cannot be identical to the task statement");
            }
        }
    }

    private void handleTaskOrdering(Task task) {
        Long courseId = task.getCourseId();
        Integer newOrder = task.getOrder();

        // Check if position is already occupied
        if (taskRepository.findByCourseIdAndOrder(courseId, newOrder).isPresent()) {
            // Shift existing tasks forward
            taskRepository.incrementOrderFromPosition(courseId, newOrder);
        }
    }

    private TaskResponse convertToTaskResponse(Task task) {
        List<TaskOptionResponse> optionResponses = null;

        if (task.getOptions() != null && !task.getOptions().isEmpty()) {
            optionResponses = task.getOptions().stream()
                    .map(option -> new TaskOptionResponse(option.getId(), option.getOption(), option.getIsCorrect()))
                    .collect(Collectors.toList());
        }

        return new TaskResponse(
                task.getId(),
                task.getCourseId(),
                task.getStatement(),
                task.getOrder(),
                task.getType(),
                task.getCreatedAt(),
                optionResponses
        );
    }
}