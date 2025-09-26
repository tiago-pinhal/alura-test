package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.task.dto.request.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.request.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.request.SingleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.request.TaskOptionRequest;
import br.com.alura.AluraFake.task.dto.response.TaskResponse;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllTasks() throws Exception {
        // Given
        List<TaskResponse> mockTasks = Arrays.asList(
                new TaskResponse(1L, 1L, "Question 1", 1, Type.OPEN_TEXT, LocalDateTime.now(), null),
                new TaskResponse(2L, 1L, "Question 2", 2, Type.SINGLE_CHOICE, LocalDateTime.now(), Collections.emptyList())
        );
        when(taskService.findAllTasks()).thenReturn(mockTasks);

        // When & Then
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].statement").value("Question 1"))
                .andExpect(jsonPath("$[0].type").value("OPEN_TEXT"));
    }

    @Test
    void shouldReturnTaskById() throws Exception {
        // Given
        TaskResponse mockTask = new TaskResponse(1L, 1L, "Question 1", 1, Type.OPEN_TEXT, LocalDateTime.now(), null);
        when(taskService.findTaskById(1L)).thenReturn(mockTask);

        // When & Then
        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statement").value("Question 1"));
    }

    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {
        // Given
        when(taskService.findTaskById(999L)).thenThrow(new IllegalArgumentException("Task not found"));

        // When & Then
        mockMvc.perform(get("/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateOpenTextTask() throws Exception {
        // Given
        OpenTextTaskRequest request = new OpenTextTaskRequest(1L, "What did we learn today?", 1);
        TaskResponse response = new TaskResponse(1L, 1L, "What did we learn today?", 1, Type.OPEN_TEXT, LocalDateTime.now(), null);

        when(taskService.createOpenTextTask(any(OpenTextTaskRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("OPEN_TEXT"))
                .andExpect(jsonPath("$.statement").value("What did we learn today?"));
    }

    @Test
    void shouldValidateOpenTextTaskRequest() throws Exception {
        // Given - Invalid request with short statement
        OpenTextTaskRequest invalidRequest = new OpenTextTaskRequest(1L, "Hi", 1);

        // When & Then
        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateSingleChoiceTask() throws Exception {
        // Given
        List<TaskOptionRequest> options = Arrays.asList(
                new TaskOptionRequest("Java", true),
                new TaskOptionRequest("Python", false),
                new TaskOptionRequest("Ruby", false)
        );
        SingleChoiceTaskRequest request = new SingleChoiceTaskRequest(1L, "What language are we learning?", 1, options);
        TaskResponse response = new TaskResponse(1L, 1L, "What language are we learning?", 1, Type.SINGLE_CHOICE, LocalDateTime.now(), Collections.emptyList());

        when(taskService.createSingleChoiceTask(any(SingleChoiceTaskRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/task/new/singlechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SINGLE_CHOICE"));
    }

    @Test
    void shouldValidateSingleChoiceMinimumOptions() throws Exception {
        // Given - Only one option (invalid)
        List<TaskOptionRequest> options = List.of(
                new TaskOptionRequest("Java", true)
        );
        SingleChoiceTaskRequest request = new SingleChoiceTaskRequest(1L, "What language?", 1, options);

        // When & Then
        mockMvc.perform(post("/task/new/singlechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateMultipleChoiceTask() throws Exception {
        // Given
        List<TaskOptionRequest> options = Arrays.asList(
                new TaskOptionRequest("Java", true),
                new TaskOptionRequest("Spring", true),
                new TaskOptionRequest("Ruby", false)
        );
        MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest(1L, "What technologies are we learning?", 1, options);
        TaskResponse response = new TaskResponse(1L, 1L, "What technologies are we learning?", 1, Type.MULTIPLE_CHOICE, LocalDateTime.now(), Collections.emptyList());

        when(taskService.createMultipleChoiceTask(any(MultipleChoiceTaskRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/task/new/multiplechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("MULTIPLE_CHOICE"));
    }

    @Test
    void shouldValidateMultipleChoiceMinimumOptions() throws Exception {
        // Given - Only two options (invalid for multiple choice)
        List<TaskOptionRequest> options = Arrays.asList(
                new TaskOptionRequest("Java", true),
                new TaskOptionRequest("Python", false)
        );
        MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest(1L, "What technologies?", 1, options);

        // When & Then
        mockMvc.perform(post("/task/new/multiplechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateNullCourseId() throws Exception {
        // Given
        OpenTextTaskRequest request = new OpenTextTaskRequest(null, "Valid statement", 1);

        // When & Then
        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateNegativeOrder() throws Exception {
        // Given
        OpenTextTaskRequest request = new OpenTextTaskRequest(1L, "Valid statement", -1);

        // When & Then
        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}