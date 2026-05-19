package eu.svenruppert.opencore.counter.enterprise.history;

import eu.svenruppert.opencore.counter.domain.CounterAction;

import java.time.Instant;
import java.util.Objects;

public record HistoryEntry(
    Instant timestamp,
    int oldValue,
    int newValue,
    CounterAction action
) {
  public HistoryEntry {
    Objects.requireNonNull(timestamp, "timestamp");
    Objects.requireNonNull(action, "action");
  }
}
