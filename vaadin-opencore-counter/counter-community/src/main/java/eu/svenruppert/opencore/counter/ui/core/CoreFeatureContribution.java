package eu.svenruppert.opencore.counter.ui.core;

import eu.svenruppert.opencore.counter.extension.FeatureContribution;
import eu.svenruppert.opencore.counter.extension.MenuContribution;
import eu.svenruppert.opencore.counter.extension.RouteContribution;

import java.util.List;

public final class CoreFeatureContribution implements FeatureContribution {

  public static final String FEATURE_ID = "community.core";

  @Override
  public String id() {
    return FEATURE_ID;
  }

  @Override
  public List<RouteContribution> routes() {
    return List.of(
        new RouteContribution("", CounterView.class),
        new RouteContribution("about", AboutView.class));
  }

  @Override
  public List<MenuContribution> menuItems() {
    return List.of(
        new MenuContribution("Counter", "", 100, "vaadin:plus"),
        new MenuContribution("About", "about", 900, "vaadin:info-circle"));
  }

  @Override
  public int order() {
    return 100;
  }
}
