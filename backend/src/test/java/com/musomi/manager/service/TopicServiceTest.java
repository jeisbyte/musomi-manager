package com.musomi.manager.service;

import java.util.List;
import java.util.Optional;

import com.musomi.manager.dto.request.CreateTopicRequest;
import com.musomi.manager.dto.response.TopicResponse;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Subject;
import com.musomi.manager.entity.Topic;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.repository.SubjectRepository;
import com.musomi.manager.repository.TopicRepository;
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
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private TopicService topicService;

    @Test
    @DisplayName("should return topics when listed")
    void shouldReturnTopicsWhenListed() {
        when(topicRepository.findBySubjectIdOrderBySortOrderAsc(4L)).thenReturn(List.of(topic()));

        List<TopicResponse> topics = topicService.listTopics(4L);

        assertThat(topics).hasSize(1);
        assertThat(topics.getFirst().name()).isEqualTo("Algebra");
    }

    @Test
    @DisplayName("should create topic when subject exists")
    void shouldCreateTopicWhenSubjectExists() {
        when(subjectRepository.findById(4L)).thenReturn(Optional.of(subject()));
        when(topicRepository.existsBySubjectIdAndName(4L, "Geometry")).thenReturn(false);
        when(topicRepository.save(any(Topic.class))).thenAnswer(inv -> {
            Topic saved = inv.getArgument(0);
            saved.setId(8L);
            return saved;
        });

        TopicResponse response = topicService.createTopic(1L,
                new CreateTopicRequest(4L, "Geometry", null, 2));

        assertThat(response.id()).isEqualTo(8L);
        assertThat(response.subjectId()).isEqualTo(4L);
        assertThat(response.name()).isEqualTo("Geometry");
    }

    @Test
    @DisplayName("should throw when subject is missing")
    void shouldThrowWhenSubjectMissing() {
        when(subjectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> topicService.createTopic(1L,
                new CreateTopicRequest(99L, "Geometry", null, 2)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);

        verify(topicRepository, never()).save(any(Topic.class));
    }

    private Topic topic() {
        return Topic.builder()
                .id(8L)
                .school(School.builder().id(1L).build())
                .subject(subject())
                .name("Algebra")
                .sortOrder(1)
                .build();
    }

    private Subject subject() {
        return Subject.builder()
                .id(4L)
                .school(School.builder().id(1L).build())
                .code("MATH")
                .name("Mathematics")
                .build();
    }
}
