package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.exception.CourseNotFoundException;
import br.com.alura.AluraFake.exception.InstructorNotFoundException;
import br.com.alura.AluraFake.exception.InvalidCourseStateException;
import br.com.alura.AluraFake.exception.UserNotInstructorException;
import br.com.alura.AluraFake.task.dto.response.InstructorCoursesReportResponse;
import br.com.alura.AluraFake.task.dto.response.PublishCourseResponse;
import br.com.alura.AluraFake.user.Role;
import br.com.alura.AluraFake.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        // Given
        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java");
        newCourseDTO.setDescription("Curso de Java");
        newCourseDTO.setEmailInstructor("invalid@email.com");

        when(courseService.createCourse(any(NewCourseDTO.class)))
                .thenThrow(new UserNotInstructorException());

        // When & Then
        mockMvc.perform(post("/course/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is not an instructor"));
    }

    @Test
    void shouldReturnBadRequestWhenUserIsNotInstructor() throws Exception {
        // Given
        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java");
        newCourseDTO.setDescription("Curso de Java");
        newCourseDTO.setEmailInstructor("student@email.com");

        when(courseService.createCourse(any(NewCourseDTO.class)))
                .thenThrow(new UserNotInstructorException());

        // When & Then
        mockMvc.perform(post("/course/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is not an instructor"));
    }

    @Test
    void shouldReturnCreatedWhenNewCourseRequestIsValid() throws Exception {
        // Given
        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java");
        newCourseDTO.setDescription("Curso de Java");
        newCourseDTO.setEmailInstructor("instructor@email.com");

        User instructor = new User("Paulo", "instructor@email.com", Role.INSTRUCTOR);
        Course course = new Course("Java", "Curso de Java", instructor);

        when(courseService.createCourse(any(NewCourseDTO.class))).thenReturn(course);

        // When & Then
        mockMvc.perform(post("/course/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isCreated());

        verify(courseService, times(1)).createCourse(any(NewCourseDTO.class));
    }

    @Test
    void shouldValidateNewCourseDTO() throws Exception {
        // Given - Invalid DTO with missing title
        NewCourseDTO invalidDTO = new NewCourseDTO();
        invalidDTO.setDescription("Curso de Java");
        invalidDTO.setEmailInstructor("instructor@email.com");
        // title is null

        // When & Then
        mockMvc.perform(post("/course/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListAllCourses() throws Exception {
        // Given
        List<CourseListItemDTO> courses = Arrays.asList(
                new CourseListItemDTO(new Course("Java", "Curso de java",
                        new User("Paulo", "paulo@email.com", Role.INSTRUCTOR))),
                new CourseListItemDTO(new Course("Hibernate", "Curso de hibernate",
                        new User("Paulo", "paulo@email.com", Role.INSTRUCTOR))),
                new CourseListItemDTO(new Course("Spring", "Curso de spring",
                        new User("Paulo", "paulo@email.com", Role.INSTRUCTOR)))
        );

        when(courseService.getAllCourses()).thenReturn(courses);

        // When & Then
        mockMvc.perform(get("/course/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java"))
                .andExpect(jsonPath("$[0].description").value("Curso de java"))
                .andExpect(jsonPath("$[1].title").value("Hibernate"))
                .andExpect(jsonPath("$[1].description").value("Curso de hibernate"))
                .andExpect(jsonPath("$[2].title").value("Spring"))
                .andExpect(jsonPath("$[2].description").value("Curso de spring"));
    }

    @Test
    void shouldReturnEmptyListWhenNoCoursesExist() throws Exception {
        // Given
        when(courseService.getAllCourses()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/course/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldPublishCourseSuccessfully() throws Exception {
        // Given
        Long courseId = 1L;
        PublishCourseResponse response = new PublishCourseResponse(
                courseId, "Java Course", Status.PUBLISHED, LocalDateTime.now()
        );

        when(courseService.publishCourse(courseId)).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/course/{id}/publish", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(courseId))
                .andExpect(jsonPath("$.title").value("Java Course"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedAt").exists());
    }

    @Test
    void shouldReturnNotFoundWhenPublishingNonExistentCourse() throws Exception {
        // Given
        Long invalidCourseId = 999L;
        when(courseService.publishCourse(invalidCourseId))
                .thenThrow(new CourseNotFoundException(invalidCourseId));

        // When & Then
        mockMvc.perform(post("/course/{id}/publish", invalidCourseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course not found with id: 999"));
    }

    @Test
    void shouldReturnBadRequestWhenCourseCannotBePublished() throws Exception {
        // Given
        Long courseId = 1L;
        when(courseService.publishCourse(courseId))
                .thenThrow(new InvalidCourseStateException("Course must be in BUILDING status to be published"));

        // When & Then
        mockMvc.perform(post("/course/{id}/publish", courseId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Course must be in BUILDING status to be published"));
    }

    @Test
    void shouldReturnInstructorCoursesReport() throws Exception {
        // Given
        Long instructorId = 1L;
        InstructorCoursesReportResponse response = new InstructorCoursesReportResponse(
                Collections.emptyList(), 0
        );

        when(courseService.getInstructorCoursesReport(instructorId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/instructor/{id}/courses", instructorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses").isArray())
                .andExpect(jsonPath("$.totalPublishedCourses").value(0));
    }

    @Test
    void shouldReturnNotFoundWhenInstructorDoesNotExist() throws Exception {
        // Given
        Long invalidInstructorId = 999L;
        when(courseService.getInstructorCoursesReport(invalidInstructorId))
                .thenThrow(new InstructorNotFoundException(invalidInstructorId));

        // When & Then
        mockMvc.perform(get("/instructor/{id}/courses", invalidInstructorId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Instructor not found with id: 999"));
    }

    @Test
    void shouldReturnBadRequestWhenUserIsNotInstructorInReport() throws Exception {
        // Given
        Long studentId = 1L;
        when(courseService.getInstructorCoursesReport(studentId))
                .thenThrow(new UserNotInstructorException());

        // When & Then
        mockMvc.perform(get("/instructor/{id}/courses", studentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User is not an instructor"));
    }
}