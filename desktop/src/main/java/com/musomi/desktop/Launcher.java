package com.musomi.desktop;

/**
 * Launcher class for the Musomi Manager desktop application.
 *
 * <p>This class does NOT extend {@link javafx.application.Application}.
 * Launching from a non-Application main class tells the JVM to load JavaFX
 * from the classpath rather than requiring it on the module path — a
 * well-known workaround for the "JavaFX runtime components are missing"
 * error that occurs when running an Application subclass directly.
 */
public final class Launcher {

    private Launcher() {}

    public static void main(String[] args) {
        DesktopApplication.main(args);
    }
}