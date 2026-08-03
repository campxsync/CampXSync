package logger.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AuditEventPublisher {
    private static final List<AuditEventListener> LISTENERS = new CopyOnWriteArrayList<>();
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "audit-event-publisher");
        t.setDaemon(true);
        return t;
    });

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
     * Publishes an audit event asynchronously using the executor thread pool.
     */
    public static void publishAsync(AuditEvent event) {
        if (event == null) {
            return;
        }
        EXECUTOR.submit(() -> publishSync(event));
    }
}
