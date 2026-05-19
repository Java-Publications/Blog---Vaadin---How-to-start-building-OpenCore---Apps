package eu.svenruppert.opencore.counter.extension;

import eu.svenruppert.opencore.counter.domain.CounterChangedEvent;

@FunctionalInterface
public interface CounterEventListener {

  void onCounterChanged(CounterChangedEvent event);
}
