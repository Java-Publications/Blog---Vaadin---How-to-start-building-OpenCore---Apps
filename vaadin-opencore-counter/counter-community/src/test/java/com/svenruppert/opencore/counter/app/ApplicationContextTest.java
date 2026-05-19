package com.svenruppert.opencore.counter.app;

import com.svenruppert.opencore.counter.domain.CounterService;
import com.svenruppert.opencore.counter.domain.CounterState;
import com.svenruppert.opencore.counter.extension.FeatureRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ApplicationContextTest {

  @Test
  @DisplayName("default constructor wires service, state and registry via ServiceLoader")
  void defaultConstructorWiresEverything() {
    ApplicationContext context = new ApplicationContext();

    assertNotNull(context.featureRegistry());
    assertNotNull(context.counterState());
    assertNotNull(context.counterService());
    assertEquals(0, context.counterState().value());
    assertEquals(0, context.counterService().value());
  }

  @Test
  @DisplayName("counterState() returns the same instance the service writes to")
  void counterStateInstanceIsShared() {
    ApplicationContext context = new ApplicationContext();
    CounterState state = context.counterState();
    CounterService service = context.counterService();

    service.increment();

    assertEquals(1, state.value(),
        "Service and state must operate on the same CounterState");
  }

  @Test
  @DisplayName("explicit constructor uses the provided FeatureRegistry")
  void explicitConstructorRespectsRegistry() {
    FeatureRegistry registry = new FeatureRegistry(List.of());
    ApplicationContext context = new ApplicationContext(registry);

    assertSame(registry, context.featureRegistry());
    assertEquals(0, context.featureRegistry().features().size());
  }

  @Test
  @DisplayName("Application static holder exposes a non-null context that can be replaced")
  void applicationHolderExposesContext() {
    ApplicationContext before = Application.context();
    assertNotNull(before);

    ApplicationContext replacement = new ApplicationContext(new FeatureRegistry(List.of()));
    Application.replaceContext(replacement);
    assertSame(replacement, Application.context());

    Application.replaceContext(before);
    assertSame(before, Application.context());
  }
}
