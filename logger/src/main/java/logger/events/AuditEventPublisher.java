package logger.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class AuditEventPublisher {
    private static final List<AuditEventListener> LISTENERS = new CopyOnWriteArrayList<>();

    /**
     * Bounded thread pool for async audit event publishing.
     *
     * - Core threads: 2 (always alive, handles steady-state load)
     * - Max threads: 8 (burst capacity)
     * - Queue: 500 bounded tasks (prevents unbounded memory growth)
     * - Rejection policy: CallerRunsPolicy — if the queue is full, the calling thread
     *   executes the task itself, providing natural backpressure instead of crashing.
     *
     * This replaces the previous Executors.newCachedThreadPool() which had no upper
     * bound on threads, risking OutOfMemoryError under sustained high load.
     */
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(
            2,
            8,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            r -> {
                Thread t = new Thread(r, "audit-event-publisher");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private AuditEventPublisher() {
        // Prevent instantiation
    }

    public static void registerListener(AuditEventListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void unregisterListener(AuditEventListener listener) {
        if (listener != null) {
            LISTENERS.remove(listener);
        }
    }

    /**
     * Publishes an audit event synchronously to all listeners.
     */
    public static void publishSync(AuditEvent event) {
        if (event == null) {
            return;
        }
        for (AuditEventListener listener : LISTENERS) {
            try {
                listener.onAuditEvent(event);
            } catch (Exception e) {
                // Prevent failures in one listener from blocking others
                System.err.println("Error in audit event listener execution: " + e.getMessage());
            }
        }
    }

    /**
     * Publishes an audit event asynchronously using the bounded executor thread pool.
     * If the queue is full, the CallerRunsPolicy will execute the task synchronously
     * in the calling thread to apply backpressure.
     */
    public static void publishAsync(AuditEvent event) {
        if (event == null) {
            return;
        }
        try {
            EXECUTOR.submit(() -> publishSync(event));
        } catch (RejectedExecutionException e) {
            // Should not happen with CallerRunsPolicy, but handle defensively
            publishSync(event);
        }
    }
}
