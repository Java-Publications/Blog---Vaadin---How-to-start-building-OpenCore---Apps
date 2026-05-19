package eu.svenruppert.opencore.counter.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public final class FeatureRegistry {

  private final List<FeatureContribution> features;
  private final List<RouteContribution> routes;
  private final List<MenuContribution> menuItems;
  private final List<NavbarContribution> navbarItems;
  private final List<CounterEventListener> counterEventListeners;

  public FeatureRegistry() {
    this(loadViaServiceLoader());
  }

  public FeatureRegistry(List<FeatureContribution> contributions) {
    List<FeatureContribution> sorted = new ArrayList<>(contributions);
    sorted.sort(Comparator.comparingInt(FeatureContribution::order));
    this.features = Collections.unmodifiableList(sorted);

    List<RouteContribution> collectedRoutes = new ArrayList<>();
    Set<String> seenRoutePaths = new HashSet<>();
    for (FeatureContribution feature : this.features) {
      for (RouteContribution route : feature.routes()) {
        if (!seenRoutePaths.add(route.path())) {
          throw new IllegalStateException(
              "Duplicate route path '" + route.path()
                  + "' contributed by feature '" + feature.id() + "'");
        }
        collectedRoutes.add(route);
      }
    }
    this.routes = Collections.unmodifiableList(collectedRoutes);

    List<MenuContribution> collectedMenu = new ArrayList<>();
    Set<String> seenMenuKeys = new HashSet<>();
    for (FeatureContribution feature : this.features) {
      for (MenuContribution item : feature.menuItems()) {
        String key = item.label() + "|" + item.path();
        if (!seenMenuKeys.add(key)) {
          throw new IllegalStateException(
              "Duplicate menu entry '" + item.label() + "' for path '"
                  + item.path() + "' contributed by feature '" + feature.id() + "'");
        }
        collectedMenu.add(item);
      }
    }
    collectedMenu.sort(Comparator.comparingInt(MenuContribution::order));
    this.menuItems = Collections.unmodifiableList(collectedMenu);

    List<CounterEventListener> collectedListeners = new ArrayList<>();
    for (FeatureContribution feature : this.features) {
      if (feature instanceof CounterEventFeature eventFeature) {
        collectedListeners.addAll(eventFeature.counterEventListeners());
      }
    }
    this.counterEventListeners = Collections.unmodifiableList(collectedListeners);

    List<NavbarContribution> collectedNavbar = new ArrayList<>();
    for (FeatureContribution feature : this.features) {
      collectedNavbar.addAll(feature.navbarItems());
    }
    collectedNavbar.sort(Comparator.comparingInt(NavbarContribution::order));
    this.navbarItems = Collections.unmodifiableList(collectedNavbar);
  }

  private static List<FeatureContribution> loadViaServiceLoader() {
    List<FeatureContribution> result = new ArrayList<>();
    for (FeatureContribution contribution : ServiceLoader.load(FeatureContribution.class)) {
      result.add(contribution);
    }
    return result;
  }

  public List<FeatureContribution> features() {
    return features;
  }

  public List<RouteContribution> routes() {
    return routes;
  }

  public List<MenuContribution> menuItems() {
    return menuItems;
  }

  public List<CounterEventListener> counterEventListeners() {
    return counterEventListeners;
  }

  public List<NavbarContribution> navbarItems() {
    return navbarItems;
  }

  public boolean hasFeature(String id) {
    for (FeatureContribution feature : features) {
      if (feature.id().equals(id)) {
        return true;
      }
    }
    return false;
  }
}
