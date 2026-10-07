package com.musomi.manager.service;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.dto.request.CreateAssignmentRequest;
import com.musomi.manager.dto.response.AssignmentResponse;
import com.musomi.manager.entity.AcademicYear;
import com.musomi.manager.entity.ClassEntity;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Subject;
import com.musomi.manager.entity.TeacherAssignment;
import com.musomi.manager.entity.User;
import com.musomi.manager.entity.enums.Role;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.repository.AcademicYearRepository;
import com.musomi.manager.repository.ClassRepository;
import com.musomi.manager.repository.StreamRepository;
import com.musomi.manager.repository.SubjectRepository;
import com.musomi.manager.repository.TeacherAssignmentRepository;
import com.musomi.manager.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherAssignmentServiceTest {

    private static final Long SCHOOL_ID = 1L;

    @Mock
    private TeacherAssignmentRepository teacherAssignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private StreamRepository streamRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @InjectMocks
    private TeacherAssignmentService teacherAssignmentService;

    @Test
    @DisplayName("should list assignments by teacher")
    void shouldListAssignmentsByTeacher() {
        TeacherAssignment assignment = assignment();
        when(teacherAssignmentRepository.findBySchoolIdAndTeacherIdOrderByIdAsc(SCHOOL_ID, 2L))
                .thenReturn(List.of(assignment));

        List<AssignmentResponse> result = teacherAssignmentService.listAssignments(SCHOOL_ID, 2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).teacherId()).isEqualTo(2L);
        verify(teacherAssignmentRepository).findBySchoolIdAndTeacherIdOrderByIdAsc(SCHOOL_ID, 2L);
    }

    @Test
    @DisplayName("should create assignment when parents exist")
    void shouldCreateAssignmentWhenParentsExist() {
        CreateAssignmentRequest request = new CreateAssignmentRequest(2L, 3L, 4L, null, 5L);
        User teacher = teacher();
        Subject subject = Subject.builder().id(3L).school(school()).name("Mathematics").build();
        ClassEntity classEntity = ClassEntity.builder().id(4L).school(school())
                .academicYear(academicYear()).name("S3").build();
        AcademicYear year = academicYear();
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));
        when(subjectRepository.findById(3L)).thenReturn(Optional.of(subject));
        when(classRepository.findById(4L)).thenReturn(Optional.of(classEntity));
        when(academicYearRepository.findById(5L)).thenReturn(Optional.of(year));
        when(teacherAssignmentRepository.save(any(TeacherAssignment.class))).thenAnswer(invocation -> {
            TeacherAssignment saved = invocation.getArgument(0);
            saved.setId(8L);
            return saved;
        });

        AssignmentResponse response = teacherAssignmentService.createAssignment(SCHOOL_ID, request);

        assertThat(response.id()).isEqualTo(8L);
        assertThat(response.teacherName()).isEqualTo("Taylor Teacher");
        assertThat(response.subjectName()).isEqualTo("Mathematics");
        assertThat(response.className()).isEqualTo("S3");
        assertThat(response.academicYear()).isEqualTo(2026);
        verify(teacherAssignmentRepository).save(any(TeacherAssignment.class));
    }

    @Test
    @DisplayName("should throw when teacher missing")
    void shouldThrowWhenTeacherMissing() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherAssignmentService.createAssignment(
                SCHOOL_ID, new CreateAssignmentRequest(2L, 3L, 4L, null, 5L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("should throw when subject missing")
    void shouldThrowWhenSubjectMissing() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher()));
        when(subjectRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherAssignmentService.createAssignment(
                SCHOOL_ID, new CreateAssignmentRequest(2L, 3L, 4L, null, 5L)))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);
    }

    @Test
    @DisplayName("should throw when class missing")
    void shouldThrowWhenClassMissing() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher()));
        when(subjectRepository.findById(3L)).thenReturn(Optional.of(
                Subject.builder().id(3L).school(school()).build()));
        when(classRepository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherAssignmentService.createAssignment(
                SCHOOL_ID, new CreateAssignmentRequest(2L, 3L, 4L, null, 5L)))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);
    }

    @Test
    @DisplayName("should delete assignment when it exists")
    void shouldDeleteWhenExists() {
        when(teacherAssignmentRepository.existsById(8L)).thenReturn(true);

        teacherAssignmentService.deleteAssignment(8L);

        verify(teacherAssignmentRepository).deleteById(8L);
    }

    private TeacherAssignment assignment() {
        return TeacherAssignment.builder()
                .id(8L)
                .teacher(teacher())
                .subject(Subject.builder().id(3L).name("Mathematics").build())
                .classEntity(ClassEntity.builder().id(4L).name("S3").build())
                .academicYear(academicYear())
                .build();
    }

    private User teacher() {
        return User.builder()
                .id(2L)
                .school(school())
                .fullName("Taylor Teacher")
                .role(Role.TEACHER)
                .build();
    }

    private School school() {
        return School.builder().id(SCHOOL_ID).build();
    }

    private AcademicYear academicYear() {
        return AcademicYear.builder().id(5L).school(school()).year(2026).build();
    }
}
