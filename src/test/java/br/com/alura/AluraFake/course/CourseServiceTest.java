package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.exception.CourseNotFoundException;
import br.com.alura.AluraFake.exception.InstructorNotFoundException;
import br.com.alura.AluraFake.exception.InvalidCourseStateException;
import br.com.alura.AluraFake.exception.UserNotInstructorException;
import br.com.alura.AluraFake.task.Task;
import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.task.Type;
import br.com.alura.AluraFake.task.dto.response.InstructorCoursesReportResponse;
import br.com.alura.AluraFake.task.dto.response.PublishCourseResponse;
import br.com.alura.AluraFake.user.Role;
import br.com.alura.AluraFake.user.User;
import br.com.alura.AluraFake.user.UserRepository;
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
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CourseService courseService;

    private User instructor;
    private User student;
    private Course validCourse;
    private NewCourseDTO validCourseDTO;

    @BeforeEach
    void setUp() {
        instructor = new User("John Doe", "john@instructor.com", Role.INSTRUCTOR);
        student = new User("Jane Student", "jane@student.com", Role.STUDENT);

        validCourse = new Course("Java Basics", "Learn Java fundamentals", instructor);
        // Status is automatically set to BUILDING in constructor

        validCourseDTO = new NewCourseDTO();
        validCourseDTO.setTitle("Java Basics");
        validCourseDTO.setDescription("Learn Java fundamentals");
        validCourseDTO.setEmailInstructor("john@instructor.com");
    }

    @Nested
    class CreateCourseTests {

        @Test
        void shouldCreateCourseSuccessfully() {
            // Given
            when(userRepository.findByEmail("john@instructor.com"))
                    .thenReturn(Optional.of(instructor));
            when(courseRepository.save(any(Course.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Course result = courseService.createCourse(validCourseDTO);

            // Then
            assertNotNull(result);
            assertEquals("Java Basics", result.getTitle());
            assertEquals("Learn Java fundamentals", result.getDescription());
            assertEquals(instructor, result.getInstructor());
            assertEquals(Status.BUILDING, result.getStatus());

            verify(userRepository).findByEmail("john@instructor.com");
            verify(courseRepository).save(any(Course.class));
        }

        @Test
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findByEmail("invalid@email.com"))
                    .thenReturn(Optional.empty());

            validCourseDTO.setEmailInstructor("invalid@email.com");

            // When & Then
            UserNotInstructorException exception = assertThrows(
                    UserNotInstructorException.class,
                    () -> courseService.createCourse(validCourseDTO)
            );
            assertEquals("User is not an instructor", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenUserIsNotInstructor() {
            // Given
            when(userRepository.findByEmail("jane@student.com"))
                    .thenReturn(Optional.of(student));

            validCourseDTO.setEmailInstructor("jane@student.com");

            // When & Then
            UserNotInstructorException exception = assertThrows(
                    UserNotInstructorException.class,
                    () -> courseService.createCourse(validCourseDTO)
            );
            assertEquals("User is not an instructor", exception.getMessage());
        }
    }

    @Nested
    class GetAllCoursesTests {

        @Test
        void shouldReturnAllCourses() {
            // Given
            Course course1 = new Course("Java Basics", "Learn Java", instructor);
            Course course2 = new Course("Spring Boot", "Learn Spring", instructor);

            when(courseRepository.findAll()).thenReturn(Arrays.asList(course1, course2));

            // When
            List<CourseListItemDTO> result = courseService.getAllCourses();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Java Basics", result.get(0).getTitle());
            assertEquals("Spring Boot", result.get(1).getTitle());
        }

        @Test
        void shouldReturnEmptyListWhenNoCoursesExist() {
            // Given
            when(courseRepository.findAll()).thenReturn(List.of());

            // When
            List<CourseListItemDTO> result = courseService.getAllCourses();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class PublishCourseTests {

        @Test
        void shouldPublishCourseSuccessfully() {
            // Given
            Long courseId = 1L;

            List<Task> tasks = Arrays.asList(
                    new Task(courseId, "Question 1", 1, Type.OPEN_TEXT),
                    new Task(courseId, "Question 2", 2, Type.SINGLE_CHOICE),
                    new Task(courseId, "Question 3", 3, Type.MULTIPLE_CHOICE)
            );

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.findByCourseId(courseId)).thenReturn(tasks);
            when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
                Course course = invocation.getArgument(0);
                course.setAsPublished();
                return course;
            });

            // When
            PublishCourseResponse result = courseService.publishCourse(courseId);

            // Then
            assertNotNull(result);
            assertEquals("Java Basics", result.getTitle());
            assertEquals(Status.PUBLISHED, result.getStatus());
            assertNotNull(result.getPublishedAt());

            verify(courseRepository).save(validCourse);
        }

        @Test
        void shouldThrowExceptionWhenCourseNotFound() {
            // Given
            Long invalidCourseId = 999L;
            when(courseRepository.findById(invalidCourseId)).thenReturn(Optional.empty());

            // When & Then
            CourseNotFoundException exception = assertThrows(
                    CourseNotFoundException.class,
                    () -> courseService.publishCourse(invalidCourseId)
            );
            assertEquals("Course not found with id: 999", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenCourseNotInBuildingStatus() {
            // Given
            Long courseId = 1L;
            validCourse.setStatus(Status.PUBLISHED);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));

            // When & Then
            InvalidCourseStateException exception = assertThrows(
                    InvalidCourseStateException.class,
                    () -> courseService.publishCourse(courseId)
            );
            assertEquals("Course must be in BUILDING status to be published", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenMissingTaskTypes() {
            // Given
            Long courseId = 1L;

            // Missing MULTIPLE_CHOICE type
            List<Task> incompleteTasks = Arrays.asList(
                    new Task(courseId, "Question 1", 1, Type.OPEN_TEXT),
                    new Task(courseId, "Question 2", 2, Type.SINGLE_CHOICE)
            );

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.findByCourseId(courseId)).thenReturn(incompleteTasks);

            // When & Then
            InvalidCourseStateException exception = assertThrows(
                    InvalidCourseStateException.class,
                    () -> courseService.publishCourse(courseId)
            );
            assertTrue(exception.getMessage().contains("Missing: [MULTIPLE_CHOICE]"));
        }

        @Test
        void shouldThrowExceptionWhenTaskSequenceIsInvalid() {
            // Given
            Long courseId = 1L;

            // Invalid sequence: 1, 3, 4 (missing 2)
            List<Task> tasksWithGap = Arrays.asList(
                    new Task(courseId, "Question 1", 1, Type.OPEN_TEXT),
                    new Task(courseId, "Question 3", 3, Type.SINGLE_CHOICE),
                    new Task(courseId, "Question 4", 4, Type.MULTIPLE_CHOICE)
            );

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.findByCourseId(courseId)).thenReturn(tasksWithGap);

            // When & Then
            InvalidCourseStateException exception = assertThrows(
                    InvalidCourseStateException.class,
                    () -> courseService.publishCourse(courseId)
            );
            assertEquals("Tasks must have continuous sequential order starting from 1", exception.getMessage());
        }

        @Test
        void shouldPublishCourseWithNoTasks() {
            // Given
            Long courseId = 1L;

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(validCourse));
            when(taskRepository.findByCourseId(courseId)).thenReturn(List.of());

            // When & Then
            InvalidCourseStateException exception = assertThrows(
                    InvalidCourseStateException.class,
                    () -> courseService.publishCourse(courseId)
            );
            assertTrue(exception.getMessage().contains("Missing:"));
        }
    }

    @Nested
    class InstructorCoursesReportTests {

        @Test
        void shouldThrowExceptionWhenInstructorNotFound() {
            // Given
            Long invalidInstructorId = 999L;
            when(userRepository.findById(invalidInstructorId)).thenReturn(Optional.empty());

            // When & Then
            InstructorNotFoundException exception = assertThrows(
                    InstructorNotFoundException.class,
                    () -> courseService.getInstructorCoursesReport(invalidInstructorId)
            );
            assertEquals("Instructor not found with id: 999", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenUserIsNotInstructor() {
            // Given
            Long studentId = 1L;

            when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

            // When & Then
            UserNotInstructorException exception = assertThrows(
                    UserNotInstructorException.class,
                    () -> courseService.getInstructorCoursesReport(studentId)
            );
            assertEquals("User is not an instructor", exception.getMessage());
        }

        @Test
        void shouldReturnEmptyReportWhenInstructorHasNoCourses() {
            // Given
            Long instructorId = 1L;

            when(userRepository.findById(instructorId)).thenReturn(Optional.of(instructor));
            when(courseRepository.findByInstructor(instructor)).thenReturn(List.of());

            // When
            InstructorCoursesReportResponse result = courseService.getInstructorCoursesReport(instructorId);

            // Then
            assertNotNull(result);
            assertTrue(result.getCourses().isEmpty());
            assertEquals(0, result.getTotalPublishedCourses());
        }

        @Test
        void shouldCountOnlyPublishedCoursesInTotal() {
            // Given
            Long instructorId = 1L;

            Course published1 = new Course("Course 1", "Desc", instructor);
            published1.setAsPublished();

            Course published2 = new Course("Course 2", "Desc", instructor);
            published2.setAsPublished();

            Course building = new Course("Course 3", "Desc", instructor);
            building.setStatus(Status.BUILDING);

            List<Course> courses = Arrays.asList(published1, published2, building);

            when(userRepository.findById(instructorId)).thenReturn(Optional.of(instructor));
            when(courseRepository.findByInstructor(instructor)).thenReturn(courses);
            when(taskRepository.countByCourseId(any())).thenReturn(0);

            // When
            InstructorCoursesReportResponse result = courseService.getInstructorCoursesReport(instructorId);

            // Then
            assertEquals(3, result.getCourses().size());
            assertEquals(2, result.getTotalPublishedCourses()); // Only published courses
        }
    }
}