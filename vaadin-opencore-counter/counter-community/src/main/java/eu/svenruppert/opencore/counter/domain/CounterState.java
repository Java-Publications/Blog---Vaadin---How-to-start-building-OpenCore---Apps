package eu.svenruppert.opencore.counter.domain;

import java.time.Instant;

public final class CounterState {

  private int value;

  public int value() {
    return value;
  }

  public CounterChangedEvent increment() {
    int oldValue = value;
    value = oldValue + 1;
    return new CounterChangedEvent(oldValue, value, CounterAction.INCREMENT, Instant.now());
  }

  public CounterChangedEvent decrement() {
    int oldValue = value;
    value = oldValue - 1;
    return new CounterChangedEvent(oldValue, value, CounterAction.DECREMENT, Instant.now());
  }

  public CounterChangedEvent reset() {
    int oldValue = value;
    value = 0;
    return new CounterChangedEvent(oldValue, value, CounterAction.RESET, Instant.now());
  }
}
