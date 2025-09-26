package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.task.Task;
import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.task.Type;
import br.com.alura.AluraFake.user.Role;
import br.com.alura.AluraFake.user.User;
import br.com.alura.AluraFake.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureWebMvc
@Transactional
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CourseControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    private User instructor;

    @BeforeEach
    void setUp() {
        // Configurar MockMvc manualmente
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        instructor = new User("John Instructor", "john@instructor.com", Role.INSTRUCTOR);
        userRepository.save(instructor);

        User student = new User("Jane Student", "jane@student.com", Role.STUDENT);
        userRepository.save(student);
    }

    @Test
    void shouldCreateCourseWithRealDatabaseInteraction() throws Exception {
        // Given
        NewCourseDTO request = new NewCourseDTO();
        request.setTitle("Java Fundamentals");
        request.setDescription("Complete Java course for beginners");
        request.setEmailInstructor("john@instructor.com");

        // When & Then
        mockMvc.perform(post("/course/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        var coursesInDb = courseRepository.findByInstructor(instructor);
        assertThat(coursesInDb).hasSize(1);
        assertThat(coursesInDb.getFirst().getTitle()).isEqualTo("Java Fundamentals");
        assertThat(coursesInDb.getFirst().getStatus()).isEqualTo(Status.BUILDING);
    }

    @Test
    void shouldRejectCourseCreationWhenUserIsNotInstructor() throws Exception {
        // Given
        NewCourseDTO request = new NewCourseDTO();
        request.setTitle("Java Course");
        request.setDescription("Course description");
        request.setEmailInstructor("jane@student.com");

        // When & Then
        mockMvc.perform(post("/course/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is not an instructor"));

        var coursesInDb = courseRepository.findAll();
        assertThat(coursesInDb).isNotEmpty();
    }

    @Test
    void shouldListAllCoursesFromDatabase() throws Exception {
        // Given
        Course course1 = new Course("Spring Boot", "Learn Spring Boot", instructor);
        Course course2 = new Course("JPA Basics", "Learn JPA fundamentals", instructor);
        courseRepository.save(course1);
        courseRepository.save(course2);

        // When & Then
        mockMvc.perform(get("/course/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("Java"))
                .andExpect(jsonPath("$[1].title").value("Spring Boot"));
    }

    @Test
    void shouldPublishCourseWithRealValidations() throws Exception {
        // Given
        Course course = new Course("Complete Java Course", "Full course", instructor);
        courseRepository.save(course);

        Task openTextTask = new Task(course.getId(), "What is Java?", 1, Type.OPEN_TEXT);
        Task singleChoiceTask = new Task(course.getId(), "Choose the correct answer", 2, Type.SINGLE_CHOICE);
        Task multipleChoiceTask = new Task(course.getId(), "Select all that apply", 3, Type.MULTIPLE_CHOICE);

        taskRepository.save(openTextTask);
        taskRepository.save(singleChoiceTask);
        taskRepository.save(multipleChoiceTask);

        // When & Then
        mockMvc.perform(post("/course/{id}/publish", course.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(course.getId()))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedAt").exists());

        Course publishedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(publishedCourse.getStatus()).isEqualTo(Status.PUBLISHED);
        assertThat(publishedCourse.getPublishedAt()).isNotNull();
    }

    @Test
    void shouldRejectPublishingCourseWithMissingTaskTypes() throws Exception {
        // Given
        Course course = new Course("Incomplete Course", "Missing task types", instructor);
        courseRepository.save(course);

        Task onlyOpenTextTask = new Task(course.getId(), "What is Java?", 1, Type.OPEN_TEXT);
        taskRepository.save(onlyOpenTextTask);

        // When & Then
        mockMvc.perform(post("/course/{id}/publish", course.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Course must have at least one task of each type. Missing: [MULTIPLE_CHOICE, SINGLE_CHOICE]"));

        Course unchangedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(unchangedCourse.getStatus()).isEqualTo(Status.BUILDING);
    }

    @Test
    void shouldRejectPublishingCourseWithInvalidTaskSequence() throws Exception {
        // Given - Curso com sequência inválida de tasks
        Course course = new Course("Invalid Sequence Course", "Bad task order", instructor);
        courseRepository.save(course);

        // Criar tasks com sequência inválida (1, 3, 4 - faltando 2)
        Task task1 = new Task(course.getId(), "Question 1", 1, Type.OPEN_TEXT);
        Task task3 = new Task(course.getId(), "Question 3", 3, Type.SINGLE_CHOICE);
        Task task4 = new Task(course.getId(), "Question 4", 4, Type.MULTIPLE_CHOICE);

        taskRepository.save(task1);
        taskRepository.save(task3);
        taskRepository.save(task4);

        // When & Then
        mockMvc.perform(post("/course/{id}/publish", course.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tasks must have continuous sequential order starting from 1"));
    }

    @Test
    void shouldGenerateInstructorReport() throws Exception {
        // Given - create the course
        Long instructorId = 2L;
        Course publishedCourse = new Course("Published Course", "Description", instructor);
        courseRepository.save(publishedCourse);
        publishedCourse.setAsPublished();
        courseRepository.save(publishedCourse);

        Course buildingCourse = new Course("Building Course", "Description", instructor);
        courseRepository.save(buildingCourse);

        // Add some tasks
        Task task1 = new Task(publishedCourse.getId(), "Task 1", 1, Type.OPEN_TEXT);
        Task task2 = new Task(buildingCourse.getId(), "Task 2", 1, Type.SINGLE_CHOICE);
        taskRepository.save(task1);
        taskRepository.save(task2);

        // When & Then
        mockMvc.perform(get("/instructor/{id}/courses", instructorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses.length()").value(1))
                .andExpect(jsonPath("$.totalPublishedCourses").value(0))
                .andExpect(jsonPath("$.courses[0].title").value("Java"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentInstructor() throws Exception {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        mockMvc.perform(get("/instructor/{id}/courses", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Instructor not found with id: 999"));
    }

    @Test
    void shouldReturnBadRequestWhenUserIsNotInstructorForReport() throws Exception {
        // Given
        Long userId = 1L;

        // When & Then
        mockMvc.perform(get("/instructor/{id}/courses", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is not an instructor"));
    }

    @Test
    void shouldValidateBeanValidationOnCourseCreation() throws Exception {
        // Given - invalid DTO
        NewCourseDTO invalidRequest = new NewCourseDTO();
        invalidRequest.setTitle(""); // blank title
        invalidRequest.setDescription("Valid description");
        invalidRequest.setEmailInstructor("john@instructor.com");

        // When & Then
        mockMvc.perform(post("/course/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}