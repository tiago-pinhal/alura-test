package br.com.alura.AluraFake.task.dto.response;

import java.util.List;

public class InstructorCoursesReportResponse {

    private List<CourseReportResponse> courses;
    private Integer totalPublishedCourses;

    // Constructors
    public InstructorCoursesReportResponse() {}

    public InstructorCoursesReportResponse(List<CourseReportResponse> courses, Integer totalPublishedCourses) {
        this.courses = courses;
        this.totalPublishedCourses = totalPublishedCourses;
    }

    // Getters and Setters
    public List<CourseReportResponse> getCourses() { return courses; }
    public void setCourses(List<CourseReportResponse> courses) { this.courses = courses; }

    public Integer getTotalPublishedCourses() { return totalPublishedCourses; }
    public void setTotalPublishedCourses(Integer totalPublishedCourses) { this.totalPublishedCourses = totalPublishedCourses; }
}
