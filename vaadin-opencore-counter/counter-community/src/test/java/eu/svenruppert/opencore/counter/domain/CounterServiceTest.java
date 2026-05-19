package eu.svenruppert.opencore.counter.domain;

import eu.svenruppert.opencore.counter.extension.CounterEventListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CounterServiceTest {

  private static final class RecordingListener implements CounterEventListener {
    final List<CounterChangedEvent> events = new ArrayList<>();

    @Override
    public void onCounterChanged(CounterChangedEvent event) {
      events.add(event);
    }
  }

  @Test
  @DisplayName("service delegates value to state")
  void valueDelegatesToState() {
    CounterService service = new CounterService(new CounterState(), List.of());
    assertEquals(0, service.value());
  }

  @Test
  @DisplayName("increment updates value and publishes one event")
  void incrementPublishesEvent() {
    RecordingListener listener = new RecordingListener();
    CounterService service = new CounterService(new CounterState(), List.of(listener));

    service.increment();

    assertEquals(1, service.value());
    assertEquals(1, listener.events.size());
    assertEquals(CounterAction.INCREMENT, listener.events.get(0).action());
    assertEquals(0, listener.events.get(0).oldValue());
    assertEquals(1, listener.events.get(0).newValue());
  }

  @Test
  @DisplayName("decrement updates value and publishes event")
  void decrementPublishesEvent() {
    RecordingListener listener = new RecordingListener();
    CounterService service = new CounterService(new CounterState(), List.of(listener));

    service.decrement();

    assertEquals(-1, service.value());
    assertEquals(1, listener.events.size());
    assertEquals(CounterAction.DECREMENT, listener.events.get(0).action());
  }

  @Test
  @DisplayName("reset updates value and publishes event with previous value")
  void resetPublishesEvent() {
    RecordingListener listener = new RecordingListener();
    CounterService service = new CounterService(new CounterState(), List.of(listener));

    service.increment();
    service.increment();
    service.reset();

    assertEquals(0, service.value());
    assertEquals(3, listener.events.size());
    CounterChangedEvent resetEvent = listener.events.get(2);
    assertEquals(CounterAction.RESET, resetEvent.action());
    assertEquals(2, resetEvent.oldValue());
    assertEquals(0, resetEvent.newValue());
  }

  @Test
  @DisplayName("multiple listeners all receive the same event")
  void multipleListenersReceiveEvents() {
    RecordingListener a = new RecordingListener();
    RecordingListener b = new RecordingListener();
    CounterService service = new CounterService(new CounterState(), List.of(a, b));

    service.increment();
    service.decrement();
    service.reset();

    assertEquals(3, a.events.size());
    assertEquals(3, b.events.size());
    assertEquals(a.events.get(0).action(), b.events.get(0).action());
    assertEquals(a.events.get(1).action(), b.events.get(1).action());
    assertEquals(a.events.get(2).action(), b.events.get(2).action());
  }

  @Test
  @DisplayName("no events are lost across many operations")
  void noEventsLost() {
    RecordingListener listener = new RecordingListener();
    CounterService service = new CounterService(new CounterState(), List.of(listener));

    for (int i = 0; i < 5; i++) {
      service.increment();
    }
    for (int i = 0; i < 2; i++) {
      service.decrement();
    }
    service.reset();

    assertEquals(8, listener.events.size());
  }
}
