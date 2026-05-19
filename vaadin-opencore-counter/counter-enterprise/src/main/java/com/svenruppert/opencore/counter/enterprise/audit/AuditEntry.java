package com.svenruppert.opencore.counter.enterprise.audit;

import java.time.Instant;
import java.util.Objects;

public record AuditEntry(
    Instant timestamp,
    String eventType,
    String message
) {
  public AuditEntry {
    Objects.requireNonNull(timestamp, "timestamp");
    Objects.requireNonNull(eventType, "eventType");
    Objects.requireNonNull(message, "message");
  }
}
