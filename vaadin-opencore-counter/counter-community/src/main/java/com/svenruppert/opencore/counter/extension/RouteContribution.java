package com.svenruppert.opencore.counter.extension;

import com.vaadin.flow.component.Component;

import java.util.Objects;

public record RouteContribution(
    String path,
    Class<? extends Component> viewClass
) {
  public RouteContribution {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(viewClass, "viewClass");
    if (path.startsWith("/")) {
      throw new IllegalArgumentException("Route path must not start with '/': " + path);
    }
  }
}
