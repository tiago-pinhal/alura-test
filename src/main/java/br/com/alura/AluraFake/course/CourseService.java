package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.exception.CourseNotFoundException;
import br.com.alura.AluraFake.exception.InstructorNotFoundException;
import br.com.alura.AluraFake.exception.InvalidCourseStateException;
import br.com.alura.AluraFake.exception.UserNotInstructorException;
import br.com.alura.AluraFake.task.Task;
import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.task.Type;
import br.com.alura.AluraFake.task.dto.response.CourseReportResponse;
import br.com.alura.AluraFake.task.dto.response.InstructorCoursesReportResponse;
import br.com.alura.AluraFake.task.dto.response.PublishCourseResponse;
import br.com.alura.AluraFake.user.User;
import br.com.alura.AluraFake.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public CourseService(
            CourseRepository courseRepository,
            UserRepository userRepository,
            TaskRepository taskRepository
    ) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public Course createCourse(NewCourseDTO newCourse) {
        User instructor = validateAndGetInstructor(newCourse.getEmailInstructor());
        Course course = new Course(newCourse.getTitle(), newCourse.getDescription(), instructor);
        courseRepository.save(course);
        return course;
    }

    public List<CourseListItemDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(CourseListItemDTO::new)
                .toList();
    }

    @Transactional
    public PublishCourseResponse publishCourse(Long courseId) {
        Course course = findCourseById(courseId);
        validateCourseCanBePublished(course);
        validateCourseHasRequiredTasks(courseId);
        validateTaskSequence(courseId);

        course.setAsPublished();
        Course publishedCourse = courseRepository.save(course);

        return new PublishCourseResponse(
                publishedCourse.getId(),
                publishedCourse.getTitle(),
                publishedCourse.getStatus(),
                publishedCourse.getPublishedAt()
        );
    }

    public InstructorCoursesReportResponse getInstructorCoursesReport(Long instructorId) {
        User instructor = validateAndGetInstructorById(instructorId);
        List<Course> instructorCourses = courseRepository.findByInstructor(instructor);

        List<CourseReportResponse> courseReports = instructorCourses.stream()
                .map(this::buildCourseReport)
                .collect(Collectors.toList());

        int totalPublishedCourses = (int) instructorCourses.stream()
                .filter(course -> Status.PUBLISHED.equals(course.getStatus()))
                .count();

        return new InstructorCoursesReportResponse(courseReports, totalPublishedCourses);
    }

    // Private helper methods

    private User validateAndGetInstructor(String email) {
        return userRepository.findByEmail(email)
                .filter(User::isInstructor)
                .orElseThrow(UserNotInstructorException::new);
    }

    private User validateAndGetInstructorById(Long instructorId) {
        User user = userRepository.findById(instructorId)
                .orElseThrow(() -> new InstructorNotFoundException(instructorId));

        if (!user.isInstructor()) {
            throw new UserNotInstructorException();
        }

        return user;
    }

    private Course findCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    private void validateCourseCanBePublished(Course course) {
        if (!Status.BUILDING.equals(course.getStatus())) {
            throw new InvalidCourseStateException("Course must be in BUILDING status to be published");
        }
    }

    private void validateCourseHasRequiredTasks(Long courseId) {
        List<Task> tasks = taskRepository.findByCourseId(courseId);

        Set<Type> existingTypes = tasks.stream()
                .map(Task::getType)
                .collect(Collectors.toSet());

        Set<Type> requiredTypes = EnumSet.allOf(Type.class);

        if (!existingTypes.containsAll(requiredTypes)) {
            Set<Type> missingTypes = EnumSet.copyOf(requiredTypes);
            missingTypes.removeAll(existingTypes);

            throw new InvalidCourseStateException(
                    "Course must have at least one task of each type. Missing: " + missingTypes
            );
        }
    }

    private void validateTaskSequence(Long courseId) {
        List<Task> tasks = taskRepository.findByCourseId(courseId);

        if (!tasks.isEmpty()) {
            List<Integer> orders = tasks.stream()
                    .map(Task::getOrder)
                    .sorted()
                    .toList();

            for (int i = 0; i < orders.size(); i++) {
                if (!orders.get(i).equals(i + 1)) {
                    throw new InvalidCourseStateException("Tasks must have continuous sequential order starting from 1");
                }
            }
        }
    }

    private CourseReportResponse buildCourseReport(Course course) {
        int taskCount = taskRepository.countByCourseId(course.getId());
        return new CourseReportResponse(
                course.getId(),
                course.getTitle(),
                course.getStatus(),
                course.getPublishedAt(),
                taskCount
        );
    }
}