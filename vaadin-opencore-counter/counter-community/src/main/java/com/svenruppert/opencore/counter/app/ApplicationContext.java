package com.svenruppert.opencore.counter.app;

import com.svenruppert.opencore.counter.domain.CounterService;
import com.svenruppert.opencore.counter.domain.CounterState;
import com.svenruppert.opencore.counter.extension.FeatureRegistry;

public final class ApplicationContext {

  private final FeatureRegistry featureRegistry;
  private final CounterState counterState;
  private final CounterService counterService;

  public ApplicationContext() {
    this(new FeatureRegistry());
  }

  public ApplicationContext(FeatureRegistry featureRegistry) {
    this.featureRegistry = featureRegistry;
    this.counterState = new CounterState();
    this.counterService = new CounterService(
        counterState,
        featureRegistry.counterEventListeners());
  }

  public FeatureRegistry featureRegistry() {
    return featureRegistry;
  }

  public CounterState counterState() {
    return counterState;
  }

  public CounterService counterService() {
    return counterService;
  }
}
