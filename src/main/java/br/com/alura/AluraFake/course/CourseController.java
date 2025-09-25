package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.task.Task;
import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.task.Type;
import br.com.alura.AluraFake.task.dto.response.CourseReportResponse;
import br.com.alura.AluraFake.task.dto.response.InstructorCoursesReportResponse;
import br.com.alura.AluraFake.task.dto.response.PublishCourseResponse;
import br.com.alura.AluraFake.user.User;
import br.com.alura.AluraFake.user.UserRepository;
import br.com.alura.AluraFake.util.ErrorItemDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class CourseController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public CourseController(
            CourseRepository courseRepository,
            UserRepository userRepository,
            TaskRepository taskRepository

    ) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    @PostMapping("/course/new")
    public ResponseEntity createCourse(@Valid @RequestBody NewCourseDTO newCourse) {

        //Caso implemente o bonus, pegue o instrutor logado
        Optional<User> possibleAuthor = userRepository
                .findByEmail(newCourse.getEmailInstructor())
                .filter(User::isInstructor);

        if (possibleAuthor.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorItemDTO("emailInstructor", "Usuário não é um instrutor"));
        }

        Course course = new Course(newCourse.getTitle(), newCourse.getDescription(), possibleAuthor.get());

        courseRepository.save(course);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/course/all")
    public ResponseEntity<List<CourseListItemDTO>> createCourse() {
        List<CourseListItemDTO> courses = courseRepository.findAll().stream()
                .map(CourseListItemDTO::new)
                .toList();
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/course/{id}/publish")
    public ResponseEntity publishCourse(@PathVariable("id") Long id) {
        try {
            // Search course
            Optional<Course> courseOpt = courseRepository.findById(id);
            if (courseOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorItemDTO("courseId", "Course not found"));
            }

            Course course = courseOpt.get();

            // Validate if the course is in BUILDING status
            if (!Status.BUILDING.equals(course.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorItemDTO("status", "Course must be in BUILDING status to be published"));
            }

            // Buscar todas as tarefas do curso
            List<Task> tasks = taskRepository.findByCourseId(id);

            // Validar se tem pelo menos uma atividade de cada tipo
            boolean hasOpenText = tasks.stream().anyMatch(task -> Type.OPEN_TEXT.equals(task.getType()));
            boolean hasSingleChoice = tasks.stream().anyMatch(task -> Type.SINGLE_CHOICE.equals(task.getType()));
            boolean hasMultipleChoice = tasks.stream().anyMatch(task -> Type.MULTIPLE_CHOICE.equals(task.getType()));

            if (!hasOpenText || !hasSingleChoice || !hasMultipleChoice) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorItemDTO("tasks", "Course must have at least one task of each type (OPEN_TEXT, SINGLE_CHOICE, MULTIPLE_CHOICE)"));
            }

            // Validar sequência contínua das ordens
            if (!tasks.isEmpty()) {
                List<Integer> orders = tasks.stream()
                        .map(Task::getOrder)
                        .sorted()
                        .toList();

                for (int i = 0; i < orders.size(); i++) {
                    if (!orders.get(i).equals(i + 1)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ErrorItemDTO("taskOrder", "Tasks must have continuous sequential order starting from 1"));
                    }
                }
            }

            course.setAsPublished();

            Course publishedCourse = courseRepository.save(course);

            PublishCourseResponse response = new PublishCourseResponse(
                    publishedCourse.getId(),
                    publishedCourse.getTitle(),
                    publishedCourse.getStatus(),
                    publishedCourse.getPublishedAt()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorItemDTO("error", "Internal server error occurred"));
        }
    }

    @GetMapping("/instructor/{id}/courses")
    public ResponseEntity<?> getInstructorCoursesReport(@PathVariable("id") Long instructorId) {
        Optional<User> userOpt = userRepository.findById(instructorId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOpt.get();

        if (!user.isInstructor()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        List<Course> instructorCourses = courseRepository.findByInstructor(user);

        List<CourseReportResponse> courseReports = instructorCourses.stream()
                .map(course -> {
                    int taskCount = taskRepository.countByCourseId(course.getId());
                    return new CourseReportResponse(
                            course.getId(),
                            course.getTitle(),
                            course.getStatus(),
                            course.getPublishedAt(),
                            taskCount
                    );
                })
                .collect(Collectors.toList());

        int totalPublishedCourses = (int) instructorCourses.stream()
                .filter(course -> Status.PUBLISHED.equals(course.getStatus()))
                .count();

        InstructorCoursesReportResponse response = new InstructorCoursesReportResponse(
                courseReports,
                totalPublishedCourses
        );

        return ResponseEntity.ok(response);
    }

}
