package com.svenruppert.opencore.counter.extension;

import com.vaadin.flow.component.Component;

import java.util.function.Supplier;

/**
 * A piece of UI that a feature contributes to the navbar, shown right
 * of the application title.
 *
 * <p>Suppliers are used instead of Component instances because a Vaadin
 * Component can only be attached to one parent at a time — each call
 * to {@link #componentFactory()} produces a fresh instance for the
 * current UI.
 */
public interface NavbarContribution {

  /** Stable id, used for diagnostics. */
  String id();

  /** Returns a fresh component instance to be added to the navbar. */
  Supplier<Component> componentFactory();

  /** Ordering: lower is rendered further to the left. */
  default int order() {
    return 1000;
  }
}
