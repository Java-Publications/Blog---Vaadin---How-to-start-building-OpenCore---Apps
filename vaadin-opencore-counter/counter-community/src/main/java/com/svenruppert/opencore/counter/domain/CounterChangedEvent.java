package com.svenruppert.opencore.counter.domain;

import java.time.Instant;
import java.util.Objects;

public record CounterChangedEvent(
    int oldValue,
    int newValue,
    CounterAction action,
    Instant timestamp
) {
  public CounterChangedEvent {
    Objects.requireNonNull(action, "action");
    Objects.requireNonNull(timestamp, "timestamp");
  }
}
