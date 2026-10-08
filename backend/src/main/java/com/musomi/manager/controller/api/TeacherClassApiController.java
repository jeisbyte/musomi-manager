package com.musomi.manager.controller.api;

import java.util.List;
import java.util.Objects;

import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.AssessmentResponse;
import com.musomi.manager.dto.response.AssignmentResponse;
import com.musomi.manager.dto.response.StudentResponse;
import com.musomi.manager.mapper.StudentMapper;
import com.musomi.manager.repository.StudentRepository;
import com.musomi.manager.service.AssessmentService;
import com.musomi.manager.service.TeacherAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Teacher endpoints for assigned classes and student rosters. */
@RestController
@RequestMapping("/api/v1/teacher/classes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherClassApiController {

    private static final Long SCHOOL_ID = 1L;
    private static final Long TEACHER_ID = 1L;

    private final TeacherAssignmentService teacherAssignmentService;
    private final StudentRepository studentRepository;
    private final AssessmentService assessmentService;

    /** Lists the teacher's assigned class, stream, and subject combinations. */
    @GetMapping
    public ApiResponse<List<ClassStreamResponse>> listClasses() {
        List<ClassStreamResponse> classes = teacherAssignmentService.listAssignments(SCHOOL_ID, TEACHER_ID)
                .stream()
                .map(this::toClassStreamResponse)
                .toList();
        return ApiResponse.success(classes);
    }

    /** Lists active students in a class stream. */
    @GetMapping("/{classId}/streams/{streamId}/students")
    public ApiResponse<List<StudentResponse>> listStudents(
            @PathVariable Long classId,
            @PathVariable Long streamId) {
        List<StudentResponse> students = StudentMapper.toResponseList(
                studentRepository.findByCurrentClassIdAndIsActiveTrue(classId).stream()
                        .filter(student -> student.getCurrentStream() != null
                                && streamId.equals(student.getCurrentStream().getId()))
                        .toList());
        return ApiResponse.success(students);
    }

    private ClassStreamResponse toClassStreamResponse(AssignmentResponse assignment) {
        List<AssessmentResponse> assessments = assessmentService.listAssessments(
                        TEACHER_ID, assignment.classId(), assignment.subjectId(), null, null)
                .stream()
                .filter(assessment -> Objects.equals(assessment.streamId(), assignment.streamId()))
                .toList();
        long studentCount = studentRepository.findByCurrentClassIdAndIsActiveTrue(assignment.classId())
                .stream()
                .filter(student -> assignment.streamId() == null
                        || student.getCurrentStream() != null
                        && assignment.streamId().equals(student.getCurrentStream().getId()))
                .count();
        long draftCount = assessments.stream()
                .filter(assessment -> "DRAFT".equalsIgnoreCase(assessment.status()))
                .count();

        return new ClassStreamResponse(
                assignment.classId(),
                assignment.className(),
                assignment.streamId(),
                assignment.streamName(),
                assignment.subjectId(),
                assignment.subjectName(),
                studentCount,
                (long) assessments.size(),
                draftCount
        );
    }

    /** Class and stream assignment details with roster and assessment counts. */
    public record ClassStreamResponse(
            Long classId,
            String className,
            Long streamId,
            String streamName,
            Long subjectId,
            String subjectName,
            Long studentCount,
            Long assessmentCount,
            Long draftCount
    ) {
    }
}
