package com.svenruppert.opencore.counter.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CounterStateTest {

  @Test
  @DisplayName("initial value is zero")
  void initialValueIsZero() {
    CounterState state = new CounterState();
    assertEquals(0, state.value());
  }

  @Test
  @DisplayName("increment increases value by 1 and reports correct old/new values")
  void incrementIncreasesValue() {
    CounterState state = new CounterState();
    CounterChangedEvent event = state.increment();

    assertEquals(1, state.value());
    assertEquals(0, event.oldValue());
    assertEquals(1, event.newValue());
    assertEquals(CounterAction.INCREMENT, event.action());
    assertNotNull(event.timestamp());
  }

  @Test
  @DisplayName("two increments produce value 2")
  void twoIncrementsProduceTwo() {
    CounterState state = new CounterState();
    state.increment();
    CounterChangedEvent second = state.increment();

    assertEquals(2, state.value());
    assertEquals(1, second.oldValue());
    assertEquals(2, second.newValue());
  }

  @Test
  @DisplayName("decrement decreases value by 1 and reports correct old/new values")
  void decrementDecreasesValue() {
    CounterState state = new CounterState();
    CounterChangedEvent event = state.decrement();

    assertEquals(-1, state.value());
    assertEquals(0, event.oldValue());
    assertEquals(-1, event.newValue());
    assertEquals(CounterAction.DECREMENT, event.action());
  }

  @Test
  @DisplayName("reset returns value to zero and reports old value")
  void resetReturnsToZero() {
    CounterState state = new CounterState();
    state.increment();
    state.increment();
    state.increment();

    CounterChangedEvent reset = state.reset();

    assertEquals(0, state.value());
    assertEquals(3, reset.oldValue());
    assertEquals(0, reset.newValue());
    assertEquals(CounterAction.RESET, reset.action());
  }

  @Test
  @DisplayName("event timestamp is non null and contains the action")
  void eventTimestampPopulated() {
    CounterState state = new CounterState();
    CounterChangedEvent event = state.increment();
    assertNotNull(event.timestamp());
    assertEquals(CounterAction.INCREMENT, event.action());
  }
}
