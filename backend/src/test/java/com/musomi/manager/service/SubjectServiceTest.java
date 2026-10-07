package com.musomi.manager.service;

import java.util.List;

import com.musomi.manager.dto.request.CreateSubjectRequest;
import com.musomi.manager.dto.response.SubjectResponse;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Subject;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.repository.SubjectRepository;
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
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private SubjectService subjectService;

    @Test
    @DisplayName("should return subjects when listed")
    void shouldReturnSubjectsWhenListed() {
        when(subjectRepository.findBySchoolIdAndIsActiveTrueOrderByNameAsc(1L))
                .thenReturn(List.of(subject()));

        List<SubjectResponse> subjects = subjectService.listSubjects(1L);

        assertThat(subjects).hasSize(1);
        assertThat(subjects.getFirst().code()).isEqualTo("MATH");
    }

    @Test
    @DisplayName("should create subject when code is available")
    void shouldCreateSubjectWhenCodeAvailable() {
        when(subjectRepository.existsBySchoolIdAndCode(1L, "ENG")).thenReturn(false);
        when(subjectRepository.save(any(Subject.class))).thenAnswer(inv -> {
            Subject saved = inv.getArgument(0);
            saved.setId(4L);
            return saved;
        });

        SubjectResponse response = subjectService.createSubject(1L,
                new CreateSubjectRequest("ENG", "English", null, true));

        assertThat(response.id()).isEqualTo(4L);
        assertThat(response.code()).isEqualTo("ENG");
        assertThat(response.isCore()).isTrue();
    }

    @Test
    @DisplayName("should throw when code already exists")
    void shouldThrowWhenCodeAlreadyExists() {
        when(subjectRepository.existsBySchoolIdAndCode(1L, "MATH")).thenReturn(true);

        assertThatThrownBy(() -> subjectService.createSubject(1L,
                new CreateSubjectRequest("MATH", "Mathematics", null, true)))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);

        verify(subjectRepository, never()).save(any(Subject.class));
    }

    private Subject subject() {
        return Subject.builder()
                .id(4L)
                .school(School.builder().id(1L).build())
                .code("MATH")
                .name("Mathematics")
                .isCore(true)
                .isActive(true)
                .build();
    }
}
