package com.svenruppert.opencore.counter.extension;

import java.util.Objects;

public record MenuContribution(
    String label,
    String path,
    int order,
    String iconName
) {
  public MenuContribution {
    Objects.requireNonNull(label, "label");
    Objects.requireNonNull(path, "path");
  }
}
