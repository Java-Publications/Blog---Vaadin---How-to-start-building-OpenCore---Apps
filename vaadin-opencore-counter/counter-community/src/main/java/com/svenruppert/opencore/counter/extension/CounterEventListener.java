package com.svenruppert.opencore.counter.extension;

import com.svenruppert.opencore.counter.domain.CounterChangedEvent;

@FunctionalInterface
public interface CounterEventListener {

  void onCounterChanged(CounterChangedEvent event);
}
