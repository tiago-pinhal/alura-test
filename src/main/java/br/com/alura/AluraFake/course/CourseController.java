package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.task.dto.response.InstructorCoursesReportResponse;
import br.com.alura.AluraFake.task.dto.response.PublishCourseResponse;
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
    public ResponseEntity<Void> createCourse(@Valid @RequestBody NewCourseDTO newCourse) {
        courseService.createCourse(newCourse);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/course/all")
    public ResponseEntity<List<CourseListItemDTO>> getAllCourses() {
        List<CourseListItemDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/course/{id}/publish")
    public ResponseEntity<PublishCourseResponse> publishCourse(@PathVariable("id") Long id) {
        PublishCourseResponse response = courseService.publishCourse(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/instructor/{id}/courses")
    public ResponseEntity<InstructorCoursesReportResponse> getInstructorCoursesReport(@PathVariable("id") Long instructorId) {
        InstructorCoursesReportResponse response = courseService.getInstructorCoursesReport(instructorId);
        return ResponseEntity.ok(response);
    }
}