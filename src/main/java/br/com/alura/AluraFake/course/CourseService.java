package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.task.dto.response.InstructorCoursesReportResponse;
import br.com.alura.AluraFake.task.dto.response.PublishCourseResponse;
import br.com.alura.AluraFake.util.ErrorItemDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping("/course/new")
    public ResponseEntity<?> createCourse(@Valid @RequestBody NewCourseDTO newCourse) {
        try {
            courseService.createCourse(newCourse);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorItemDTO("emailInstructor", "Usuario nao e um instrutor"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorItemDTO("error", "Internal server error occurred"));
        }
    }

    @GetMapping("/course/all")
    public ResponseEntity<List<CourseListItemDTO>> getAllCourses() {
        List<CourseListItemDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/course/{id}/publish")
    public ResponseEntity<?> publishCourse(@PathVariable("id") Long id) {
        try {
            PublishCourseResponse response = courseService.publishCourse(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorItemDTO("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorItemDTO("error", "Internal server error occurred"));
        }
    }

    @GetMapping("/instructor/{id}/courses")
    public ResponseEntity<?> getInstructorCoursesReport(@PathVariable("id") Long instructorId) {
        try {
            InstructorCoursesReportResponse response = courseService.getInstructorCoursesReport(instructorId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorItemDTO("error", "Internal server error occurred"));
        }
    }
}