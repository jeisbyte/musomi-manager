package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.TopicResponse;
import com.musomi.manager.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin endpoint for listing topics by subject. */
@RestController
@RequestMapping("/api/v1/admin/topics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTopicApiController {

    private final TopicService topicService;

    /** Lists topics within a subject. */
    @GetMapping
    public ApiResponse<List<TopicResponse>> listTopics(@RequestParam Long subjectId) {
        return ApiResponse.success(topicService.listTopics(subjectId));
    }
}
