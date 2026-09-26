package com.musomi.desktop.config;

/**
 * Thin wrapper around {@link AppConfig} that exposes API-specific
 * configuration values.
 *
 * <p>Callers that only need API settings should depend on this class rather
 * than {@link AppConfig} directly, keeping the dependency surface narrow and
 * making future per-environment overrides easier to introduce.
 */
public final class ApiConfig {

    // ------------------------------------------------------------------
    // Singleton
    // ------------------------------------------------------------------

    private static final class Holder {
        private static final ApiConfig INSTANCE = new ApiConfig();
    }

    private ApiConfig() {}

    /**
     * Returns the application-wide singleton instance.
     *
     * @return the {@code ApiConfig} singleton
     */
    public static ApiConfig getInstance() {
        return Holder.INSTANCE;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------

    /**
     * Returns the base URL of the Musomi REST API.
     *
     * <p>Delegates to {@link AppConfig#getApiBaseUrl()}.  The value is
     * resolved from {@code config.properties} (key {@code api.baseUrl}) and
     * may be overridden at runtime via {@code -Dmusomi.api.baseUrl=…}.
     *
     * @return the API base URL string; never {@code null} or blank
     */
    public String getBaseUrl() {
        return AppConfig.getInstance().getApiBaseUrl();
    }
}
