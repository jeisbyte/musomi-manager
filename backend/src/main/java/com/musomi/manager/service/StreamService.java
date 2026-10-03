package com.musomi.manager.service;

import java.time.LocalDateTime;
import java.util.List;

import com.musomi.manager.dto.request.CreateStreamRequest;
import com.musomi.manager.dto.response.StreamResponse;
import com.musomi.manager.entity.ClassEntity;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.Stream;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.mapper.StreamMapper;
import com.musomi.manager.repository.ClassRepository;
import com.musomi.manager.repository.StreamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Manages streams within classes. */
@Service
@RequiredArgsConstructor
@Slf4j
public class StreamService {

    private static final long DEFAULT_SCHOOL_ID = 1L;

    private final StreamRepository streamRepository;
    private final ClassRepository classRepository;

    /** Lists active streams within a class. */
    @Transactional(readOnly = true)
    public List<StreamResponse> listStreams(Long classId) {
        return StreamMapper.toResponseList(streamRepository.findByClassEntityIdAndIsActiveTrue(classId));
    }

    /** Creates a stream within a school's class. */
    @Transactional
    public StreamResponse createStream(Long schoolId, CreateStreamRequest request) {
        Long resolvedSchoolId = schoolId != null ? schoolId : DEFAULT_SCHOOL_ID;
        ClassEntity classEntity = classRepository.findById(request.classId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED));
        if (classEntity.getSchool() == null
                || !resolvedSchoolId.equals(classEntity.getSchool().getId())) {
            throw new ResourceNotFoundException(ErrorCode.VALIDATION_FAILED);
        }
        if (streamRepository.existsByClassEntityIdAndName(request.classId(), request.name())) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED);
        }

        Stream stream = Stream.builder()
                .school(School.builder().id(resolvedSchoolId).build())
                .classEntity(classEntity)
                .name(request.name())
                .capacity(request.capacity())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        Stream saved = streamRepository.save(stream);
        log.info("Stream created: streamId={}, classId={}, name={}",
                saved.getId(), request.classId(), saved.getName());
        return StreamMapper.toResponse(saved);
    }
}
