package com.musomi.desktop.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Utility for running asynchronous background tasks without blocking the JavaFX UI thread.
 *
 * <p>Executes work on a dedicated daemon thread and guarantees callbacks
 * are invoked on the JavaFX Application Thread.
 */
public final class TaskRunner {

    private static final String THREAD_NAME = "api-task";

    private TaskRunner() {}

    /**
     * Executes the given work callable in a background daemon thread.
     *
     * @param <T>       result type
     * @param work      work to execute off the JavaFX Application Thread
     * @param onSuccess callback receiving the result on the JavaFX Application Thread
     * @param onError   callback receiving any exception on the JavaFX Application Thread
     */
    public static <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Exception> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            if (onError != null) {
                Throwable ex = task.getException();
                Exception exception = ex instanceof Exception exc ? exc : new Exception(ex);
                onError.accept(exception);
            }
        }));

        Thread thread = new Thread(task, THREAD_NAME);
        thread.setDaemon(true);
        thread.start();
    }
}
