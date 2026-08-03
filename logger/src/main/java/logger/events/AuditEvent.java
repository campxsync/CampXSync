package logger.events;

import logger.dto.AuditLogRecord;

public class AuditEvent {
    private final AuditLogRecord record;

    public AuditEvent(AuditLogRecord record) {
        this.record = record;
    }

    public AuditLogRecord getRecord() {
        return record;
    }
}
