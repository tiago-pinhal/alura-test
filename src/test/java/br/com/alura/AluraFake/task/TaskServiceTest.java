package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.dto.request.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.request.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.request.SingleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.request.TaskOptionRequest;
import br.com.alura.AluraFake.task.dto.response.TaskResponse;
import br.com.alura.AluraFake.user.Role;
import br.com.alura.AluraFake.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private TaskService taskService;

    private Course validCourse;

    @BeforeEach
    void setUp() {
        User instructor = new User("Tiago Pinhal", "tiago@email.com", Role.INSTRUCTOR);
        validCourse = new Course("Java Basics", "Learn Java fundamentals", instructor);
    }

    @Nested
    class OpenTextTaskTests {

        @Test
        void shouldCreateOpenTextTaskSuccessfully() {
            // Given
            Long courseId = 1L;
            OpenTextTaskRequest request = new OpenTextTaskRequest(courseId, "What did you learn?", 1);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.existsByCourseIdAndStatement(courseId, "What did you learn?")).thenReturn(false);
            when(taskRepository.findMaxOrderByCourseId(courseId)).thenReturn(0);
            when(taskRepository.findByCourseIdAndOrder(courseId, 1)).thenReturn(Optional.empty());
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                task.setId(1L);
                return task;
            });

            // When
            TaskResponse response = taskService.createOpenTextTask(request);

            // Then
            assertNotNull(response);
            assertEquals(courseId, response.getCourseId());
            assertEquals("What did you learn?", response.getStatement());
            assertEquals(1, response.getOrder());
            assertEquals(Type.OPEN_TEXT, response.getType());
            assertNull(response.getOptions());

            verify(taskRepository).save(any(Task.class));
        }

        @Test
        void shouldThrowExceptionWhenCourseNotFound() {
            // Given
            Long invalidCourseId = 999L;
            OpenTextTaskRequest request = new OpenTextTaskRequest(invalidCourseId, "What did you learn?", 1);
            when(courseRepository.findById(invalidCourseId)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.createOpenTextTask(request)
            );
            assertEquals("Course not found", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenCourseNotInBuildingStatus() {
            // Given
            Long courseId = 1L;
            validCourse.setStatus(Status.PUBLISHED);
            OpenTextTaskRequest request = new OpenTextTaskRequest(courseId, "What did you learn?", 1);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.createOpenTextTask(request)
            );
            assertEquals("Cannot add tasks to course. Course status must be BUILDING", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenStatementAlreadyExists() {
            // Given
            Long courseId = 1L;
            OpenTextTaskRequest request = new OpenTextTaskRequest(courseId, "What did you learn?", 1);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(validCourse));
            when(taskRepository.existsByCourseIdAndStatement(courseId, "What did you learn?")).thenReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> taskService.createOpenTextTask(request));
            assertEquals("Course already has a task with this statement", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenOrderSequenceIsInvalid() {
            // Given
            Long courseId = 1L;
            OpenTextTaskRequest request = new OpenTextTaskRequest(courseId, "What did you learn?", 3);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.existsByCourseIdAndStatement(courseId, "What did you learn?")).thenReturn(false);
            when(taskRepository.findMaxOrderByCourseId(courseId)).thenReturn(1);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> taskService.createOpenTextTask(request)
            );
            assertTrue(exception.getMessage().contains("Invalid order sequence"));
        }

        @Test
        void shouldIncrementOrderWhenPositionIsOccupied() {
            // Given
            Long courseId = 1L;
            OpenTextTaskRequest request = new OpenTextTaskRequest(courseId, "What did you learn?", 2);
            Task existingTask = new Task(courseId, "Existing task", 2, Type.OPEN_TEXT);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.existsByCourseIdAndStatement(courseId, "What did you learn?")).thenReturn(false);
            when(taskRepository.findMaxOrderByCourseId(courseId)).thenReturn(3);
            when(taskRepository.findByCourseIdAndOrder(courseId, 2)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            taskService.createOpenTextTask(request);

            // Then
            verify(taskRepository).incrementOrderFromPosition(courseId, 2);
        }
    }

    @Nested
    class SingleChoiceTaskTests {

        @Test
        void shouldCreateSingleChoiceTaskSuccessfully() {
            // Given
            Long courseId = 1L;
            List<TaskOptionRequest> options = Arrays.asList(
                    new TaskOptionRequest("Java", true),
                    new TaskOptionRequest("Python", false),
                    new TaskOptionRequest("Ruby", false)
            );
            SingleChoiceTaskRequest request = new SingleChoiceTaskRequest(courseId, "What is this language?", 1, options);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.existsByCourseIdAndStatement(courseId, "What is this language?")).thenReturn(false);
            when(taskRepository.findMaxOrderByCourseId(courseId)).thenReturn(0);
            when(taskRepository.findByCourseIdAndOrder(courseId, 1)).thenReturn(Optional.empty());
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                task.setId(1L);
                return task;
            });

            // When
            TaskResponse response = taskService.createSingleChoiceTask(request);

            // Then
            assertNotNull(response);
            assertEquals(Type.SINGLE_CHOICE, response.getType());
            assertEquals(3, response.getOptions().size());

            long correctOptions = response.getOptions().stream()
                    .mapToLong(option -> option.getIsCorrect() ? 1 : 0)
                    .sum();
            assertEquals(1, correctOptions);
        }

        @Test
        void shouldThrowExceptionWhenSingleChoiceHasMultipleCorrectAnswers() {
            // Given
            Long courseId = 1L;
            List<TaskOptionRequest> options = Arrays.asList(
                    new TaskOptionRequest("Java", true),
                    new TaskOptionRequest("Python", true), // Could not be also correct
                    new TaskOptionRequest("Ruby", false)
            );
            SingleChoiceTaskRequest request = new SingleChoiceTaskRequest(courseId, "What is this language?", 1, options);

            when(courseRepository.findById(1L)).thenReturn(Optional.of(validCourse));
            when(taskRepository.existsByCourseIdAndStatement(courseId, "What is this language?")).thenReturn(false);
            when(taskRepository.findMaxOrderByCourseId(courseId)).thenReturn(0);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> taskService.createSingleChoiceTask(request));
            assertEquals("Single choice task must have exactly one correct option", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenSingleChoiceHasNoCorrectAnswer() {
            // Given
            Long courseId = 1L;
            List<TaskOptionRequest> options = Arrays.asList(
                    new TaskOptionRequest("Java", false),
                    new TaskOptionRequest("Python", false),
                    new TaskOptionRequest("Ruby", false)
            );
            SingleChoiceTaskRequest request = new SingleChoiceTaskRequest(courseId, "What is this language?", 1, options);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.existsByCourseIdAndStatement(courseId, "What is this language?")).thenReturn(false);
            when(taskRepository.findMaxOrderByCourseId(courseId)).thenReturn(0);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> taskService.createSingleChoiceTask(request));
            assertEquals("Single choice task must have exactly one correct option", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenOptionsAreDuplicate() {
            // Given
            Long courseId = 1L;
            List<TaskOptionRequest> options = Arrays.asList(
                    new TaskOptionRequest("Java", true),
                    new TaskOptionRequest("Java", false), // Duplicated
                    new TaskOptionRequest("Ruby", false)
            );
            SingleChoiceTaskRequest request = new SingleChoiceTaskRequest(courseId, "What is this language?", 1, options);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.existsByCourseIdAndStatement(courseId, "What is this language?")).thenReturn(false);
            when(taskRepository.findMaxOrderByCourseId(courseId)).thenReturn(0);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> taskService.createSingleChoiceTask(request));
            assertEquals("Options cannot be identical", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenOptionEqualsStatement() {
            // Given
            Long courseId = 1L;
            List<TaskOptionRequest> options = Arrays.asList(
                    new TaskOptionRequest("What is this language?", true), // Equals to the statement
                    new TaskOptionRequest("Python", false),
                    new TaskOptionRequest("Ruby", false)
            );
            SingleChoiceTaskRequest request = new SingleChoiceTaskRequest(courseId, "What is this language?", 1, options);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.existsByCourseIdAndStatement(courseId, "What is this language?")).thenReturn(false);
            when(taskRepository.findMaxOrderByCourseId(courseId)).thenReturn(0);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> taskService.createSingleChoiceTask(request));
            assertEquals("Options cannot be identical to the task statement", exception.getMessage());
        }
    }

    @Nested
    class MultipleChoiceTaskTests {
        @Test
        void shouldCreateMultipleChoiceTaskSuccessfully() {
            // Given
            Long courseId = 1L;
            List<TaskOptionRequest> optionsCorrects = Arrays.asList(
                    new TaskOptionRequest("Java", true),
                    new TaskOptionRequest("Spring", true),
                    new TaskOptionRequest("Ruby", false)
            );
            MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest(courseId, "What technologies?", 1, optionsCorrects);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.existsByCourseIdAndStatement(courseId, "What technologies?")).thenReturn(false);
            when(taskRepository.findMaxOrderByCourseId(courseId)).thenReturn(0);
            when(taskRepository.findByCourseIdAndOrder(courseId, 1)).thenReturn(Optional.empty());
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                task.setId(1L);
                return task;
            });

            // When
            TaskResponse response = taskService.createMultipleChoiceTask(request);

            // Then
            assertNotNull(response);
            assertEquals(Type.MULTIPLE_CHOICE, response.getType());

            long correctOptions = response.getOptions().stream()
                    .mapToLong(option -> option.getIsCorrect() ? 1 : 0)
                    .sum();
            assertEquals(2, correctOptions);
        }

        @Test
        void shouldThrowExceptionWhenMultipleChoiceHasLessThanTwoCorrectAnswers() {
            // Given
            Long courseId = 1L;
            List<TaskOptionRequest> optionsWithOneCorrect = Arrays.asList(
                    new TaskOptionRequest("Java", true),
                    new TaskOptionRequest("Python", false),
                    new TaskOptionRequest("Ruby", false)
            );
            MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest(courseId, "What technologies?", 1, optionsWithOneCorrect);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.existsByCourseIdAndStatement(courseId, "What technologies?")).thenReturn(false);
            when(taskRepository.findMaxOrderByCourseId(courseId)).thenReturn(0);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> taskService.createMultipleChoiceTask(request));
            assertEquals("Multiple choice task must have at least 2 correct options", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenMultipleChoiceHasNoIncorrectAnswers() {
            // Given
            Long courseId = 1L;
            List<TaskOptionRequest> options = Arrays.asList(
                    new TaskOptionRequest("Java", true),
                    new TaskOptionRequest("Spring", true),
                    new TaskOptionRequest("Hibernate", true)
            );
            MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest(courseId, "What technologies?", 1, options);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.existsByCourseIdAndStatement(courseId, "What technologies?")).thenReturn(false);
            when(taskRepository.findMaxOrderByCourseId(courseId)).thenReturn(0);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> taskService.createMultipleChoiceTask(request));
            assertEquals("Multiple choice task must have at least 1 incorrect option", exception.getMessage());
        }
    }

    @Nested
    class FindTaskTests {
        @Test
        void shouldFindTaskByIdSuccessfully() {
            // Given
            Long courseId = 1L;
            Long taskId = 1L;
            Task task = new Task(courseId, "What is Java?", 1, Type.OPEN_TEXT);
            task.setId(taskId);

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // When
            TaskResponse response = taskService.findTaskById(taskId);

            // Then
            assertNotNull(response);
            assertEquals(taskId, response.getId());
            assertEquals("What is Java?", response.getStatement());
            assertEquals(Type.OPEN_TEXT, response.getType());
        }

        @Test
        void shouldThrowExceptionWhenTaskNotFoundById() {
            // Given
            Long invalidId = 999L;
            when(taskRepository.findById(invalidId)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> taskService.findTaskById(invalidId));
            assertEquals("Task not found with id: 999", exception.getMessage());
        }

        @Test
        void shouldFindAllTasksSuccessfully() {
            // Given
            Task task1 = new Task(1L, "What is Java?", 1, Type.OPEN_TEXT);
            task1.setId(1L);
            Task task2 = new Task(1L, "Choose language", 2, Type.SINGLE_CHOICE);
            task2.setId(2L);

            when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2));

            // When
            List<TaskResponse> responses = taskService.findAllTasks();

            // Then
            assertNotNull(responses);
            assertEquals(2, responses.size());
            assertEquals("What is Java?", responses.get(0).getStatement());
            assertEquals("Choose language", responses.get(1).getStatement());
        }

        @Test
        void shouldReturnEmptyListWhenNoTasksFound() {
            // Given
            when(taskRepository.findAll()).thenReturn(List.of());

            // When
            List<TaskResponse> responses = taskService.findAllTasks();

            // Then
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
        }
    }
}