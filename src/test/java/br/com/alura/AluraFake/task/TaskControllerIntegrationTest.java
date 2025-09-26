package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.task.dto.request.OpenTextTaskRequest;
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
class TaskControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        // MockMvc Configuration
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Current data stored in the database
        User instructor = new User("Tiago Pinhal", "tiago@email.com", Role.INSTRUCTOR);
        userRepository.save(instructor);
        testCourse = new Course("Java Basics", "Learn Java fundamentals", instructor);
        courseRepository.save(testCourse);
    }

    @Test
    @Transactional
    void shouldCreateTask() throws Exception {
        // Given
        OpenTextTaskRequest request = new OpenTextTaskRequest(
                testCourse.getId(),
                "What is polymorphism?",
                1
        );

        // When & Then
        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statement").value("What is polymorphism?"))
                .andExpect(jsonPath("$.type").value("OPEN_TEXT"));

        // Assert
        var tasksInDb = taskRepository.findByCourseId(testCourse.getId());
        assertThat(tasksInDb).hasSize(1);
        assertThat(tasksInDb.getFirst().getStatement()).isEqualTo("What is polymorphism?");
    }

    @Test
    void shouldReturnBadRequestWhenExistsTaskWithSameStatement() throws Exception {
        // Given
        Task existingTask = new Task(testCourse.getId(), "Duplicate statement", 1, Type.OPEN_TEXT);
        taskRepository.save(existingTask);

        OpenTextTaskRequest duplicateRequest = new OpenTextTaskRequest(
                testCourse.getId(),
                "Duplicate statement",
                2
        );

        // When & Then
        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest());
    }
}