package com.musomi.desktop.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.musomi.desktop.config.ApiConfig;
import com.musomi.desktop.config.AppConfig;
import com.musomi.desktop.config.SceneManager;
import com.musomi.desktop.config.Session;
import com.musomi.desktop.model.dto.ApiResponse;
import com.musomi.desktop.util.MockData;

/**
 * Singleton HTTP client for communicating with the Musomi Manager backend REST API.
 *
 * <p>Handles JSON serialization/deserialization via Jackson, token injection
 * from the active {@link Session}, and error translation into {@link ApiException}.
 * When mock mode is enabled, calls are delegated to {@link MockData}.
 */
public final class ApiClient {

    private static final Logger log = LoggerFactory.getLogger(ApiClient.class);

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    private static final class Holder {
        private static final ApiClient INSTANCE = new ApiClient();
    }

    private ApiClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Returns the singleton ApiClient instance.
     *
     * @return the {@code ApiClient} singleton
     */
    public static ApiClient getInstance() {
        return Holder.INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private final HttpClient client;
    private final ObjectMapper mapper;

    // -------------------------------------------------------------------------
    // Public HTTP API
    // -------------------------------------------------------------------------

    /**
     * Executes an HTTP GET request.
     *
     * @param <T>  expected response data type
     * @param path API endpoint path (e.g. "/auth/me")
     * @param type response class type
     * @return deserialized data payload
     */
    public <T> T get(String path, Class<T> type) {
        if (AppConfig.getInstance().isMockMode()) {
            return MockData.respond("GET", path, null, type);
        }
        return exchange("GET", path, null, type);
    }

    /**
     * Executes an HTTP POST request.
     *
     * @param <T>  expected response data type
     * @param path API endpoint path (e.g. "/auth/login")
     * @param body request payload object
     * @param type response class type
     * @return deserialized data payload
     */
    public <T> T post(String path, Object body, Class<T> type) {
        if (AppConfig.getInstance().isMockMode()) {
            return MockData.respond("POST", path, body, type);
        }
        return exchange("POST", path, body, type);
    }

    /**
     * Executes an HTTP PUT request.
     *
     * @param <T>  expected response data type
     * @param path API endpoint path
     * @param body request payload object
     * @param type response class type
     * @return deserialized data payload
     */
    public <T> T put(String path, Object body, Class<T> type) {
        if (AppConfig.getInstance().isMockMode()) {
            return MockData.respond("PUT", path, body, type);
        }
        return exchange("PUT", path, body, type);
    }

    /**
     * Executes an HTTP DELETE request.
     *
     * @param path API endpoint path
     */
    public void delete(String path) {
        if (AppConfig.getInstance().isMockMode()) {
            MockData.respond("DELETE", path, null, Void.class);
            return;
        }
        exchange("DELETE", path, null, Void.class);
    }

    // -------------------------------------------------------------------------
    // Internal Exchange
    // -------------------------------------------------------------------------

    private <T> T exchange(String method, String path, Object body, Class<T> type) {
        log.debug("{} {}", method, path);

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(resolveUri(path))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            if (Session.getInstance().isAuthenticated()) {
                builder.header("Authorization", "Bearer " + Session.getInstance().getToken());
            }

            HttpRequest.BodyPublisher publisher;
            if (body != null) {
                String json = mapper.writeValueAsString(body);
                publisher = HttpRequest.BodyPublishers.ofString(json);
            } else {
                publisher = HttpRequest.BodyPublishers.noBody();
            }

            builder.method(method, publisher);

            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                if (response.statusCode() == 401) {
                    Session.getInstance().clear();
                    SceneManager.getInstance().handleUnauthorizedResponse();
                }
                throw ApiException.from(response);
            }

            if (type == Void.class || response.statusCode() == 204
                    || response.body() == null || response.body().isBlank()) {
                return null;
            }

            JavaType javaType = mapper.getTypeFactory().constructParametricType(ApiResponse.class, type);
            ApiResponse<T> wrapper = mapper.readValue(response.body(), javaType);
            return wrapper != null ? wrapper.getData() : null;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ApiException("Network error: " + e.getMessage(), e);
        }
    }

    private URI resolveUri(String path) {
        String base = ApiConfig.getInstance().getBaseUrl();
        if (base.endsWith("/") && path.startsWith("/")) {
            return URI.create(base + path.substring(1));
        } else if (!base.endsWith("/") && !path.startsWith("/")) {
            return URI.create(base + "/" + path);
        } else {
            return URI.create(base + path);
        }
    }
}
