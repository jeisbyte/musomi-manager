package com.musomi.manager.service;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.dto.request.CreateStreamRequest;
import com.musomi.manager.dto.response.StreamResponse;
import com.musomi.manager.entity.ClassEntity;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Stream;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.repository.ClassRepository;
import com.musomi.manager.repository.StreamRepository;
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
class StreamServiceTest {

    @Mock
    private StreamRepository streamRepository;

    @Mock
    private ClassRepository classRepository;

    @InjectMocks
    private StreamService streamService;

    @Test
    @DisplayName("should return streams when listed")
    void shouldReturnStreamsWhenListed() {
        when(streamRepository.findByClassEntityIdAndIsActiveTrue(5L)).thenReturn(List.of(stream()));

        List<StreamResponse> streams = streamService.listStreams(5L);

        assertThat(streams).hasSize(1);
        assertThat(streams.getFirst().name()).isEqualTo("Blue");
    }

    @Test
    @DisplayName("should create stream when class exists")
    void shouldCreateStreamWhenClassExists() {
        when(classRepository.findById(5L)).thenReturn(Optional.of(classEntity()));
        when(streamRepository.existsByClassEntityIdAndName(5L, "Green")).thenReturn(false);
        when(streamRepository.save(any(Stream.class))).thenAnswer(inv -> {
            Stream saved = inv.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        StreamResponse response = streamService.createStream(1L, new CreateStreamRequest(5L, "Green", 40));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.classId()).isEqualTo(5L);
        assertThat(response.name()).isEqualTo("Green");
    }

    @Test
    @DisplayName("should throw when class is missing")
    void shouldThrowWhenClassMissing() {
        when(classRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> streamService.createStream(1L, new CreateStreamRequest(99L, "Green", 40)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);

        verify(streamRepository, never()).save(any(Stream.class));
    }

    private Stream stream() {
        return Stream.builder()
                .id(7L)
                .school(School.builder().id(1L).build())
                .classEntity(classEntity())
                .name("Blue")
                .capacity(40)
                .isActive(true)
                .build();
    }

    private ClassEntity classEntity() {
        return ClassEntity.builder()
                .id(5L)
                .school(School.builder().id(1L).build())
                .name("S1")
                .build();
    }
}
