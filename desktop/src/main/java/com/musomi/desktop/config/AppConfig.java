package com.musomi.desktop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application-wide configuration loaded once from {@code config.properties}
 * on the classpath.
 *
 * <p>Any property may be overridden at JVM startup via a system property:
 * <pre>
 *   -Dmusomi.api.baseUrl=https://staging.musomi.com/api
 *   -Dmusomi.mock=true
 * </pre>
 *
 * <p>Required keys ({@code api.baseUrl}, {@code app.version}) cause an
 * {@link IllegalStateException} if they are absent from both the properties
 * file and system properties.
 */
public final class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    // ------------------------------------------------------------------
    // Property-file keys
    // ------------------------------------------------------------------

    private static final String PROPS_FILE      = "/config.properties";

    private static final String KEY_API_BASE_URL = "api.baseUrl";
    private static final String KEY_APP_VERSION  = "app.version";

    // ------------------------------------------------------------------
    // System-property override keys
    // ------------------------------------------------------------------

    private static final String SYS_API_BASE_URL = "musomi.api.baseUrl";
    private static final String SYS_MOCK_MODE    = "musomi.mock";

    // ------------------------------------------------------------------
    // Singleton (initialisation-on-demand holder)
    // ------------------------------------------------------------------

    private static final class Holder {
        private static final AppConfig INSTANCE = new AppConfig();
    }

    /**
     * Returns the application-wide singleton instance.
     *
     * @return the {@code AppConfig} singleton
     */
    public static AppConfig getInstance() {
        return Holder.INSTANCE;
    }

    // ------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------

    private final String  apiBaseUrl;
    private final String  appVersion;
    private final boolean mockMode;

    // ------------------------------------------------------------------
    // Constructor — loads and validates config
    // ------------------------------------------------------------------

    private AppConfig() {
        Properties props = loadProperties();

        this.apiBaseUrl = resolveRequired(props, KEY_API_BASE_URL, SYS_API_BASE_URL);
        this.appVersion = resolveRequired(props, KEY_APP_VERSION,  null);
        this.mockMode   = Boolean.parseBoolean(
                System.getProperty(SYS_MOCK_MODE,
                        props.getProperty(SYS_MOCK_MODE, "false"))
        );

        log.info("Musomi Manager configuration loaded:");
        log.info("  app.version  = {}", appVersion);
        log.info("  api.baseUrl  = {}", apiBaseUrl);
        log.info("  mock mode    = {}", mockMode);
    }

    // ------------------------------------------------------------------
    // Public accessors
    // ------------------------------------------------------------------

    /**
     * Returns the base URL of the Musomi REST API.
     * Never {@code null} or blank — an {@link IllegalStateException} is thrown
     * at startup if the key is absent.
     *
     * @return the API base URL string
     */
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    /**
     * Returns the application version string (e.g. {@code "1.0.0"}).
     * Never {@code null} or blank.
     *
     * @return the application version
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * Returns {@code true} when the application is running in mock mode
     * (controlled by {@code -Dmusomi.mock=true}).
     *
     * @return {@code true} if mock mode is active
     */
    public boolean isMockMode() {
        return mockMode;
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Loads {@value #PROPS_FILE} from the classpath into a {@link Properties}
     * object. Missing file is treated as an empty property set and logged at
     * WARN — system-property overrides still work in that case.
     *
     * @return a non-null {@link Properties} instance
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getResourceAsStream(PROPS_FILE)) {
            if (in == null) {
                log.warn("'{}' not found on classpath; relying on system properties", PROPS_FILE);
                return props;
            }
            props.load(in);
            log.debug("Loaded properties file: {}", PROPS_FILE);
        } catch (IOException ex) {
            log.warn("Failed to read '{}': {}; relying on system properties", PROPS_FILE, ex.getMessage());
        }
        return props;
    }

    /**
     * Resolves a required string value from, in priority order:
     * <ol>
     *   <li>the JVM system property identified by {@code sysPropKey} (if non-null)</li>
     *   <li>the properties file entry identified by {@code propKey}</li>
     * </ol>
     *
     * @param props      the loaded properties
     * @param propKey    key used in the properties file
     * @param sysPropKey JVM override key, or {@code null} if no override is defined
     * @return the resolved, non-blank value
     * @throws IllegalStateException if no value is found or the value is blank
     */
    private static String resolveRequired(Properties props, String propKey, String sysPropKey) {
        String value = null;

        if (sysPropKey != null) {
            value = System.getProperty(sysPropKey);
        }

        if (value == null || value.isBlank()) {
            value = props.getProperty(propKey);
        }

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required configuration key '" + propKey + "' is missing. "
                            + "Add it to " + PROPS_FILE
                            + (sysPropKey != null ? " or pass -D" + sysPropKey + "=<value>" : "")
                            + "."
            );
        }

        return value.strip();
    }
}
