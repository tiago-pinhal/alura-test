package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.task.dto.request.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.request.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.request.SingleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.response.TaskResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/task/new/opentext")
    public ResponseEntity<TaskResponse> newOpenTextExercise(@Valid @RequestBody OpenTextTaskRequest request) {
        TaskResponse response = taskService.createOpenTextTask(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/task/new/singlechoice")
    public ResponseEntity<TaskResponse> newSingleChoice(@Valid @RequestBody SingleChoiceTaskRequest request) {
        TaskResponse response = taskService.createSingleChoiceTask(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/task/new/multiplechoice")
    public ResponseEntity<TaskResponse> newMultipleChoice(@Valid @RequestBody MultipleChoiceTaskRequest request) {
        TaskResponse response = taskService.createMultipleChoiceTask(request);
        return ResponseEntity.ok(response);
    }
}