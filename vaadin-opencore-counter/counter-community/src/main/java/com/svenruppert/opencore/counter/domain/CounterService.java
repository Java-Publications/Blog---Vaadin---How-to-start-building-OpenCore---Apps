package com.svenruppert.opencore.counter.domain;

import com.svenruppert.opencore.counter.extension.CounterEventListener;

import java.util.List;
import java.util.Objects;

public final class CounterService {

  private final CounterState state;
  private final List<CounterEventListener> listeners;

  public CounterService(CounterState state, List<CounterEventListener> listeners) {
    this.state = Objects.requireNonNull(state, "state");
    this.listeners = Objects.requireNonNull(listeners, "listeners");
  }

  public int value() {
    return state.value();
  }

  public void increment() {
    publish(state.increment());
  }

  public void decrement() {
    publish(state.decrement());
  }

  public void reset() {
    publish(state.reset());
  }

  private void publish(CounterChangedEvent event) {
    for (CounterEventListener listener : listeners) {
      listener.onCounterChanged(event);
    }
  }
}
