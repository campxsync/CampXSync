package logger.events;

@FunctionalInterface
public interface AuditEventListener {
    void onAuditEvent(AuditEvent event);
}
